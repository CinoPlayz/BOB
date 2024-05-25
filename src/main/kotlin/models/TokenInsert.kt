package models

import kotlinx.serialization.Serializable
import utils.parsing.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class TokenInsert(
    var token: String = "",
    @Serializable(with = LocalDateTimeSerializer::class)
    var expiresOn: LocalDateTime? = LocalDateTime.now(),
    var type: String = "", // [all, login]
)