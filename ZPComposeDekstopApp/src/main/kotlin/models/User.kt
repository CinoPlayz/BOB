package models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import utils.parsing.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class User(
    @SerialName("_id")
    val id: String,
    val username: String,
    var email: String,
    val password: String,
    var tokens: List<Token>? = emptyList(),
    var `2faEnabled`: Boolean,
    var `2faSecret`: String? = null,
    val role: String, // [user, admin]
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime,
)