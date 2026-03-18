package com.vitavault.mobile.health

import android.content.Context
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.records.metadata.DataOrigin
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.vitavault.mobile.network.DeviceReadingPayload
import java.time.Instant

class HealthConnectManager(private val context: Context) {
    companion object {
        const val PROVIDER_PACKAGE_NAME = "com.google.android.apps.healthdata"
    }

    private val client by lazy { HealthConnectClient.getOrCreate(context) }

    val permissions: Set<String> = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(WeightRecord::class),
        HealthPermission.getReadPermission(BloodPressureRecord::class),
        HealthPermission.getReadPermission(OxygenSaturationRecord::class),
    )

    fun sdkStatus(): Int = HealthConnectClient.getSdkStatus(context, PROVIDER_PACKAGE_NAME)

    suspend fun hasAllPermissions(): Boolean {
        return client.permissionController.getGrantedPermissions().containsAll(permissions)
    }

    suspend fun grantedPermissionCount(): Int {
        return client.permissionController.getGrantedPermissions().intersect(permissions).size
    }

    fun requestPermissionsActivityContract(): ActivityResultContract<Set<String>, Set<String>> {
        return PermissionController.createRequestPermissionResultContract()
    }

    suspend fun readLastDays(days: Long): List<DeviceReadingPayload> {
        val end = Instant.now()
        val start = end.minusSeconds(days * 24 * 60 * 60)
        val readings = mutableListOf<DeviceReadingPayload>()

        val stepAggregate = client.aggregate(
            AggregateRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(start, end),
                dataOriginFilter = setOf(DataOrigin("android")),
            )
        )
        val totalSteps = stepAggregate[StepsRecord.COUNT_TOTAL]
        if (totalSteps != null) {
            readings += DeviceReadingPayload(
                readingType = "STEPS",
                capturedAt = end.toString(),
                clientReadingId = "steps_${end.epochSecond}",
                unit = "count",
                valueInt = totalSteps.toInt(),
                metadata = mapOf("aggregated" to true, "windowDays" to days),
            )
        }

        val weightResponse = client.readRecords(
            ReadRecordsRequest(
                recordType = WeightRecord::class,
                timeRangeFilter = TimeRangeFilter.between(start, end),
            )
        )
        weightResponse.records.forEach { record ->
            readings += DeviceReadingPayload(
                readingType = "WEIGHT",
                capturedAt = record.time.toString(),
                clientReadingId = record.metadata.id,
                unit = "kg",
                valueFloat = record.weight.inKilograms,
            )
        }

        val bpResponse = client.readRecords(
            ReadRecordsRequest(
                recordType = BloodPressureRecord::class,
                timeRangeFilter = TimeRangeFilter.between(start, end),
            )
        )
        bpResponse.records.forEach { record ->
            readings += DeviceReadingPayload(
                readingType = "BLOOD_PRESSURE",
                capturedAt = record.time.toString(),
                clientReadingId = record.metadata.id,
                unit = "mmHg",
                systolic = record.systolic.inMillimetersOfMercury.toInt(),
                diastolic = record.diastolic.inMillimetersOfMercury.toInt(),
            )
        }

        val oxygenResponse = client.readRecords(
            ReadRecordsRequest(
                recordType = OxygenSaturationRecord::class,
                timeRangeFilter = TimeRangeFilter.between(start, end),
            )
        )
        oxygenResponse.records.forEach { record ->
            readings += DeviceReadingPayload(
                readingType = "OXYGEN_SATURATION",
                capturedAt = record.time.toString(),
                clientReadingId = record.metadata.id,
                unit = "percent",
                valueInt = record.percentage.value.toInt(),
            )
        }

        val heartRateResponse = client.readRecords(
            ReadRecordsRequest(
                recordType = HeartRateRecord::class,
                timeRangeFilter = TimeRangeFilter.between(start, end),
            )
        )
        heartRateResponse.records.forEach { record ->
            record.samples.forEach { sample ->
                readings += DeviceReadingPayload(
                    readingType = "HEART_RATE",
                    capturedAt = sample.time.toString(),
                    clientReadingId = record.metadata.id + "_" + sample.time.epochSecond,
                    unit = "bpm",
                    valueInt = sample.beatsPerMinute.toInt(),
                )
            }
        }

        return readings.sortedBy { it.capturedAt }
    }

    fun deviceLabel(): String {
        val name = Settings.Global.getString(context.contentResolver, Settings.Global.DEVICE_NAME)
        return if (!name.isNullOrBlank()) name else "Android Device"
    }

    fun appVersion(): String = "1.0.0"
}
