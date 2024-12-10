package models

import kotlinx.serialization.Serializable
import utils.parsing.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class UserInsert (
    val username: String,
    val email: String,
    val password: String,
    val tokens: List<Token>,
    val twoFactorAuthenticationEnabled: Boolean,
    val twoFactorAuthenticationSecret: String?,
    val role: String, // [user, admin]
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime,
)