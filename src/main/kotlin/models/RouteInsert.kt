package models

import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import java.time.LocalDateTime

@Serializable
data class RouteInsert(
    val trainType: String,
    val trainNumber: String,
    val vaildFrom: LocalDateTime,
    val validUntil: LocalDateTime,
    val canSupportBikes: String,
    val drivesOn: List<Int>,
    val start: RouteStop,
    val end: RouteStop,
    val middle: List<RouteStop>,
)
