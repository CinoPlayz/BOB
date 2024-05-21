package models

import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import java.time.LocalDateTime

@Serializable
data class RouteStop(
    val station: ObjectId,
    val time: LocalDateTime
)
