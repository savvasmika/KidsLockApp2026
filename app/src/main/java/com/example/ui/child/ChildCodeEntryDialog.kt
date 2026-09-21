package com.example.ui.child

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.KidLockApp
import com.example.R
import com.example.model.ChildTheme
import com.example.ui.onboarding.NumericKeypad
import com.example.ui.theme.RoseDanger
import kotlinx.coroutines.launch

@Composable
fun ChildCodeEntryDialog(
    theme: ChildTheme,
    onCodeAccepted: () -> Unit,
    onDismiss: () -> Unit
) {
    val app = KidLockApp.instance
    val scope = rememberCoroutineScope()
    var enteredCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun handleDigit(d: String) {
        if (enteredCode.length < 6) {
            val newCode = enteredCode + d
            enteredCode = newCode
            errorMessage = null

            if (newCode.length == 6) {
                scope.launch {
                    val valid = app.unlockRepository.verifyAndConsumeCode(newCode)
                    if (valid) {
                        onCodeAccepted()
                    } else {
                        errorMessage = "Invalid or expired unlock code."
                        enteredCode = ""
                    }
                }
            }
        }
    }

    fun handleBackspace() {
        if (enteredCode.isNotEmpty()) {
            enteredCode = enteredCode.dropLast(1)
            errorMessage = null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.enter_temp_code_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 360.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Type the 6-digit code your parent gave you:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 6 Boxes / Digits
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 6) {
                        val digitChar = enteredCode.getOrNull(i)?.toString() ?: ""
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (digitChar.isNotEmpty())
                                    theme.primaryColor.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.5.dp,
                                color = if (digitChar.isNotEmpty()) theme.primaryColor else Color.Transparent
                            ),
                            modifier = Modifier.size(width = 38.dp, height = 48.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = digitChar,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(top = 10.dp)
                                )
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage!!,
                        color = RoseDanger,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                NumericKeypad(
                    onDigit = { handleDigit(it) },
                    onBackspace = { handleBackspace() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = { },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("btn_close_keypad")) {
                Text(stringResource(id = R.string.btn_cancel))
            }
        }
    )
}
