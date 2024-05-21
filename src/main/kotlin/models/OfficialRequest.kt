package models

import org.bson.types.ObjectId
import utils.parsing.unescapeUnicode
import java.time.LocalDateTime

//Can be used for both database conversion and in app request conversion
data class OfficialRequest(val id: ObjectId?, val timeOfRequest: LocalDateTime, val data: List<Official>) {

    fun toListTrainLocHistory(): List<TrainLocHistory> {
        val trainLocHistoryList = mutableListOf<TrainLocHistory>()
        for (item in data) {
            val routeUnescaped = item.Relacija.unescapeUnicode()
            val routeConverted = routeUnescaped.replace("+", " ")
            val route = routeConverted.split("-")

            val coordinatesSplit = item.Koordinate.split(",")
            val coordinates = Coordinates(lat = coordinatesSplit[1].toFloat(), lng = coordinatesSplit[0].toFloat())
            val trainLocHistory = TrainLocHistory(
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
