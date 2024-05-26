package models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import utils.parsing.LocalDateTimeSerializer
import java.time.LocalDateTime

//Can be used for database conversion
@Serializable
data class DatabaseRequest(
    val timeOfRequest: OtherDate,
    val data: String) {

}

@Serializable
data class OtherDate (
    @Serializable(with = LocalDateTimeSerializer::class)
    @SerialName("\$date")
    val date: LocalDateTime
)

