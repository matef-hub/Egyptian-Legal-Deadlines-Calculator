package com.ateflaw.legaldeadlines.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

enum class NoticeKind { ERROR, SUCCESS, INFO }

/**
 * Compact, SweetAlert-style feedback. The five-second progress bar and dialog
 * disappear together. Persistent legal reviewer notes live in the law catalog,
 * never in a blocking alert displayed when saving a case deadline.
 */
@Composable
fun SweetAlertNotice(
    message: String?,
    kind: NoticeKind,
    onDismiss: () -> Unit,
    durationMillis: Long = 5_000L
) {
    if (message.isNullOrBlank()) return

    val progress = remember(message, kind) { Animatable(1f) }
    LaunchedEffect(message, kind) {
        progress.snapTo(1f)
        progress.animateTo(0f, tween(durationMillis.coerceIn(200L, 20_000L).toInt()))
        onDismiss()
    }

    val statusColor = when (kind) {
        NoticeKind.SUCCESS -> MaterialTheme.colorScheme.primary
        NoticeKind.ERROR -> MaterialTheme.colorScheme.error
        NoticeKind.INFO -> MaterialTheme.colorScheme.tertiary
    }
    val icon = when (kind) {
        NoticeKind.SUCCESS -> Icons.Default.CheckCircle
        NoticeKind.ERROR -> Icons.Default.ErrorOutline
        NoticeKind.INFO -> Icons.Default.Info
    }
    val header = when (kind) {
        NoticeKind.SUCCESS -> "تم بنجاح"
        NoticeKind.ERROR -> "تعذّر إتمام العملية"
        NoticeKind.INFO -> "معلومة"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 18.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(62.dp).background(
                        statusColor.copy(alpha = 0.12f), CircleShape
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(38.dp)
                    )
                }
                Text(
                    text = header,
                    style = MaterialTheme.typography.titleLarge,
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(1.dp))
                LinearProgressIndicator(
                    progress = { progress.value },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = statusColor,
                    trackColor = statusColor.copy(alpha = 0.12f)
                )
                Button(onClick = onDismiss) { Text("حسنًا") }
            }
        }
    }
}
