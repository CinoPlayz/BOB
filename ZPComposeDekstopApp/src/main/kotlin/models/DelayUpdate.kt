package models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import utils.parsing.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class DelayUpdate(
    @SerialName("_id")
    val id: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    var timeOfRequest: LocalDateTime?,
    val route: String,
    val currentStation: String,
    val delay: Int,
)
