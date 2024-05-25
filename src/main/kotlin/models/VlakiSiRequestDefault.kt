package models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import utils.api.dao.getAllRoutes
import utils.api.dao.getAllStations
import utils.parsing.LocalDateTimeSerializer
import java.time.LocalDateTime

//Can be used for both database conversion and in app request conversion
@Serializable
data class VlakiSiRequestDefault(
    val timeOfRequest: OtherDate,
    val data: String) {

}

@Serializable
data class OtherDate (
    @Serializable(with = LocalDateTimeSerializer::class)
    @SerialName("\$date")
    val date: LocalDateTime
)

