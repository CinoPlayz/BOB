package models

import kotlinx.serialization.Serializable

@Serializable
data class RouteStopInsert(
    val station: String = "",
    val time: String = "",
)