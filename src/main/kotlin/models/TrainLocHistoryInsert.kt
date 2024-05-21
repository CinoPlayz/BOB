package models

import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import utils.parsing.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable(with = LocalDateTimeSerializer::class)
data class TrainLocHistoryInsert(
    val timeOfRequest: LocalDateTime,
    val trainType: String,
    val trainNumber: String,
    val routeFrom: String,
    val routeTo: String,
    val routeStartTime: String,
    val nextStation: String,
    val delay: Int,
    val coordinates: Coordinates,
)


