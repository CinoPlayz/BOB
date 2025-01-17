package si.bob.zpmobileapp.ui.map

data class Coordinates(
    val lat: Double,
    val lng: Double
)

data class TrainLocHistory(
    val timeOfRequest: String,
    val trainType: String,
    val trainNumber: String,
    val routeFrom: String,
    val routeTo: String,
    val routeStartTime: String,
    val nextStation: String,
    val delay: Int,
    val coordinates: Coordinates,
    val estimatedOccupancy: Double? = null,
    val realOccupancy: Double? = null
)

data class Passenger(
    val timeOfRequest: String,
    val coordinates: Coordinates,
    val guessedOccupancyRate: Double,
    val realOccupancyRate: Double? = null,
    val route: String, // == trainNumber
    val postedByUserId: String
)

fun getOccupancyLevel(occupancy: Double?): String {
    return when {
        occupancy == null -> "N/A"
        occupancy <= 40 -> "Low"
        occupancy <= 70 -> "Moderate"
        else -> "High"
    }
}