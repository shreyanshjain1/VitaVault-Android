package com.vitavault.mobile.ui

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitavault.mobile.data.MobileRepository
import com.vitavault.mobile.data.SessionStore
import com.vitavault.mobile.health.HealthConnectManager
import kotlinx.coroutines.launch

class VitaVaultViewModel(
    private val repository: MobileRepository,
) : ViewModel() {
    var state = mutableStateOf(VitaVaultUiState())
        private set

    fun bootstrap() {
        viewModelScope.launch {
            val currentBaseUrl = repository.currentBaseUrl()
            val lastEmail = repository.lastEmail().orEmpty()
            val user = repository.currentUser()
            val permissions = runCatching { repository.hasAllHealthPermissions() }.getOrDefault(false)
            val grantedPermissionCount = runCatching { repository.grantedPermissionCount() }.getOrDefault(0)
            val requiredPermissionCount = repository.requiredPermissionCount()
            val connections = if (user != null) runCatching { repository.connections() }.getOrDefault(emptyList()) else emptyList()
            val lastSyncSummary = repository.lastSyncSummary()
            state.value = state.value.copy(
                baseUrl = currentBaseUrl,
                email = lastEmail,
                user = user,
                healthConnectStatus = repository.healthConnectStatus(),
                permissionsGranted = permissions,
                grantedPermissionCount = grantedPermissionCount,
                requiredPermissionCount = requiredPermissionCount,
                connections = connections,
                lastSyncSummary = lastSyncSummary,
                message = null,
                loading = false,
            )
        }
    }

    fun updateEmail(value: String) { state.value = state.value.copy(email = value) }
    fun updatePassword(value: String) { state.value = state.value.copy(password = value) }
    fun updateBaseUrl(value: String) { state.value = state.value.copy(baseUrl = value) }

    fun saveBaseUrl() {
        viewModelScope.launch {
            repository.saveBaseUrl(state.value.baseUrl)
            state.value = state.value.copy(message = "API base URL saved.")
        }
    }

    fun login() {
        viewModelScope.launch {
            state.value = state.value.copy(loading = true, message = null)
            runCatching {
                repository.login(state.value.email, state.value.password)
            }.onSuccess { user ->
                val permissions = runCatching { repository.hasAllHealthPermissions() }.getOrDefault(false)
                val grantedPermissionCount = runCatching { repository.grantedPermissionCount() }.getOrDefault(0)
                val connections = runCatching { repository.connections() }.getOrDefault(emptyList())
                val lastSyncSummary = repository.lastSyncSummary()
                state.value = state.value.copy(
                    user = user,
                    password = "",
                    permissionsGranted = permissions,
                    grantedPermissionCount = grantedPermissionCount,
                    requiredPermissionCount = repository.requiredPermissionCount(),
                    connections = connections,
                    lastSyncSummary = lastSyncSummary,
                    healthConnectStatus = repository.healthConnectStatus(),
                    loading = false,
                    message = "Logged in successfully.",
                )
            }.onFailure { error ->
                state.value = state.value.copy(
                    loading = false,
                    message = error.message ?: "Login failed.",
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            state.value = state.value.copy(
                user = null,
                password = "",
                connections = emptyList(),
                message = "Logged out.",
            )
        }
    }

    fun refreshHealthConnectStatus() {
        viewModelScope.launch {
            val permissions = runCatching { repository.hasAllHealthPermissions() }.getOrDefault(false)
            val grantedPermissionCount = runCatching { repository.grantedPermissionCount() }.getOrDefault(0)
            state.value = state.value.copy(
                healthConnectStatus = repository.healthConnectStatus(),
                permissionsGranted = permissions,
                grantedPermissionCount = grantedPermissionCount,
                requiredPermissionCount = repository.requiredPermissionCount(),
                message = "Health Connect status refreshed.",
            )
        }
    }

    fun refreshConnections() {
        viewModelScope.launch {
            if (state.value.user == null) return@launch
            val connections = runCatching { repository.connections() }.getOrDefault(emptyList())
            state.value = state.value.copy(
                connections = connections,
                message = "Connection list refreshed.",
            )
        }
    }

    fun onPermissionsResult(granted: Set<String>) {
        state.value = state.value.copy(
            permissionsGranted = granted.isNotEmpty(),
            grantedPermissionCount = granted.size,
            requiredPermissionCount = repository.requiredPermissionCount(),
            message = if (granted.isNotEmpty()) "Permissions updated." else "No permissions granted.",
        )
    }

    fun syncLastDays(days: Long) {
        viewModelScope.launch {
            state.value = state.value.copy(loading = true, message = null)
            runCatching {
                repository.syncLastDays(days)
            }.onSuccess { message ->
                val connections = runCatching { repository.connections() }.getOrDefault(emptyList())
                state.value = state.value.copy(
                    loading = false,
                    connections = connections,
                    lastSyncSummary = message,
                    message = message,
                )
            }.onFailure { error ->
                state.value = state.value.copy(
                    loading = false,
                    message = error.message ?: "Sync failed.",
                )
            }
        }
    }
}

class VitaVaultViewModelFactory(
    context: Context,
    healthManager: HealthConnectManager,
) : ViewModelProvider.Factory {
    private val sessionStore = SessionStore(context)
    private val repository = MobileRepository(context, sessionStore, healthManager)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VitaVaultViewModel(repository) as T
    }
}
