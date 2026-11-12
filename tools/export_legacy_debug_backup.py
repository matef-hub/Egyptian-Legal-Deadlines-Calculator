#!/usr/bin/env python3
"""Export an existing *debuggable* Android Room database into an encrypted MEEAD transfer file.

Requires: Python 3.10+, Android platform-tools (adb), `pip install cryptography`.
Never uninstalls either application. Refuses devices/packages where 'run-as' is unavailable.
Source DB/WAL are copied to a temporary PRIVATE local directory, used read-only, then removed.
This is not a bypass for production non-debuggable Android app data isolation.
"""
from __future__ import annotations

import argparse
import getpass
import json
import os
from pathlib import Path
import sqlite3
import subprocess
import sys
import tempfile
from datetime import datetime, timezone

HEADER = b"MEEAD-ENC-1"
DATABASE_NAME = "egyptian_legal_deadlines.db"
TABLES = ("legal_rules", "saved_deadlines", "holidays", "rule_edit_history")
MAX_BYTES = 16 * 1024 * 1024
ALLOWED_PACKAGES = (
    "com.ateflaw.legaldeadlines.debug",
    "com.ateflaw.legaldeadlines.legalreview",
    "com.ateflaw.legaldeadlines",
)


def adb(*args: str, allow_missing: bool = False) -> bytes | None:
    process = subprocess.run(
        ["adb", *args], stdout=subprocess.PIPE, stderr=subprocess.PIPE, check=False
    )
    if process.returncode:
        if allow_missing:
            return None
        details = process.stderr.decode("utf-8", "replace").strip()
        raise RuntimeError("ADB failed: " + details[:800])
    return process.stdout


def extract_database(package: str, directory: Path) -> Path:
    adb("shell", "am", "force-stop", package)
    adb("exec-out", "run-as", package, "id")
    database = directory / DATABASE_NAME
    for suffix in ("", "-wal", "-shm"):
        destination = directory / (DATABASE_NAME + suffix)
        contents = adb(
            "exec-out", "run-as", package, "cat", "databases/" + DATABASE_NAME + suffix,
            allow_missing=bool(suffix),
        )
        if contents is not None and len(contents) > 0:
            destination.write_bytes(contents)
    if not database.is_file() or database.stat().st_size == 0:
        raise RuntimeError("The source database is missing. No changes were made to the app.")
    return database


def read_database(path: Path) -> dict[str, list[dict]]:
    connection = sqlite3.connect(f"file:{path}?mode=ro", uri=True)
    connection.row_factory = sqlite3.Row
    try:
        check = connection.execute("PRAGMA integrity_check").fetchone()[0]
        if check != "ok":
            raise RuntimeError(f"SQLite integrity check failed: {check}")
        found = {
            item[0] for item in connection.execute(
                "SELECT name FROM sqlite_master WHERE type='table'"
            )
        }
        tables: dict[str, list[dict]] = {}
        for table in TABLES:
            if table not in found:
                tables[table] = []
                continue
            records = []
            for row in connection.execute('SELECT * FROM "' + table + '"'):
                record = dict(row)
                if any(isinstance(value, bytes) for value in record.values()):
                    raise RuntimeError(f"Unsupported binary value in {table}.")
                records.append(record)
                if len(records) > 30_000:
                    raise RuntimeError("Too many rows in a single table.")
            tables[table] = records
        return tables
    finally:
        connection.close()


def encrypted_payload(data: bytes, password: str) -> bytes:
    from cryptography.hazmat.primitives.ciphers.aead import AESGCM
    from cryptography.hazmat.primitives.kdf.pbkdf2 import PBKDF2HMAC
    from cryptography.hazmat.primitives import hashes

    if not 1 <= len(data) <= MAX_BYTES:
        raise ValueError("The database snapshot exceeds the 16 MB transfer limit.")
    salt, nonce = os.urandom(16), os.urandom(12)
    kdf = PBKDF2HMAC(algorithm=hashes.SHA1(), length=32, salt=salt, iterations=310_000)
    key = kdf.derive(password.encode("utf-8"))
    ciphertext = AESGCM(key).encrypt(nonce, data, HEADER + salt + nonce)
    return HEADER + salt + nonce + ciphertext


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--package", required=True, choices=ALLOWED_PACKAGES,
        help="Exact installed applicationId. Confirm with adb shell pm list packages.",
    )
    parser.add_argument("--output", required=True, type=Path, help="New encrypted .meead file")
    args = parser.parse_args()
    target = args.output.expanduser().resolve()
    if target.exists():
        parser.error("Output file already exists. Refusing to overwrite a previous backup.")
    target.parent.mkdir(parents=True, exist_ok=True)

    secret = getpass.getpass("New backup password (12+ characters): ")
    if len(secret) < 12 or len(secret) > 128:
        parser.error("Password must contain between 12 and 128 characters.")
    if secret != getpass.getpass("Confirm backup password: "):
        parser.error("Passwords do not match.")

    try:
        with tempfile.TemporaryDirectory(prefix="meead-adb-transfer-") as tmp:
            source = extract_database(args.package, Path(tmp))
            rows = read_database(source)
            payload = {
                "format": "meead-transfer", "format_version": 1,
                "source_database_version": -1,
                "exported_at": datetime.now(timezone.utc).isoformat(),
                "tables": rows,
            }
            plaintext = json.dumps(payload, ensure_ascii=False, separators=(",", ":")).encode("utf-8")
            envelope = encrypted_payload(plaintext, secret)
        with target.open("xb") as output:
            output.write(envelope)
        try:
            target.chmod(0o600)
        except OSError:
            pass
        print(f"Created encrypted transfer: {target}")
        for table in TABLES:
            print(f"  {table}: {len(rows[table])}")
        print("Keep the old app installed until restoration has been verified on-device.")
        return 0
    except (OSError, RuntimeError, sqlite3.DatabaseError, ValueError) as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
