package models

import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import utils.parsing.unescapeUnicode
import java.time.LocalDateTime

//Can be used for both database conversion and in app request conversion
@Serializable
data class OfficialRequest(val timeOfRequest: LocalDateTime, val data: List<Official>) {

    fun toListTrainLocHistory(): List<TrainLocHistoryInsert> {
        val trainLocHistoryList = mutableListOf<TrainLocHistoryInsert>()
        for (item in data) {
            val routeUnescaped = item.Relacija.unescapeUnicode()
            val routeConverted = routeUnescaped.replace("+", " ")
            val route = routeConverted.split("-")

            val coordinatesSplit = item.Koordinate.split(",")
            val coordinates = Coordinates(lat = coordinatesSplit[1].toFloat(), lng = coordinatesSplit[0].toFloat())
            val trainLocHistory = TrainLocHistoryInsert(
                timeOfRequest = timeOfRequest,
                trainType = item.Rang,
                trainNumber = item.St_vlaka,
                routeFrom = route[0],
                routeTo = route[1],
                routeStartTime = item.Odhod,
                nextStation = item.Postaja,
                delay = item.Zamuda_cas,
                coordinates = coordinates
            )

            trainLocHistoryList.add(trainLocHistory)
        }
        return trainLocHistoryList
    }
}
