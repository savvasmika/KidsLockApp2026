package com.example.ui.parent

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TabletAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.KidLockApp
import com.example.R
import com.example.model.ChildTheme
import com.example.model.ConnectionState
import com.example.model.KidLockDevice
import com.example.model.LockState
import com.example.model.TemporaryUnlockCode
import com.example.model.UnlockRequest
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RoseDanger
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentDashboardScreen(
    onNavigateSettings: () -> Unit,
    onNavigateThemes: () -> Unit,
    onNavigatePairNew: () -> Unit,
    onSwitchRole: () -> Unit,
    onLanguageClick: () -> Unit,
    currentLanguage: String = "en"
) {
    val app = KidLockApp.instance
    val primaryDevice by app.deviceRepository.primaryDeviceFlow.collectAsState(initial = null)
    val pendingRequests by app.unlockRepository.pendingRequestsFlow.collectAsState(initial = emptyList())
    val activeTheme by app.settingsRepository.activeThemeFlow.collectAsState(initial = ChildTheme.SPACE)

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showTempCodeDialog by remember { mutableStateOf(false) }
    var currentTempCode by remember { mutableStateOf<TemporaryUnlockCode?>(null) }

    // If no paired device exists yet in DB, supply initial sample paired child profile
    LaunchedEffect(primaryDevice) {
        if (primaryDevice == null) {
            val initialChild = KidLockDevice(
                deviceId = "giorgos_tablet_01",
                name = "Giorgos's Tablet",
                deviceType = "Tablet",
                ipAddress = "192.168.1.105",
                port = 8765,
                isPaired = true,
                sharedSecret = "secret_key_abc",
                lockState = LockState.LOCKED,
                batteryPct = 72,
                lastActivityTime = System.currentTimeMillis() - 180000,
                activeThemeId = "space",
                activeLanguage = currentLanguage,
                connectionState = ConnectionState.CONNECTED_LOCAL
            )
            app.deviceRepository.saveDevice(initialChild)
        }
    }

    val childDevice = primaryDevice ?: KidLockDevice(
        deviceId = "giorgos_tablet_01",
        name = "Giorgos's Tablet",
        deviceType = "Tablet",
        lockState = LockState.LOCKED,
        batteryPct = 72,
        connectionState = ConnectionState.CONNECTED_LOCAL
    )

    fun handleUnlock() {
        scope.launch {
            app.deviceRepository.updateLockState(childDevice.deviceId, LockState.UNLOCKED)
            // Attempt local Wi-Fi unlock command if client can reach host
            app.localClient.sendUnlockCommand(
                host = childDevice.ipAddress.ifBlank { "127.0.0.1" },
                port = childDevice.port,
                parentDeviceId = "parent_01",
                secretKey = childDevice.sharedSecret
            )
            snackbarHostState.showSnackbar("Child device unlocked successfully!")
        }
    }

    fun handleLock() {
        scope.launch {
            app.deviceRepository.updateLockState(childDevice.deviceId, LockState.LOCKED)
            app.localClient.sendLockCommand(
                host = childDevice.ipAddress.ifBlank { "127.0.0.1" },
                port = childDevice.port,
                parentDeviceId = "parent_01",
                secretKey = childDevice.sharedSecret
            )
            snackbarHostState.showSnackbar("Child device locked successfully!")
        }
    }

    fun handleGenerateCode() {
        scope.launch {
            val code = app.unlockRepository.generateTemporaryCode(childDevice.deviceId)
            currentTempCode = code
            showTempCodeDialog = true
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.parent_dashboard_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    )
                },
                actions = {
                    IconButton(onClick = onLanguageClick, modifier = Modifier.testTag("btn_parent_lang")) {
                        Icon(imageVector = Icons.Default.Language, contentDescription = "Language")
                    }
                    IconButton(onClick = onSwitchRole, modifier = Modifier.testTag("btn_parent_switch_role")) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = "Switch Role")
                    }
                    IconButton(onClick = onNavigateSettings, modifier = Modifier.testTag("btn_parent_settings")) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
            ) {
                // Pending Unlock Request Banner (if any)
                if (pendingRequests.isNotEmpty()) {
                    val req = pendingRequests.first()
                    PendingRequestCard(
                        request = req,
                        onApprove = {
                            scope.launch {
                                app.unlockRepository.approveRequest(req.requestId)
                                handleUnlock()
                            }
                        },
                        onGenerateCode = {
                            scope.launch {
                                val code = app.unlockRepository.generateTemporaryCode(childDevice.deviceId)
                                app.unlockRepository.approveRequest(req.requestId, code.code)
                                currentTempCode = code
                                showTempCodeDialog = true
                            }
                        },
                        onDeny = {
                            scope.launch {
                                app.unlockRepository.denyRequest(req.requestId)
                                snackbarHostState.showSnackbar("Unlock request denied.")
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                }

                // Child Device Status Card
                ChildDeviceCard(
                    device = childDevice,
                    activeTheme = activeTheme,
                    currentLanguage = currentLanguage
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Primary Quick Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (childDevice.lockState == LockState.LOCKED) {
                        Button(
                            onClick = { handleUnlock() },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .testTag("btn_unlock_child_device"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                        ) {
                            Icon(imageVector = Icons.Default.LockOpen, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(id = R.string.btn_unlock_child),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Button(
                            onClick = { handleLock() },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .testTag("btn_lock_child_device"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoseDanger)
                        ) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(id = R.string.btn_lock_child),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Secondary Action: Send Unlock Code
                OutlinedButton(
                    onClick = { handleGenerateCode() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_send_unlock_code"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.Key, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.btn_send_code),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Additional Management Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateThemes,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("btn_dashboard_themes"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(id = R.string.btn_themes))
                    }

                    OutlinedButton(
                        onClick = onNavigatePairNew,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("btn_dashboard_pair_new"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(id = R.string.btn_pair_new))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Quick Simulation Affordance for Demo Testing
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Test Child Request",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    app.unlockRepository.createUnlockRequest(
                                        childDeviceId = childDevice.deviceId,
                                        childDeviceName = childDevice.name
                                    )
                                }
                            },
                            modifier = Modifier.testTag("btn_trigger_test_request")
                        ) {
                            Text("Simulate Child Unlock Request", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }

    if (showTempCodeDialog && currentTempCode != null) {
        TemporaryCodeDialog(
            code = currentTempCode!!,
            onDismiss = {
                showTempCodeDialog = false
                currentTempCode = null
            }
        )
    }
}

@Composable
private fun ChildDeviceCard(
    device: KidLockDevice,
    activeTheme: ChildTheme,
    currentLanguage: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("child_device_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row: Device Name & Connection Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TabletAndroid,
                            contentDescription = "Tablet",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = device.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = device.deviceType,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Connection Pill
                ConnectionPill(connectionState = device.connectionState)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Lock State Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(id = R.string.lock_status_label),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (device.lockState == LockState.LOCKED)
                            stringResource(id = R.string.status_locked)
                        else
                            stringResource(id = R.string.status_unlocked),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (device.lockState == LockState.LOCKED) RoseDanger else EmeraldSuccess
                    )
                }

                // Battery Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BatteryFull,
                        contentDescription = "Battery",
                        tint = if (device.batteryPct > 20) EmeraldSuccess else RoseDanger,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(id = R.string.battery_label, device.batteryPct),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metadata Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(id = R.string.current_theme_label, "${activeTheme.iconEmoji} ${activeTheme.displayName}"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(id = R.string.current_language_label, if (currentLanguage == "el") "Ελληνικά" else "English"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ConnectionPill(connectionState: ConnectionState) {
    val (label, dotColor) = when (connectionState) {
        ConnectionState.CONNECTED_LOCAL -> "🟢 Wi-Fi" to EmeraldSuccess
        ConnectionState.CONNECTED_REMOTE -> "🔵 Remote" to CyanAccent
        ConnectionState.CONNECTING -> "🟡 Connecting" to AmberWarning
        ConnectionState.OFFLINE -> "🔴 Offline" to RoseDanger
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
}

@Composable
private fun PendingRequestCard(
    request: UnlockRequest,
    onApprove: () -> Unit,
    onGenerateCode: () -> Unit,
    onDeny: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pending_unlock_request_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AmberWarning.copy(alpha = 0.12f)),
        border = BorderStroke(1.5.dp, AmberWarning)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🔔", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.unlock_request_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = AmberWarning
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(id = R.string.unlock_request_message, request.childDeviceName),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(stringResource(id = R.string.btn_unlock_now), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onGenerateCode,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(stringResource(id = R.string.btn_generate_code), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onDeny,
                    modifier = Modifier.weight(0.8f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(stringResource(id = R.string.btn_deny), style = MaterialTheme.typography.labelSmall, color = RoseDanger)
                }
            }
        }
    }
}
