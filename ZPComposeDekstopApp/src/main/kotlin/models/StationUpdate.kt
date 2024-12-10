package models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import utils.parsing.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class StationUpdate(
    @SerialName("_id")
    val id: String,
    val name: String,
    val officialStationNumber: String,
    val coordinates: Coordinates
)