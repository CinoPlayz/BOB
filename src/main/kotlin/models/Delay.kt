package models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import utils.parsing.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class Delay(
    @SerialName("_id")
    val id: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val timeOfRequest: LocalDateTime,
    val route: String,
    val currentStation: String,
    val delay: Int,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime
)
