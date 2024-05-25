package models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import utils.parsing.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class UserUpdate(
    @SerialName("_id")
    val id: String,
    val username: String,
    var email: String,
    val password: String,
    var tokens: List<Token>? = emptyList(),
    var newTokens: List<TokenInsert>? = emptyList(),
    var `2faEnabled`: Boolean,
    var `2faSecret`: String,
    val role: String, // [user, admin]
)