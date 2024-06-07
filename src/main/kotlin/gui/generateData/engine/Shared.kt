package gui.generateData.engine

import models.Coordinates
import models.RouteStop
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.random.Random

fun generateRandomCoordinates(): Coordinates {
    val lat = Random.nextFloat() * (47.000000f - 45.000000f) + 45.000000f
    val lng = Random.nextFloat() * (17.000000f - 13.000000f) + 13.000000f
    return Coordinates(lat, lng)
}

fun generateRandomTimeWithSecondsString(): String {
    val random = Random(System.currentTimeMillis())

    val randomHour = random.nextInt(24)
    val randomMinute = random.nextInt(60)
    val randomSecond = random.nextInt(60)

    val randomTime = LocalTime.of(randomHour, randomMinute, randomSecond)
    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    return randomTime.format(formatter)
}

fun generateRandomTimeWithoutSecondsString(size: Int): List<String> {
    return List(size) {
        val hour = Random.nextInt(24).toString().padStart(2, '0')
        val minute = Random.nextInt(60).toString().padStart(2, '0')
        "$hour:$minute"
    }
}

fun generateProgressiveRandomTimeWithoutSecondsString(size: Int): List<String> {
    val times = mutableListOf<LocalTime>()
    var currentTime = LocalTime.of(0, 0)
    val formatter = DateTimeFormatter.ofPattern("HH:mm")

    for (i in 0 until size) {
        val nextTime = currentTime.plusHours(Random.nextLong(0, 2))
            .plusMinutes(Random.nextLong(1, 60))
        times.add(nextTime)
        currentTime = nextTime
    }

    return times.map { it.format(formatter) }
}

fun generateRandomDates(/*startDate: LocalDateTime, endDate: LocalDateTime*/): Pair<LocalDateTime, LocalDateTime> {
    val random = Random(System.currentTimeMillis())

    val startDate = LocalDateTime.of(2022, 1, 1, 0, 0)
    val endDate = LocalDateTime.of(2026, 12, 31, 23, 59)
    val daysBetween = ChronoUnit.DAYS.between(startDate, endDate)

    val randomFromDate = startDate.plusDays(random.nextLong(daysBetween + 1))
        .plusHours(random.nextInt(24).toLong())
        .plusMinutes(random.nextInt(60).toLong())

    // randomUntilDate > randomFromDate
    val randomUntilDate = randomFromDate.plusDays(random.nextLong(1, daysBetween + 1))
        .plusHours(random.nextInt(24).toLong())
        .plusMinutes(random.nextInt(60).toLong())

    return Pair(randomFromDate, randomUntilDate)
}

fun generateUniqueRandomList(size: Int): List<Int> {
    require(size in 0..8) { "Size must be between 0 and 8" }

    val numbers = (0..7).toMutableList()
    numbers.shuffle(Random.Default)

    return numbers.take(size)
}

fun generateRouteStops(allStations: List<Pair<String, String>>, randomMiddlesSize: Int): Triple<RouteStop, RouteStop, List<RouteStop>> {
    require(randomMiddlesSize >= 0 && randomMiddlesSize <= allStations.size - 2) { "Invalid size for random middles." }

    val shuffledStations = allStations.shuffled(Random.Default)
    val selectedStations = shuffledStations.take(randomMiddlesSize + 2)

    val startStation = selectedStations.first()
    val endStation = selectedStations.last()
    val middleStations = selectedStations.subList(1, selectedStations.size - 1)

    val randomTimes = generateProgressiveRandomTimeWithoutSecondsString(randomMiddlesSize + 2)

    val start = RouteStop(station = startStation.second, time = randomTimes.first())
    val end = RouteStop(station = endStation.second, time = randomTimes.last())
    val middle = middleStations.mapIndexed { index, station ->
        RouteStop(station = station.second, time = randomTimes[index + 1])
    }

    return Triple(start, end, middle)
}

