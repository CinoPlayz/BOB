package gui.generateData.engine

import models.Coordinates
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

fun generateRandomCoordinates(): Coordinates {
    val lat = Random.nextFloat() * (47.000000f - 45.000000f) + 45.000000f
    val lng = Random.nextFloat() * (17.000000f - 13.000000f) + 13.000000f
    return Coordinates(lat, lng)
}

fun generateRandomTime(): String {
    val random = Random(System.currentTimeMillis())

    val randomHour = random.nextInt(24)
    val randomMinute = random.nextInt(60)
    val randomSecond = random.nextInt(60)

    val randomTime = LocalTime.of(randomHour, randomMinute, randomSecond)
    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    return randomTime.format(formatter)
}