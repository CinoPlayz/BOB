package models

import kotlinx.serialization.Serializable
import utils.parsing.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class Token(
    val token: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val expiresAt: LocalDateTime,
    val type: String // [all, login]
)