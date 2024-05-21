package models

import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import java.time.LocalDateTime

@Serializable
data class DelayInsert(
    val timeOfRequest: LocalDateTime,
    val route: ObjectId,
    val currentStation: ObjectId,
    val delay: Int
)
