package models

import utils.api.dao.getAllRoutes
import utils.api.dao.getAllStations
import java.time.LocalDateTime

//Can be used for both database conversion and in app request conversion
data class VlakiSiRequest(val timeOfRequest: LocalDateTime, val data: VlakiSi) {
    fun toListTrainLocHistory(): List<TrainLocHistoryInsert> {
        val trainLocHistoryList = mutableListOf<TrainLocHistoryInsert>()
        for (item in data.data) {
            val routeFrom = item.train_data.train_times.first().stop_name
            val routeTo = item.train_data.train_times.last().stop_name
            val routeStartTime = item.train_data.train_times.first().departure_time
            val nextStation: String =
                if (item.train_data.next_stop_sequence > item.train_data.train_times.count()) "" else item.train_data.train_times[item.train_data.next_stop_sequence - 1].stop_name

            val trainLocHistory = TrainLocHistoryInsert(
                timeOfRequest = timeOfRequest,
                trainType = item.train_data.train_type,
                trainNumber = item.train_data.train_number,
                routeFrom = routeFrom,
                routeTo = routeTo,
                routeStartTime = routeStartTime ?: "",
                nextStation = nextStation,
                delay = item.train_cache.delay,
                coordinates = item.coordinates
            )
            trainLocHistoryList.add(trainLocHistory)
        }
        return trainLocHistoryList
    }

    fun toListDelay(): List<DelayInsert> {
        val delayList = mutableListOf<DelayInsert>()

        val stations: List<Station> = getAllStations()
        val routes: List<Route> = getAllRoutes()

        for (item in data.data) {
            val route = routes.firstOrNull { it.trainType == item.train_data.train_type && it.trainNumber.toString() == item.train_data.train_number }
            val nextStation: String =
                if (item.train_data.current_stop_sequence > item.train_data.train_times.count()) "" else item.train_data.train_times[item.train_data.next_stop_sequence - 1].stop_name
            val station = stations.firstOrNull { it.name == nextStation }

            if(route == null || station == null){
                continue
            }

            val delay = DelayInsert(
                timeOfRequest = timeOfRequest,
                route = route.id,
                currentStation = station.id,
                delay = item.train_cache.delay
            )

            delayList.add(delay)
        }
        return delayList
    }
}

