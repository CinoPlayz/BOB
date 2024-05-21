package models

import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import java.time.LocalDateTime

@Serializable
data class StationInsert(
    val name: String,
    val officialStationNumber: String,
    val coordinates: Coordinates
)