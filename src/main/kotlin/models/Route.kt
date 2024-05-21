package models

import java.time.LocalDateTime

data class Route(
    val trainType: String,
    val trainNumber: String,
    val vaildFrom: LocalDateTime,
    val validUntil: LocalDateTime,
    val canSupportBikes: String,
    val drivesOn: List<Int>,
    val start: RouteStop,
    val end: RouteStop,
    val middle: List<RouteStop>
)
