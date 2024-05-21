package models

import kotlinx.serialization.Serializable
import utils.parsing.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class RouteInsert(
    val trainType: String,
    val trainNumber: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val vaildFrom: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val validUntil: LocalDateTime,
    val canSupportBikes: String,
    val drivesOn: List<Int>,
    val start: RouteStop,
    val end: RouteStop,
    val middle: List<RouteStop>,
)