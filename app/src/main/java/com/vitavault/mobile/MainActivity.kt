package com.vitavault.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import com.vitavault.mobile.health.HealthConnectManager
import com.vitavault.mobile.ui.VitaVaultApp
import com.vitavault.mobile.ui.VitaVaultViewModel
import com.vitavault.mobile.ui.VitaVaultViewModelFactory
import com.vitavault.mobile.ui.theme.VitaVaultTheme

class MainActivity : ComponentActivity() {
    private val healthManager by lazy { HealthConnectManager(this) }
    private val viewModel: VitaVaultViewModel by viewModels {
        VitaVaultViewModelFactory(applicationContext, healthManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val permissionContract: ActivityResultContract<Set<String>, Set<String>> =
            healthManager.requestPermissionsActivityContract()

        val permissionLauncher = registerForActivityResult(permissionContract) { granted ->
            viewModel.onPermissionsResult(granted)
        }

        setContent {
            VitaVaultTheme {
                LaunchedEffect(Unit) {
                    viewModel.bootstrap()
                }
                VitaVaultApp(
                    state = viewModel.state,
                    onEmailChanged = viewModel::updateEmail,
                    onPasswordChanged = viewModel::updatePassword,
                    onBaseUrlChanged = viewModel::updateBaseUrl,
                    onSaveBaseUrl = viewModel::saveBaseUrl,
                    onLogin = viewModel::login,
                    onLogout = viewModel::logout,
                    onCheckHealthConnect = viewModel::refreshHealthConnectStatus,
                    onRequestPermissions = {
                        permissionLauncher.launch(healthManager.permissions)
                    },
                    onSyncLast7Days = { viewModel.syncLastDays(7) },
                    onSyncLast24Hours = { viewModel.syncLastDays(1) },
                    onRefreshConnections = viewModel::refreshConnections,
                )
            }
        }
    }
}
