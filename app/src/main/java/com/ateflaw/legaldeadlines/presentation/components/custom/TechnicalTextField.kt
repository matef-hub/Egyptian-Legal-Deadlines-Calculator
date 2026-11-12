package com.ateflaw.legaldeadlines.presentation.components.custom

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ateflaw.legaldeadlines.presentation.ui.theme.BlueprintSteel
import com.ateflaw.legaldeadlines.presentation.ui.theme.JudicialNavy
import com.ateflaw.legaldeadlines.presentation.ui.theme.TerracottaSeal

@Composable
fun TechnicalTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector? = null,
    readOnly: Boolean = false,
    isError: Boolean = false,
    errorText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: @Composable (() -> Unit)? = null,
    innerContent: @Composable (() -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    Column(modifier = modifier) {
        // Label Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = BlueprintSteel,
                    modifier = Modifier.padding(end = 4.dp).height(16.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = JudicialNavy
            )
        }
        
        TechnicalContainer(
            strokeColor = if (isError) TerracottaSeal else BlueprintSteel,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (innerContent != null) {
                innerContent()
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        readOnly = readOnly,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = JudicialNavy),
                        cursorBrush = SolidColor(JudicialNavy),
                        keyboardOptions = keyboardOptions,
                        interactionSource = interactionSource,
                        modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                        decorationBox = { innerTextField ->
                            Box(modifier = Modifier.fillMaxWidth()) {
                                if (value.isEmpty() && !readOnly) {
                                    // Placeholder if needed, but usually we just let it be empty
                                }
                                innerTextField()
                            }
                        }
                    )
                    
                    if (trailingIcon != null) {
                        trailingIcon()
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            }
        }
        
        if (isError && errorText != null) {
            Text(
                text = errorText,
                color = TerracottaSeal,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}
