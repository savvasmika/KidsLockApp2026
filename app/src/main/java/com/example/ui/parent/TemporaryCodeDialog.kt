package com.example.ui.parent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.TemporaryUnlockCode
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RoseDanger
import kotlinx.coroutines.delay

@Composable
fun TemporaryCodeDialog(
    code: TemporaryUnlockCode,
    onDismiss: () -> Unit
) {
    var timeLeftMs by remember {
        mutableLongStateOf(maxOf(0L, code.expiresAt - System.currentTimeMillis()))
    }

    LaunchedEffect(code) {
        while (timeLeftMs > 0) {
            delay(1000L)
            timeLeftMs = maxOf(0L, code.expiresAt - System.currentTimeMillis())
        }
    }

    val totalDurationMs = 120_000f
    val progress = (timeLeftMs / totalDurationMs).coerceIn(0f, 1f)
    val secondsLeft = (timeLeftMs / 1000) % 60
    val minutesLeft = (timeLeftMs / 1000) / 60
    val formattedTime = "%02d:%02d".format(minutesLeft, secondsLeft)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.temp_code_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.temp_code_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Large 6-digit Code Display
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = code.code,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 36.sp,
                            letterSpacing = 10.sp
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .padding(horizontal = 24.dp, vertical = 14.dp)
                            .testTag("temp_unlock_code_display")
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Timer with progress ring
                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(80.dp),
                        strokeWidth = 6.dp,
                        color = if (progress > 0.25f) IndigoPrimary else RoseDanger,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (progress > 0.25f) MaterialTheme.colorScheme.onSurface else RoseDanger
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (timeLeftMs > 0)
                        stringResource(id = R.string.temp_code_expires_in, formattedTime)
                    else "Code Expired",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (timeLeftMs > 0) MaterialTheme.colorScheme.onSurfaceVariant else RoseDanger
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_close_temp_code")
            ) {
                Text(stringResource(id = R.string.btn_close))
            }
        }
    )
}
