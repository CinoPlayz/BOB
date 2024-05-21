package models

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class Station(
    val id: String,
    val name: String,
    val officialStationNumber: String,
    val coordinates: Coordinates,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)