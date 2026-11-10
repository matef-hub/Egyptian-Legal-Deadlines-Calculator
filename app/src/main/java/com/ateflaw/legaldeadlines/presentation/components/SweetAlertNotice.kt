package com.ateflaw.legaldeadlines.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

enum class NoticeKind { ERROR, SUCCESS, INFO }

/**
 * Five-second, non-persistent SweetAlert-style information dialog.
 * Only ephemeral UI notices disappear; legally significant review findings
 * remain readable in the relevant rule/result screen.
 */
@Composable
fun SweetAlertNotice(
    message: String?,
    kind: NoticeKind,
    onDismiss: () -> Unit,
    durationMillis: Long = 5_000L
) {
    if (message.isNullOrBlank()) return
    LaunchedEffect(message, kind) {
        delay(durationMillis)
        onDismiss()
    }
    val color = when(kind) {
        NoticeKind.ERROR -> MaterialTheme.colorScheme.error
        NoticeKind.SUCCESS -> MaterialTheme.colorScheme.primary
        NoticeKind.INFO -> MaterialTheme.colorScheme.tertiary
    }
    val symbol = when(kind) {
        NoticeKind.ERROR -> Icons.Default.ErrorOutline
        NoticeKind.SUCCESS -> Icons.Default.CheckCircle
        NoticeKind.INFO -> Icons.Default.Info
    }
    val title = when(kind) {
        NoticeKind.ERROR -> "تنبيه"
        NoticeKind.SUCCESS -> "تم بنجاح"
        NoticeKind.INFO -> "معلومة مهمة"
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(symbol, contentDescription = null, tint = color) },
        title = {
            Text(
                title,
                textAlign = TextAlign.Center,
                color = color,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(message, modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                Text("تُغلق الرسالة تلقائيًا بعد 5 ثوانٍ",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("إغلاق") } },
        shape = RoundedCornerShape(22.dp)
    )
}
