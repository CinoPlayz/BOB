package models

import kotlinx.serialization.Serializable

@Serializable
data class RouteStop(
    val station: String,
    val time: String
)
