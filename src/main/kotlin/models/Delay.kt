package models

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class Delay(
    val id: String,
    val timeOfRequest: LocalDateTime,
    val route: String,
    val currentStation: String,
    val delay: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
