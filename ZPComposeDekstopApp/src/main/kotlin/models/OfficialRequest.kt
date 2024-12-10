package models

import utils.api.dao.getAllRoutes
import utils.api.dao.getAllStations
import utils.parsing.unescapeUnicode
import java.time.LocalDateTime

//Can be used for both database conversion and in app request conversion
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
                routeStartTime = item.Odhod + ":00",
                nextStation = item.Postaja,
                delay = item.Zamuda_cas,
                coordinates = coordinates
            )

            trainLocHistoryList.add(trainLocHistory)
        }
        return trainLocHistoryList
    }

    fun toListDelay(): List<DelayInsert> {
        val delayList = mutableListOf<DelayInsert>()

        val stations: List<Station> = getAllStations()
        val routes: List<Route> = getAllRoutes()

        for (item in data) {
            val route = routes.firstOrNull { it.trainType == item.Rang && it.trainNumber.toString() == item.St_vlaka }
            val station = stations.firstOrNull { it.name == item.Postaja }

            if(route == null || station == null){
                continue
            }

            val delay = DelayInsert(
                timeOfRequest = timeOfRequest,
                route = route.id,
                currentStation = station.id,
                delay = item.Zamuda_cas
            )

            delayList.add(delay)
        }
        return delayList
    }
}
