package models

import kotlinx.serialization.Serializable

@Serializable
data class StationInsert(
    val name: String,
    val officialStationNumber: String,
    val coordinates: Coordinates
)