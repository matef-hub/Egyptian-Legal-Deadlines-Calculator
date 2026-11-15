"""Validate the published Egypt holiday calendar and its upgrade from a previous revision."""
import argparse
import json
from datetime import date
from pathlib import Path
from urllib.parse import urlsplit

MAX_BYTES = 1024 * 1024
OFFICIAL_HOSTS = {
    "cabinet.gov.eg", "www.cabinet.gov.eg", "alamiria.com", "www.alamiria.com",
    "presidency.eg", "www.presidency.eg", "sis.gov.eg", "www.sis.gov.eg",
    "moj.gov.eg", "www.moj.gov.eg",
}


def required_text(row, key):
    value = row[key]
    if not isinstance(value, str) or not value.strip() or len(value) > 300:
        raise ValueError(f"Invalid {key}")
    return value


def iso_date(value):
    parsed = date.fromisoformat(value)
    if parsed.isoformat() != value:
        raise ValueError(f"Expected YYYY-MM-DD: {value}")
    return parsed


def official_source(row):
    value = row["sourceUrl"]
    url = urlsplit(value)
    if (len(value) > 2048 or url.scheme != "https" or url.hostname not in OFFICIAL_HOSTS
            or url.username is not None or url.port is not None):
        raise ValueError(f"Invalid official source for {row.get('date')}")


def load_feed(path):
    raw = path.read_bytes()
    if not 0 < len(raw) <= MAX_BYTES:
        raise ValueError("The calendar is empty or exceeds 1 MiB")
    feed = json.loads(raw)
    if type(feed["schemaVersion"]) is not int or feed["schemaVersion"] != 1:
        raise ValueError("Unsupported calendar schema version")
    if type(feed["revision"]) is not int or not 0 < feed["revision"] <= 2**31 - 1:
        raise ValueError("Invalid calendar revision")
    published = iso_date(feed["publishedOn"])
    dates = set()
    for row in feed["holidays"]:
        day = iso_date(row["date"])
        if day in dates:
            raise ValueError(f"Duplicate holiday on {day}")
        dates.add(day)
        required_text(row, "name")
        required_text(row, "decision")
        official_source(row)
    if not dates:
        raise ValueError("The calendar must contain published dates")

    removed = set()
    for row in feed["superseded"]:
        day = iso_date(row["date"])
        if day in dates or day in removed:
            raise ValueError(f"Conflicting superseded date {day}")
        removed.add(day)
        replacement = row.get("replacementDate")
        if replacement is not None and iso_date(replacement) not in dates:
            raise ValueError(f"Missing replacement date for {day}")
        required_text(row, "decision")
        official_source(row)
    return feed["revision"], published, dates, removed


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("calendar", type=Path, nargs="?", default=Path("calendar/egypt-holidays.json"))
    parser.add_argument("--previous", type=Path)
    args = parser.parse_args()
    try:
        latest = load_feed(args.calendar)
        revision, published, dates, removed = latest
        if args.previous and args.calendar.read_bytes() != args.previous.read_bytes():
            old_revision, old_published, old_dates, old_removed = load_feed(args.previous)
            unresolved = (old_dates | old_removed) - dates - removed
            if revision <= old_revision or published < old_published or unresolved:
                raise ValueError(f"Expected a later revision with evidence for changed dates: {sorted(unresolved)}")
        print(f"Calendar revision {revision}, {len(dates)} holidays, {len(removed)} superseded dates")
    except (KeyError, OSError, TypeError, ValueError) as exc:
        parser.exit(1, f"Invalid calendar: {exc}\n")


if __name__ == "__main__":
    main()
