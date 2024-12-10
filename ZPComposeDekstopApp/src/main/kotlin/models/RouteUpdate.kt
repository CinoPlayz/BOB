package models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import utils.parsing.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class RouteUpdate(
    @SerialName("_id")
    val id: String,
    val trainType: String,
    val trainNumber: Int,
    @Serializable(with = LocalDateTimeSerializer::class)
    val validFrom: LocalDateTime?,
    @Serializable(with = LocalDateTimeSerializer::class)
    val validUntil: LocalDateTime?,
    val canSupportBikes: Boolean,
    val drivesOn: List<Int>,
    val start: RouteStop,
    val end: RouteStop,
    val middle: List<RouteStop>,
    val newMiddle: List<RouteStopInsert>,
)
