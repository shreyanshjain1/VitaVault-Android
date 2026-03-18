package com.vitavault.mobile.data

import android.content.Context
import android.provider.Settings
import com.vitavault.mobile.BuildConfig
import com.vitavault.mobile.health.HealthConnectManager
import com.vitavault.mobile.network.ConnectionDto
import com.vitavault.mobile.network.DeviceReadingsRequest
import com.vitavault.mobile.network.LoginRequest
import com.vitavault.mobile.network.MobileUser
import com.vitavault.mobile.network.NetworkFactory
import kotlinx.coroutines.flow.first
import java.util.UUID

class MobileRepository(
    context: Context,
    private val sessionStore: SessionStore,
    private val healthManager: HealthConnectManager,
) {
    private val applicationContext = context.applicationContext

    private suspend fun api() = NetworkFactory.create(currentBaseUrl())

    suspend fun currentBaseUrl(): String {
        return sessionStore.baseUrlFlow.first()?.ifBlank { null } ?: BuildConfig.API_BASE_URL
    }

    suspend fun login(email: String, password: String): MobileUser {
        val normalizedEmail = email.trim().lowercase()
        val response = api().login(LoginRequest(normalizedEmail, password))
        sessionStore.saveToken(response.token)
        sessionStore.saveLastEmail(normalizedEmail)
        return response.user
    }

    suspend fun logout() {
        val token = sessionStore.tokenFlow.first() ?: return
        runCatching { api().logout("Bearer $token") }
        sessionStore.saveToken(null)
    }

    suspend fun currentUser(): MobileUser? {
        val token = sessionStore.tokenFlow.first() ?: return null
        return runCatching { api().me("Bearer $token").user }.getOrNull()
    }

    suspend fun connections(): List<ConnectionDto> {
        val token = sessionStore.tokenFlow.first() ?: return emptyList()
        return api().connections("Bearer $token").connections
    }

    suspend fun saveBaseUrl(value: String) {
        sessionStore.saveBaseUrl(value)
    }

    suspend fun lastEmail(): String? = sessionStore.emailFlow.first()
    suspend fun lastSyncSummary(): String? = sessionStore.lastSyncSummaryFlow.first()

    suspend fun syncLastDays(days: Long): String {
        val token = sessionStore.tokenFlow.first() ?: throw IllegalStateException("Not logged in.")
        val readings = healthManager.readLastDays(days)
        if (readings.isEmpty()) {
            val msg = "No Health Connect readings found in the selected range."
            sessionStore.saveLastSyncSummary(msg)
            return msg
        }

        val androidId = Settings.Secure.getString(
            applicationContext.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: UUID.randomUUID().toString()

        val request = DeviceReadingsRequest(
            clientDeviceId = androidId,
            deviceLabel = healthManager.deviceLabel(),
            appVersion = healthManager.appVersion(),
            scopes = healthManager.permissions.toList().sorted(),
            syncMetadata = mapOf(
                "windowDays" to days,
                "readingCount" to readings.size,
                "providerPackage" to HealthConnectManager.PROVIDER_PACKAGE_NAME,
            ),
            readings = readings,
        )

        val response = api().syncReadings("Bearer $token", request)
        val summary = "Sync complete. Accepted: ${response.sync.acceptedCount}, mirrored to vitals: ${response.sync.mirroredCount}."
        sessionStore.saveLastSyncSummary(summary)
        return summary
    }

    fun healthConnectStatus(): Int = healthManager.sdkStatus()
    suspend fun hasAllHealthPermissions(): Boolean = healthManager.hasAllPermissions()
    suspend fun grantedPermissionCount(): Int = healthManager.grantedPermissionCount()
    fun requiredPermissionCount(): Int = healthManager.permissions.size
}
