package com.ateflaw.legaldeadlines.presentation.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.ateflaw.legaldeadlines.data.backup.BackupTransferService
import com.ateflaw.legaldeadlines.data.database.AppDatabase
import com.ateflaw.legaldeadlines.presentation.components.NoticeKind
import com.ateflaw.legaldeadlines.presentation.components.SweetAlertNotice
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun BackupRestoreScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val transfer = remember(context) {
        BackupTransferService(AppDatabase.getInstance(context), context.contentResolver)
    }
    var password by remember { mutableStateOf("") } // never persist passwords in saved UI state
    var busy by remember { mutableStateOf(false) }
    var confirmation by remember { mutableStateOf(false) }
    var notice by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    fun validatePassword(): Boolean {
        if (password.length >= 12) return true
        error = "استخدم كلمة مرور من 12 حرفًا على الأقل واحتفظ بها؛ لا يمكن استرجاعها."
        return false
    }

    fun launchTransfer(uri: android.net.Uri, restoring: Boolean) {
        if (busy || !validatePassword()) return
        val secret = password.toCharArray()
        password = ""
        busy = true
        notice = null
        error = null
        scope.launch {
            try {
                val result = if (restoring) transfer.importFrom(uri, secret) else transfer.exportTo(uri, secret)
                notice = if (restoring) {
                    "تمت الاستعادة: ${result.savedDeadlines} موعد محفوظ، " +
                        "${result.editedRules} قاعدة معدلة، و${result.historyEntries} سجل مراجعة. " +
                        "أعد فتح الحاسبة والدليل لتحديث العرض."
                } else {
                    "تم إنشاء النسخة الاحتياطية المشفرة. تحتوي ${result.savedDeadlines} موعدًا محفوظًا؛ " +
                        "احتفظ بالملف وكلمة المرور في مكان آمن."
                }
            } catch (ex: Exception) {
                error = ex.localizedMessage ?: "تعذرت عملية نقل البيانات دون تغيير السجلات الحالية."
            } finally {
                secret.fill('\u0000')
                busy = false
            }
        }
    }

    val exportPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) launchTransfer(uri, restoring = false)
    }
    val importPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) launchTransfer(uri, restoring = true)
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("نقل البيانات والنسخ الاحتياطي", style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Text(
            "احفظ المواعيد والقواعد التي عدّلتها وسجل مراجعاتها في ملف مشفّر بكلمة مرور. " +
                "الملف لا يرفع تلقائيًا إلى أي خادم.",
            style = MaterialTheme.typography.bodyMedium
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Default.Lock, contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary)
            Text("AES-256-GCM • بدون صلاحيات تخزين عامة",
                style = MaterialTheme.typography.labelMedium)
        }
        OutlinedTextField(
            value = password,
            onValueChange = { password = it.take(128) },
            label = { Text("كلمة مرور النسخة الاحتياطية") },
            visualTransformation = PasswordVisualTransformation(),
            supportingText = { Text("12 حرفًا على الأقل. لا تحفظ كلمة المرور داخل التطبيق.") },
            enabled = !busy,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                if (validatePassword()) {
                    exportPicker.launch("meead-backup-${LocalDate.now()}.meead")
                }
            },
            enabled = !busy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.FileDownload, contentDescription = null)
            Spacer(Modifier.padding(horizontal = 4.dp))
            Text("تصدير نسخة احتياطية مشفرة")
        }
        OutlinedButton(
            onClick = { if (validatePassword()) confirmation = true },
            enabled = !busy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.FileUpload, contentDescription = null)
            Spacer(Modifier.padding(horizontal = 4.dp))
            Text("استعادة من ملف سابق")
        }
        Text(
            "الاستعادة مخصصة لتثبيت جديد بلا بيانات شخصية. إذا كان هذا التطبيق يحتوي بالفعل " +
                "على مواعيد أو تعديلات، سيتم رفض الاستعادة بدلًا من استبدالها. " +
                "نسخ Debug القديمة لا يمكن قراءتها مباشرة بسبب عزل تطبيقات Android؛ " +
                "يلزم تصديرها أولًا بالطريقة الموثقة.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
        if (busy) {
            CircularProgressIndicator()
            Text("جارٍ تنفيذ العملية محليًا…", style = MaterialTheme.typography.labelMedium)
        }
        TextButton(onClick = onBack, enabled = !busy) { Text("العودة إلى دليل المواعيد") }
    }

    if (confirmation) {
        AlertDialog(
            onDismissRequest = { confirmation = false },
            title = { Text("تأكيد استعادة البيانات") },
            text = {
                Text(
                    "لن تتم الاستعادة إلا إذا كان التطبيق الجديد خاليًا من بيانات المستخدم. " +
                        "يجب أن تكون النسخة من مصدر تثق به. لن نحذف أي سجلات محلية تلقائيًا."
                )
            },
            confirmButton = {
                Button(onClick = {
                    confirmation = false
                    importPicker.launch(arrayOf("*/*"))
                }) { Text("اختيار ملف النسخة") }
            },
            dismissButton = {
                TextButton(onClick = { confirmation = false }) { Text("إلغاء") }
            }
        )
    }

    SweetAlertNotice(
        message = error ?: notice,
        kind = if (error != null) NoticeKind.ERROR else NoticeKind.SUCCESS,
        onDismiss = { error = null; notice = null }
    )
}
