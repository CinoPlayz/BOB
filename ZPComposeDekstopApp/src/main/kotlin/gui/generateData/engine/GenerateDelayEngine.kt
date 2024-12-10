package gui.generateData.engine

import androidx.compose.runtime.MutableState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import models.DelayInsert
import utils.api.dao.insertDelay
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.random.Random

suspend fun generateDelays(
    delays: List<DelayInsert>,
    numberToGenerate: Int,
    allStations: List<Pair<String, String>>,
    allRoutes: List<Pair<Int, String>>,
    isLoading: MutableState<Boolean>
): Pair<String, List<DelayInsert>> {
    return withContext(Dispatchers.Default) {
        try {
            isLoading.value = true

            val mutableDelays = delays.toMutableList()

            val random = Random(System.currentTimeMillis())

            val startDate = LocalDateTime.of(2024, 4, 1, 0, 0)
            val endDate = LocalDateTime.of(2024, 5, 31, 23, 59)
            val daysBetween = ChronoUnit.DAYS.between(startDate, endDate).toInt()

            repeat(numberToGenerate) {
                val randomDate = startDate.plusDays(random.nextLong(daysBetween.toLong())).plusHours(random.nextInt(24).toLong()).plusMinutes(random.nextInt(60).toLong())
                val randomRoute = allRoutes[random.nextInt(allRoutes.size)].second
                val randomStation = allStations[random.nextInt(allStations.size)].second
                val randomDelay = random.nextInt(1, 61)

                // println("Generated DelayInsert - Date: $randomDate, Route: $randomRoute, Station: $randomStation, Delay: $randomDelay")

                val delayInsert = DelayInsert(
                    timeOfRequest = randomDate,
                    route = randomRoute,
                    currentStation = randomStation,
                    delay = randomDelay
                )

                mutableDelays.add(delayInsert)
            }

            isLoading.value = false
            "" to mutableDelays
            // "Delays generated successfully" to mutableDelays
        } catch (e: Exception) {
            isLoading.value = false
            "Error: ${e.message}" to delays
        }
    }
}

suspend fun insertAllDelaysFromGeneratedListToDB(
    delays: List<DelayInsert>,
    isLoading: MutableState<Boolean>
): Pair<String, List<DelayInsert>> {
    return withContext(Dispatchers.Default) {
        isLoading.value = true

        val mutableDelays = delays.toMutableList()

        var successCount = 0
        var failureCount = 0

        val iterator = mutableDelays.iterator()
        while (iterator.hasNext()) {
            val delay = iterator.next()
            try {
                insertDelay(delay)
                successCount++
                iterator.remove()
            } catch (e: Exception) {
                failureCount++
            }
        }

        isLoading.value = false
        "Insert All Delays: Success: $successCount, Failed: $failureCount" to mutableDelays
    }
}

