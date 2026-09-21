package com.example.ui.child

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
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
import com.example.model.ConnectionState
import com.example.model.LockState
import com.example.ui.parent.ParentPinDialog
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseDanger
import kotlinx.coroutines.launch

@Composable
fun ChildLockScreen(
    childName: String,
    onUnlocked: () -> Unit,
    onLanguageClick: () -> Unit,
    onThemesClick: () -> Unit,
    onSwitchRole: () -> Unit,
    currentLanguage: String = "en"
) {
    val app = KidLockApp.instance
    val scope = rememberCoroutineScope()
    val activeTheme by app.settingsRepository.activeThemeFlow.collectAsState(initial = ChildTheme.SPACE)
    val animationsEnabled by app.settingsRepository.animationsEnabledFlow.collectAsState(initial = true)
    val primaryDevice by app.deviceRepository.primaryDeviceFlow.collectAsState(initial = null)

    var showCodeKeypad by remember { mutableStateOf(false) }
    var showParentPinDialog by remember { mutableStateOf(false) }
    var requestSent by remember { mutableStateOf(false) }

    // Live observation: If parent unlocks tablet, trigger onUnlocked callback!
    LaunchedEffect(primaryDevice?.lockState) {
        if (primaryDevice?.lockState == LockState.UNLOCKED) {
            onUnlocked()
        }
    }

    // Floating Mascot Animation
    val infiniteTransition = rememberInfiniteTransition(label = "mascot")
    val mascotFloat by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mascot_y"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(activeTheme.backgroundColor)
    ) {
        // Dynamic Animated Starfield / Floating Particles Canvas
        if (animationsEnabled) {
            AnimatedStarfield(theme = activeTheme)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Connection Status & Theme/Language shortcuts
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Connection badge
                ConnectionStatusPill(
                    connectionState = primaryDevice?.connectionState ?: ConnectionState.CONNECTED_LOCAL
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onThemesClick, modifier = Modifier.testTag("btn_child_theme_picker")) {
                        Text(text = activeTheme.iconEmoji, fontSize = 22.sp)
                    }
                    IconButton(onClick = onLanguageClick, modifier = Modifier.testTag("btn_child_lang")) {
                        Icon(imageVector = Icons.Default.Language, contentDescription = "Language", tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Playful Break Reminder Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp)
                    .testTag("child_break_card"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = activeTheme.surfaceColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Bobbing Mascot & Icon
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .offset(y = if (animationsEnabled) mascotFloat.dp else 0.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(activeTheme.primaryColor, activeTheme.secondaryColor)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = activeTheme.mascotEmoji, fontSize = 54.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = stringResource(id = R.string.child_break_title),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp
                        ),
                        color = activeTheme.accentColor,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(id = R.string.child_break_subtitle),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = stringResource(id = R.string.child_break_prompt),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center
                    )

                    if (requestSent) {
                        Spacer(modifier = Modifier.height(18.dp))
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = AmberWarning.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.HourglassTop, contentDescription = null, tint = AmberWarning)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(id = R.string.request_sent_waiting),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Action 1: Ask Parent For Code
                    Button(
                        onClick = {
                            requestSent = true
                            scope.launch {
                                app.unlockRepository.createUnlockRequest(
                                    childDeviceId = primaryDevice?.deviceId ?: "child_dev",
                                    childDeviceName = "$childName's Tablet"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("btn_ask_parent_for_code"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = activeTheme.primaryColor)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = R.string.btn_ask_parent),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action 2: Enter Unlock Code
                    OutlinedButton(
                        onClick = { showCodeKeypad = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("btn_enter_unlock_code"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Icon(imageVector = Icons.Default.Key, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = R.string.btn_enter_code),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Emergency Parent PIN Bypass at bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onSwitchRole,
                    modifier = Modifier.testTag("btn_child_switch_role")
                ) {
                    Text("Switch Role", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall)
                }

                TextButton(
                    onClick = { showParentPinDialog = true },
                    modifier = Modifier.testTag("btn_parent_override_pin")
                ) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Parent PIN Bypass", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }

    // 6-digit Code Entry Dialog
    if (showCodeKeypad) {
        ChildCodeEntryDialog(
            theme = activeTheme,
            onCodeAccepted = {
                showCodeKeypad = false
                scope.launch {
                    primaryDevice?.deviceId?.let {
                        app.deviceRepository.updateLockState(it, LockState.UNLOCKED)
                    }
                }
                onUnlocked()
            },
            onDismiss = { showCodeKeypad = false }
        )
    }

    // Parent PIN bypass dialog
    if (showParentPinDialog) {
        ParentPinDialog(
            title = "Parent Unlock Bypass",
            subtitle = "Enter your Parent PIN to immediately unlock this tablet:",
            onPinSuccess = {
                showParentPinDialog = false
                scope.launch {
                    primaryDevice?.deviceId?.let {
                        app.deviceRepository.updateLockState(it, LockState.UNLOCKED)
                    }
                }
                onUnlocked()
            },
            onDismiss = { showParentPinDialog = false }
        )
    }
}

@Composable
private fun ConnectionStatusPill(connectionState: ConnectionState) {
    val (textRes, color) = when (connectionState) {
        ConnectionState.CONNECTED_LOCAL -> R.string.child_connection_local to EmeraldSuccess
        ConnectionState.CONNECTED_REMOTE -> R.string.child_connection_remote to EmeraldSuccess
        else -> R.string.child_connection_offline to RoseDanger
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(id = textRes),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
        }
    }
}

@Composable
fun AnimatedStarfield(theme: ChildTheme) {
    val transition = rememberInfiniteTransition(label = "star_twinkle")
    val alphaAnim by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "twinkle"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Deterministic stars scattered across screen
        val starCoords = listOf(
            0.15f to 0.12f, 0.82f to 0.08f, 0.45f to 0.18f,
            0.10f to 0.42f, 0.88f to 0.48f, 0.25f to 0.65f,
            0.75f to 0.72f, 0.12f to 0.88f, 0.65f to 0.92f,
            0.52f to 0.38f, 0.35f to 0.82f, 0.90f to 0.25f
        )

        for ((idx, coord) in starCoords.withIndex()) {
            val starX = coord.first * w
            val starY = coord.second * h
            val starAlpha = if (idx % 2 == 0) alphaAnim else (1f - alphaAnim + 0.15f).coerceIn(0.2f, 0.9f)
            drawCircle(
                color = theme.accentColor.copy(alpha = starAlpha),
                radius = if (idx % 3 == 0) 3.5.dp.toPx() else 2.dp.toPx(),
                center = Offset(starX, starY)
            )
        }
    }
}
