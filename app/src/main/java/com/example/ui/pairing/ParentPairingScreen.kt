package com.example.ui.pairing

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TabletAndroid
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.KidLockApp
import com.example.R
import com.example.model.ConnectionState
import com.example.model.KidLockDevice
import com.example.model.LockState
import com.example.network.NsdHelper
import com.example.security.CryptoUtils
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentPairingScreen(
    onPairingSuccess: (KidLockDevice) -> Unit,
    onBack: () -> Unit
) {
    val app = KidLockApp.instance
    val discoveredDevices by app.nsdHelper.discoveredServices.collectAsState()
    val isScanning by app.nsdHelper.isScanning.collectAsState()

    var selectedDevice by remember { mutableStateOf<NsdHelper.DiscoveredDevice?>(null) }
    var confirmationCode by remember { mutableStateOf<String?>(null) }
    var isPairingLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        app.nsdHelper.startDiscovery()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.nearby_devices_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_pairing")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { app.nsdHelper.startDiscovery() },
                        modifier = Modifier.testTag("btn_rescan")
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Scan Again")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Radar Animation Section
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                RadarAnimation(isScanning = isScanning)
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = "Wi-Fi",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Text(
                text = if (isScanning) stringResource(id = R.string.searching_devices)
                else "Wi-Fi scan completed",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(id = R.string.searching_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Discovered Devices List or Quick Simulated Device Button (for single-device testing)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (discoveredDevices.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stringResource(id = R.string.no_devices_found),
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Make sure the child tablet is on the same Wi-Fi and open on the Waiting screen.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                // Local quick-pair affordance for development / emulator testing
                                OutlinedButton(
                                    onClick = {
                                        val testDevice = NsdHelper.DiscoveredDevice(
                                            serviceName = "Giorgos's Tablet",
                                            host = null,
                                            port = 8765,
                                            deviceId = "kl_giorgos_tab"
                                        )
                                        selectedDevice = testDevice
                                        confirmationCode = CryptoUtils.generateSecure6DigitCode()
                                    },
                                    modifier = Modifier.testTag("btn_pair_demo_device")
                                ) {
                                    Text("Pair Detected Local Tablet (Demo)")
                                }
                            }
                        }
                    }
                }

                items(discoveredDevices) { device ->
                    DiscoveredDeviceItem(
                        device = device,
                        onPairClick = {
                            selectedDevice = device
                            confirmationCode = CryptoUtils.generateSecure6DigitCode()
                        }
                    )
                }
            }
        }
    }

    // 6-digit confirmation dialog
    if (selectedDevice != null && confirmationCode != null) {
        AlertDialog(
            onDismissRequest = {
                if (!isPairingLoading) {
                    selectedDevice = null
                    confirmationCode = null
                }
            },
            title = {
                Text(
                    text = stringResource(id = R.string.confirm_pairing_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.confirm_pairing_parent_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // 6-digit styled code display
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Text(
                            text = confirmationCode!!,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 8.sp
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                                .testTag("pairing_code_display")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Device: ${selectedDevice?.serviceName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (isPairingLoading) {
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isPairingLoading = true
                        val newDevice = KidLockDevice(
                            deviceId = selectedDevice!!.deviceId.ifBlank { "child_${System.currentTimeMillis()}" },
                            name = selectedDevice!!.serviceName,
                            deviceType = "Tablet",
                            ipAddress = selectedDevice!!.host?.hostAddress ?: "127.0.0.1",
                            port = selectedDevice!!.port,
                            isPaired = true,
                            sharedSecret = CryptoUtils.generateSecureToken(16),
                            lockState = LockState.LOCKED,
                            batteryPct = 72,
                            lastActivityTime = System.currentTimeMillis(),
                            connectionState = ConnectionState.CONNECTED_LOCAL
                        )
                        onPairingSuccess(newDevice)
                    },
                    modifier = Modifier.testTag("btn_confirm_pairing_parent")
                ) {
                    Text("Confirm & Pair")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        selectedDevice = null
                        confirmationCode = null
                    }
                ) {
                    Text(stringResource(id = R.string.btn_cancel))
                }
            }
        )
    }
}

@Composable
private fun DiscoveredDeviceItem(
    device: NsdHelper.DiscoveredDevice,
    onPairClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TabletAndroid,
                        contentDescription = "Tablet",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = device.serviceName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(EmeraldSuccess)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(id = R.string.same_wifi_network),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Button(
                onClick = onPairClick,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("btn_pair_${device.serviceName}")
            ) {
                Text(
                    text = stringResource(id = R.string.btn_pair),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun RadarAnimation(isScanning: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val wave1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave1"
    )
    val wave2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, delayMillis = 700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave2"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = center
        val maxRadius = size.minDimension / 2

        if (isScanning) {
            drawCircle(
                color = IndigoPrimary.copy(alpha = (1f - wave1) * 0.45f),
                radius = maxRadius * wave1,
                center = center,
                style = Stroke(width = 3.dp.toPx())
            )
            drawCircle(
                color = IndigoPrimary.copy(alpha = (1f - wave2) * 0.45f),
                radius = maxRadius * wave2,
                center = center,
                style = Stroke(width = 3.dp.toPx())
            )
        }
    }
}
