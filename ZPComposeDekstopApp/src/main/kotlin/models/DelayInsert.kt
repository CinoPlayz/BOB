package models

import kotlinx.serialization.Serializable
import utils.parsing.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class DelayInsert(
    @Serializable(with = LocalDateTimeSerializer::class)
    val timeOfRequest: LocalDateTime,
    val route: String,
    val currentStation: String,
    val delay: Int
)
