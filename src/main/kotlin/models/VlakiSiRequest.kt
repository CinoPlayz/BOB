package models

import org.bson.types.ObjectId
import java.time.LocalDateTime

//Can be used for both database conversion and in app request conversion
data class VlakiSiRequest(val id: ObjectId?, val timeOfRequest: LocalDateTime, val data: VlakiSi) {
    fun toListTrainLocHistory(): List<TrainLocHistory> {
        val trainLocHistoryList = mutableListOf<TrainLocHistory>()
        for (item in data.data) {
            val routeFrom = item.train_data.train_times.first().stop_name
            val routeTo = item.train_data.train_times.last().stop_name
            val routeStartTime = item.train_data.train_times.first().departure_time
            val nextStation: String =
                if (item.train_data.next_stop_sequence > item.train_data.train_times.count()) "" else item.train_data.train_times[item.train_data.next_stop_sequence - 1].stop_name

            val trainLocHistory = TrainLocHistory(
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
}

