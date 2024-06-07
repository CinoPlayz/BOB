package gui.generateData.engine

import androidx.compose.runtime.MutableState
import gui.addData.MiddleStop
import gui.addLeadingZero
import gui.daysOfWeek
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import models.*
import utils.api.dao.insertRandRoute
import utils.api.dao.insertRoute
import java.time.LocalDateTime
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import kotlin.random.Random

suspend fun generateRoutes(
    routes: List<RouteInsert>,
    trainTypes: List<String>,
    numberToGenerate: Int,
    allStations: List<Pair<String, String>>, // name, id
    isLoading: MutableState<Boolean>
): Pair<String, List<RouteInsert>> {
    return withContext(Dispatchers.Default) {
        try {
            isLoading.value = true

            val mutableRoutes = routes.toMutableList()

            val random = Random(System.currentTimeMillis())

            repeat(numberToGenerate) {
                val randomTrainType = trainTypes[random.nextInt(trainTypes.size)]
                val randomTrainNumber = random.nextInt(10, 99999)
                val (randomFromDate, randomUntilDate) = generateRandomDates(/*startDate, endDate*/)
                val randomSize = random.nextInt(1, 8) // at least one day
                val randomMiddlesSize = random.nextInt(0, 10)
                val (start, end, middle) = generateRouteStops(allStations, randomMiddlesSize)

                val routeInsert = RouteInsert(
                    trainType = randomTrainType,
                    trainNumber = randomTrainNumber,
                    validFrom = randomFromDate,
                    validUntil = randomUntilDate,
                    canSupportBikes = Random.nextBoolean(),
                    drivesOn = generateUniqueRandomList(randomSize),
                    start = start,
                    end = end,
                    middle = middle
                )

                mutableRoutes.add(routeInsert)
            }

            isLoading.value = false
            "" to mutableRoutes
        } catch (e: Exception) {
            isLoading.value = false
            "Error: ${e.message}" to routes
        }
    }
}

