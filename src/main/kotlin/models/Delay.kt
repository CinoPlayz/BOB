package models

import java.time.LocalDateTime

data class Delay(val timeOfRequest: LocalDateTime, val route: Route, val currentStation: Station, val delay: Int)
