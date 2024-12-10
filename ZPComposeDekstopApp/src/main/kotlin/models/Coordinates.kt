package models

import kotlinx.serialization.Serializable

@Serializable
data class Coordinates(val lat: Float?, val lng: Float?)