suspend fun updateRouteInGeneratedList(
    index: Int,
    trainType: String,
    trainNumber: Int?,
    validFromYear: String,
    validFromMonth: String,
    validFromDay: String,
    validFromHour: String,
    validFromMinute: String,
    validFromSecond: String,
    validUntilYear: String,
    validUntilMonth: String,
    validUntilDay: String,
    validUntilHour: String,
    validUntilMinute: String,
    validUntilSecond: String,
    canSupportBikes: Boolean,
    drivesOnDays: Map<String, Boolean>,
    selectedStartStation: String,
    hourStartStation: String,
    minuteStartStation: String,
    selectedEndStation: String,
    hourEndStation: String,
    minuteEndStation: String,
    middles: List<RouteStop>,
    newMiddles: List<RouteStopInsert>,
    onSuccess: (RouteInsert, Int) -> Unit
): String {
    if (trainNumber == null || trainNumber == 0) {
        return ("Route Number invalid.")
    }

    if (validFromYear.isEmpty() || validFromMonth.isEmpty() || validFromDay.isEmpty() || validFromHour.isEmpty() || validFromMinute.isEmpty() || validFromSecond.isEmpty()) {
        return ("Valid From format invalid.")
    }

    if (validUntilYear.isEmpty() || validUntilMonth.isEmpty() || validUntilDay.isEmpty() || validUntilHour.isEmpty() || validUntilMinute.isEmpty() || validUntilSecond.isEmpty()) {
        return ("Valid Until format invalid.")
    }

    val validFromDateTime: LocalDateTime
    val validUntilDateTime: LocalDateTime

    try {
        validFromDateTime = LocalDateTime.of(validFromYear.toInt(), validFromMonth.toInt(), validFromDay.toInt(), validFromHour.toInt(), validFromMinute.toInt(), validFromSecond.toInt())
    } catch (e: DateTimeParseException) {
        return ("Valid From format invalid.")
    }

    try {
        validUntilDateTime = LocalDateTime.of(validUntilYear.toInt(), validUntilMonth.toInt(), validUntilDay.toInt(), validUntilHour.toInt(), validUntilMinute.toInt(), validUntilSecond.toInt())
    } catch (e: DateTimeParseException) {
        return ("Valid Until format invalid.")
    }

    val drivesOn: List<Int> = drivesOnDays
        .filter { it.value } // Filter only checked
        .keys
        .mapNotNull { day -> daysOfWeek.indexOf(day).takeIf { it != -1 } }
        .sorted() // Sort low -> high


    val hourStart = hourStartStation.toIntOrNull()
    val minuteStart = minuteStartStation.toIntOrNull()
    if (hourStart == null || hourStart !in 0..23) {
        return "Invalid Route Departure Time: $hourStartStation"
    }
    if (minuteStart == null || minuteStart !in 0..59) {
        return "Invalid Route Departure Time: $minuteStartStation"
    }

    val hourEnd = hourEndStation.toIntOrNull()
    val minuteEnd = minuteEndStation.toIntOrNull()
    if (hourEnd == null || hourEnd !in 0..23) {
        return "Invalid Route Arrival Time: $hourStartStation"
    }
    if (minuteEnd == null || minuteEnd !in 0..59) {
        return "Invalid Route Arrival Time: $minuteStartStation"
    }

    val start = RouteStop(
        station = selectedStartStation,
        time = "${addLeadingZero(hourStart.toString())}:${addLeadingZero(minuteStart.toString())}"
    )

    val end = RouteStop(
        station = selectedEndStation,
        time = "${addLeadingZero(hourEnd.toString())}:${addLeadingZero(minuteEnd.toString())}"
    )

    /*
    * Filter Existing Middle Stations
    * */
    val filteredMiddles = middles.filter { it.station != "0" } // exclude with station set to Remove

    // Check if times are in the format HH:mm and within the range
    val invalidTimes = filteredMiddles.filter { stop ->
        val parts = stop.time.split(":")
        val hours = parts.getOrNull(0)?.toIntOrNull()
        val minutes = parts.getOrNull(1)?.toIntOrNull()

        // Add leading zeros if necessary
        val formattedTime = "${hours?.toString()?.padStart(2, '0')}:${minutes?.toString()?.padStart(2, '0')}"

        !formattedTime.matches(Regex("""\d{2}:\d{2}""")) ||
                hours !in 0..23 ||
                minutes !in 0..59
    }

    // If there are any invalid times, return error
    if (invalidTimes.isNotEmpty()) {
        //val invalidTimeStops = invalidTimes.joinToString(", ") { it.station }
        return "Invalid time format in Middle Stations."
    }

    /*
    * Filter New Middle Stations
    * */
    val filteredNewMiddles = newMiddles.filter { it.station != "0" }

    val invalidTimesNew = filteredNewMiddles.filter { stop ->
        val parts = stop.time.split(":")
        val hours = parts.getOrNull(0)?.toIntOrNull()
        val minutes = parts.getOrNull(1)?.toIntOrNull()

        val formattedTime = "${hours?.toString()?.padStart(2, '0')}:${minutes?.toString()?.padStart(2, '0')}"

        !formattedTime.matches(Regex("""\d{2}:\d{2}""")) ||
                hours !in 0..23 ||
                minutes !in 0..59
    }

    if (invalidTimesNew.isNotEmpty()) {
        return "Invalid time format in New Middle Stations."
    }

    val routeUpdate = RouteInsert(
        trainType = trainType,
        trainNumber = trainNumber,
        validFrom = validFromDateTime,
        validUntil = validUntilDateTime,
        canSupportBikes = canSupportBikes,
        drivesOn = drivesOn,
        start = start,
        end = end,
        middle = filteredMiddles,
        //newMiddle = filteredNewMiddles
    )

    return try {
        onSuccess(routeUpdate, index)
        ""
    } catch (e: Exception) {
        "Error updating route in list. ${e.message}"
    }
}

suspend fun insertRouteFromGeneratedListToDB(
    route: RouteInsert,
    index: Int,
    onSuccess: (Boolean, Int) -> Unit
): String {
    return try {
        var success: Boolean
        coroutineScope {
            success = insertRandRoute(route)
        }
        onSuccess(success, index)
        ""
    } catch (e: Exception) {
        "Error inserting route to the database. ${e.message}"
    }
}

suspend fun insertAllRoutesFromGeneratedListToDB(
    routes: List<RouteInsert>,
    isLoading: MutableState<Boolean>
): Pair<String, List<RouteInsert>> {
    return withContext(Dispatchers.Default) {
        isLoading.value = true

        val mutableRoutes = routes.toMutableList()

        var successCount = 0
        var failureCount = 0

        val iterator = mutableRoutes.iterator()
        while (iterator.hasNext()) {
            val route = iterator.next()
            try {
                insertRandRoute(route)
                successCount++
                iterator.remove()
            } catch (e: Exception) {
                failureCount++
            }
        }

        isLoading.value = false
        "Insert All Routes: Success: $successCount, Failed: $failureCount" to mutableRoutes
    }
}