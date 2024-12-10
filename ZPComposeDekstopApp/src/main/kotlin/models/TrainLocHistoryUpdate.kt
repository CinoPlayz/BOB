package models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import utils.parsing.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class TrainLocHistoryUpdate(
    @SerialName("_id")
    val id: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val timeOfRequest: LocalDateTime,
    val trainType: String,
    val trainNumber: String,
    val routeFrom: String,
    val routeTo: String,
    val routeStartTime: String,
    val nextStation: String,
    val delay: Int,
    val coordinates: Coordinates
)


