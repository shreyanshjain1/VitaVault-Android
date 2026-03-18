package com.vitavault.mobile.ui

import com.vitavault.mobile.network.ConnectionDto
import com.vitavault.mobile.network.MobileUser

data class VitaVaultUiState(
    val loading: Boolean = false,
    val email: String = "",
    val password: String = "",
    val baseUrl: String = "",
    val user: MobileUser? = null,
    val healthConnectStatus: Int? = null,
    val permissionsGranted: Boolean = false,
    val grantedPermissionCount: Int = 0,
    val requiredPermissionCount: Int = 0,
    val connections: List<ConnectionDto> = emptyList(),
    val lastSyncSummary: String? = null,
    val message: String? = null,
)
