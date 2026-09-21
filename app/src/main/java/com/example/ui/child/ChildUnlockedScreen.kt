package com.example.ui.child

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
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
import com.example.model.LockState
import com.example.ui.parent.ParentPinDialog
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseDanger
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ChildUnlockedScreen(
    childName: String,
    onLockRequested: () -> Unit,
    onThemesClick: () -> Unit,
    onParentSettings: () -> Unit
) {
    val app = KidLockApp.instance
    val scope = rememberCoroutineScope()
    val activeTheme by app.settingsRepository.activeThemeFlow.collectAsState(initial = ChildTheme.SPACE)
    val animationsEnabled by app.settingsRepository.animationsEnabledFlow.collectAsState(initial = true)
    val primaryDevice by app.deviceRepository.primaryDeviceFlow.collectAsState(initial = null)

    var sessionSeconds by remember { mutableLongStateOf(0L) }
    var showParentPinDialog by remember { mutableStateOf(false) }

    // If parent remotely locks tablet, return to lock screen
    LaunchedEffect(primaryDevice?.lockState) {
        if (primaryDevice?.lockState == LockState.LOCKED) {
            onLockRequested()
        }
    }

    // Active session timer ticker
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            sessionSeconds++
        }
    }

    val minutes = sessionSeconds / 60
    val seconds = sessionSeconds % 60
    val timeFormatted = "%02d:%02d".format(minutes, seconds)

    val infiniteTransition = rememberInfiniteTransition(label = "celebrate")
    val badgeScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badge"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(activeTheme.backgroundColor)
    ) {
        if (animationsEnabled) {
            AnimatedStarfield(theme = activeTheme)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Celebration Badge
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .scale(if (animationsEnabled) badgeScale else 1f)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(EmeraldSuccess, activeTheme.primaryColor)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🎉", fontSize = 52.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(id = R.string.tablet_unlocked_celebration),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    ),
                    color = activeTheme.accentColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(id = R.string.child_unlocked_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Active Session Time Counter Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = activeTheme.surfaceColor)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Timer",
                            tint = activeTheme.accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Session Time: $timeFormatted",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Take a break now (relock) button
                Button(
                    onClick = {
                        scope.launch {
                            primaryDevice?.deviceId?.let {
                                app.deviceRepository.updateLockState(it, LockState.LOCKED)
                            }
                        }
                        onLockRequested()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("btn_take_break_now"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoseDanger)
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.btn_lock_now_child),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Change Theme Button
                OutlinedButton(
                    onClick = onThemesClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_unlocked_change_theme"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(imageVector = Icons.Default.Palette, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(id = R.string.btn_change_theme))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Parental Controls Link (PIN Protected)
            OutlinedButton(
                onClick = { showParentPinDialog = true },
                modifier = Modifier.testTag("btn_unlocked_parent_controls"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = Color.White.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(id = R.string.btn_child_settings),
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }

    if (showParentPinDialog) {
        ParentPinDialog(
            title = "Parent Controls",
            subtitle = "Enter Parent PIN to manage KIDLOCK settings:",
            onPinSuccess = {
                showParentPinDialog = false
                onParentSettings()
            },
            onDismiss = { showParentPinDialog = false }
        )
    }
}
