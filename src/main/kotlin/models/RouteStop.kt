package models

import java.time.LocalDateTime

data class RouteStop(
    val station: Station,
    val time: LocalDateTime
)
