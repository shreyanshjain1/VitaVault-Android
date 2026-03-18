package com.vitavault.mobile.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface VitaVaultApi {
    @POST("api/mobile/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/mobile/auth/logout")
    suspend fun logout(@Header("Authorization") bearer: String)

    @GET("api/mobile/me")
    suspend fun me(@Header("Authorization") bearer: String): MeResponse

    @GET("api/mobile/connections")
    suspend fun connections(@Header("Authorization") bearer: String): ConnectionsResponse

    @POST("api/mobile/device-readings")
    suspend fun syncReadings(
        @Header("Authorization") bearer: String,
        @Body request: DeviceReadingsRequest,
    ): DeviceReadingsResponse
}
