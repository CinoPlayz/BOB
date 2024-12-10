package models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import utils.parsing.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class Token(
    var token: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    var expiresOn: LocalDateTime?,
    var type: String, // [all, login]
    @SerialName("_id")
    val id: String
)