package com.vitavault.mobile.network

data class LoginRequest(
    val email: String,
    val password: String,
    val deviceName: String = "Android Health Connect"
)

data class LoginResponse(
    val token: String,
    val expiresAt: String,
    val user: MobileUser
)

data class MobileUser(
    val id: String,
    val email: String,
    val name: String?
)

data class MeResponse(
    val user: MobileUser
)

data class ConnectionDto(
    val id: String,
    val source: String,
    val platform: String,
    val clientDeviceId: String,
    val deviceLabel: String?,
    val appVersion: String?,
    val status: String,
    val lastSyncedAt: String?,
    val lastError: String?
)

data class ConnectionsResponse(
    val connections: List<ConnectionDto>
)

data class DeviceReadingPayload(
    val readingType: String,
    val capturedAt: String,
    val clientReadingId: String? = null,
    val unit: String? = null,
    val valueInt: Int? = null,
    val valueFloat: Double? = null,
    val systolic: Int? = null,
    val diastolic: Int? = null,
    val metadata: Map<String, Any?>? = null,
    val rawPayload: Map<String, Any?>? = null,
)

data class DeviceReadingsRequest(
    val source: String = "ANDROID_HEALTH_CONNECT",
    val platform: String = "ANDROID",
    val clientDeviceId: String,
    val deviceLabel: String,
    val appVersion: String,
    val scopes: List<String>,
    val syncMetadata: Map<String, Any?>,
    val readings: List<DeviceReadingPayload>,
)

data class SyncResultDto(
    val syncJobId: String,
    val acceptedCount: Int,
    val mirroredCount: Int,
)

data class DeviceReadingsResponse(
    val success: Boolean,
    val sync: SyncResultDto,
)
