package models

import java.time.LocalDateTime

data class TrainLocHistory(
    var timeOfRequest: LocalDateTime,
    val trainType: String,
    var trainNumber: String,
    var routeFrom: String,
    var routeTo: String,
    var routeStartTime: String,
    var nextStation: String,
    var delay: Int,
    var coordinates: Coordinates
)


