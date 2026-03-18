package com.vitavault.mobile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient
import com.vitavault.mobile.network.ConnectionDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VitaVaultApp(
    state: VitaVaultUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onBaseUrlChanged: (String) -> Unit,
    onSaveBaseUrl: () -> Unit,
    onLogin: () -> Unit,
    onLogout: () -> Unit,
    onCheckHealthConnect: () -> Unit,
    onRequestPermissions: () -> Unit,
    onSyncLast7Days: () -> Unit,
    onSyncLast24Hours: () -> Unit,
    onRefreshConnections: () -> Unit,
) {
    val snackbars = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let { snackbars.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("VitaVault Mobile", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Android Health Connect sync companion",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbars) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 12.dp,
                bottom = 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { HeroCard(state) }
            item { BaseUrlCard(state.baseUrl, onBaseUrlChanged, onSaveBaseUrl) }

            if (state.user == null) {
                item {
                    LoginCard(
                        email = state.email,
                        password = state.password,
                        loading = state.loading,
                        onEmailChanged = onEmailChanged,
                        onPasswordChanged = onPasswordChanged,
                        onLogin = onLogin,
                    )
                }
            } else {
                item { SessionCard(state.user.email, onLogout) }
                item {
                    HealthConnectCard(
                        status = state.healthConnectStatus,
                        permissionsGranted = state.permissionsGranted,
                        grantedPermissionCount = state.grantedPermissionCount,
                        requiredPermissionCount = state.requiredPermissionCount,
                        onCheckHealthConnect = onCheckHealthConnect,
                        onRequestPermissions = onRequestPermissions,
                    )
                }
                item {
                    SyncCard(
                        loading = state.loading,
                        lastSyncSummary = state.lastSyncSummary,
                        onSyncLast24Hours = onSyncLast24Hours,
                        onSyncLast7Days = onSyncLast7Days,
                    )
                }
                item {
                    ConnectionsCard(
                        connections = state.connections,
                        onRefreshConnections = onRefreshConnections,
                    )
                }
                item { NotesCard() }
            }
        }
    }
}

@Composable
private fun HeroCard(state: VitaVaultUiState) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Outlined.PhoneAndroid, contentDescription = null)
                Text("Android sync companion", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            }
            Text(
                "This app signs into your VitaVault backend, checks Health Connect availability, requests permissions, and uploads selected readings into the mobile ingestion API.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text(if (state.user != null) "Signed in" else "Signed out") })
                AssistChip(onClick = {}, label = { Text(if (state.permissionsGranted) "Permissions granted" else "Permissions needed") })
            }
        }
    }
}

@Composable
private fun BaseUrlCard(baseUrl: String, onBaseUrlChanged: (String) -> Unit, onSaveBaseUrl: () -> Unit) {
    Card {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Link, contentDescription = null)
                Text("Backend connection", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            Text(
                "For emulator use http://10.0.2.2:3000/. For a real phone on the same Wi‑Fi, use your computer's LAN IP, for example http://192.168.1.10:3000/.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = baseUrl,
                onValueChange = onBaseUrlChanged,
                label = { Text("API base URL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Button(onClick = onSaveBaseUrl) { Text("Save base URL") }
        }
    }
}

@Composable
private fun LoginCard(
    email: String,
    password: String,
    loading: Boolean,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLogin: () -> Unit,
) {
    Card {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Lock, contentDescription = null)
                Text("Login", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            Text(
                "Use the same account credentials as your VitaVault web app.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(value = email, onValueChange = onEmailChanged, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = password, onValueChange = onPasswordChanged, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Button(onClick = onLogin, enabled = !loading, modifier = Modifier.fillMaxWidth()) {
                if (loading) CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.height(18.dp)) else Text("Login to VitaVault")
            }
        }
    }
}

@Composable
private fun SessionCard(email: String, onLogout: () -> Unit) {
    Card {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Session", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(email, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = onLogout) {
                Icon(Icons.Outlined.Logout, contentDescription = null)
                Spacer(Modifier.padding(4.dp))
                Text("Logout")
            }
        }
    }
}

@Composable
private fun HealthConnectCard(
    status: Int?,
    permissionsGranted: Boolean,
    grantedPermissionCount: Int,
    requiredPermissionCount: Int,
    onCheckHealthConnect: () -> Unit,
    onRequestPermissions: () -> Unit,
) {
    val statusLabel = when (status) {
        HealthConnectClient.SDK_AVAILABLE -> "Available"
        HealthConnectClient.SDK_UNAVAILABLE -> "Unavailable on this device"
        HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> "Provider update/install required"
        else -> "Unknown"
    }
    Card {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.HealthAndSafety, contentDescription = null)
                Text("Health Connect", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            Text("Status: $statusLabel")
            Text("Permissions: ${if (permissionsGranted) "Granted" else "Not granted"}")
            Text(
                "Granted: $grantedPermissionCount / $requiredPermissionCount",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onCheckHealthConnect) { Text("Refresh status") }
                Button(onClick = onRequestPermissions) { Text("Request permissions") }
            }
        }
    }
}

@Composable
private fun SyncCard(
    loading: Boolean,
    lastSyncSummary: String?,
    onSyncLast24Hours: () -> Unit,
    onSyncLast7Days: () -> Unit,
) {
    Card {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.CloudSync, contentDescription = null)
                Text("Sync to VitaVault", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            Text(
                "Reads steps, heart rate, weight, blood pressure, and oxygen saturation from Health Connect and posts them to /api/mobile/device-readings.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!lastSyncSummary.isNullOrBlank()) {
                Divider()
                Text(lastSyncSummary, style = MaterialTheme.typography.bodySmall)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onSyncLast24Hours, enabled = !loading) { Text("Sync 24h") }
                Button(onClick = onSyncLast7Days, enabled = !loading) { Text("Sync 7 days") }
            }
        }
    }
}

@Composable
private fun ConnectionsCard(connections: List<ConnectionDto>, onRefreshConnections: () -> Unit) {
    Card {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Backend device connections", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = onRefreshConnections) {
                    Icon(Icons.Outlined.Refresh, contentDescription = null)
                    Spacer(Modifier.padding(3.dp))
                    Text("Refresh")
                }
            }
            if (connections.isEmpty()) {
                Text("No device connections returned by the backend yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                connections.forEach { connection ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))) {
                        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(connection.deviceLabel ?: connection.clientDeviceId, fontWeight = FontWeight.SemiBold)
                            Text("${connection.source} • ${connection.platform} • ${connection.status}", style = MaterialTheme.typography.bodySmall)
                            Text("Last sync: ${connection.lastSyncedAt ?: "—"}", style = MaterialTheme.typography.bodySmall)
                            connection.lastError?.takeIf { it.isNotBlank() }?.let {
                                Text("Last error: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotesCard() {
    Card {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Notes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "This companion app is meant to work alongside the VitaVault web platform. It focuses on Android Health Connect data ingestion first, while the main web app remains the source of truth for records, collaboration, AI insights, and exports.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
