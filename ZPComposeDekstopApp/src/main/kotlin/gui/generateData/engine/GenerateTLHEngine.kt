package gui.generateData.engine

import androidx.compose.runtime.MutableState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import models.TrainLocHistoryInsert
import utils.api.dao.insertTrainLocHistory
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.random.Random

suspend fun generateTLHs(
    tlhs: List<TrainLocHistoryInsert>,
    trainTypes: List<String>,
    numberToGenerate: Int,
    allStations: List<String>,
    allRoutes: List<Int>,
    isLoading: MutableState<Boolean>
): Pair<String, List<TrainLocHistoryInsert>> {
    return withContext(Dispatchers.Default) {
        try {
            isLoading.value = true

            val mutableTLHs = tlhs.toMutableList()

            val random = Random(System.currentTimeMillis())

            val startDate = LocalDateTime.of(2024, 4, 1, 0, 0)
            val endDate = LocalDateTime.of(2024, 5, 31, 23, 59)
            val daysBetween = ChronoUnit.DAYS.between(startDate, endDate).toInt()

            repeat(numberToGenerate) {
                val randomDate = startDate.plusDays(random.nextLong(daysBetween.toLong())).plusHours(random.nextInt(24).toLong()).plusMinutes(random.nextInt(60).toLong())
                val randomRoute = allRoutes[random.nextInt(allRoutes.size)]
                val randomStationFrom = allStations[random.nextInt(allStations.size)]
                val randomStationTo = allStations[random.nextInt(allStations.size)]
                val randomStationNext = allStations[random.nextInt(allStations.size)]
                val randomTrainType = trainTypes[random.nextInt(trainTypes.size)]
                val randomDelay = random.nextInt(1, 61)
                val randomCoordinates = generateRandomCoordinates()

                val tlhInsert = TrainLocHistoryInsert(
                    timeOfRequest = randomDate,
                    trainType = randomTrainType,
                    trainNumber = randomRoute.toString(),
                    routeFrom = randomStationFrom,
                    routeTo = randomStationTo,
                    routeStartTime = generateRandomTimeWithSecondsString(),
                    nextStation = randomStationNext,
                    delay = randomDelay,
                    coordinates = randomCoordinates
                )

                mutableTLHs.add(tlhInsert)
            }

            isLoading.value = false
            "" to mutableTLHs
        } catch (e: Exception) {
            isLoading.value = false
            "Error: ${e.message}" to tlhs
        }
    }
}


suspend fun insertAllTLHsFromGeneratedListToDB(
    tlhs: List<TrainLocHistoryInsert>,
    isLoading: MutableState<Boolean>
): Pair<String, List<TrainLocHistoryInsert>> {
    return withContext(Dispatchers.Default) {
        isLoading.value = true

        val mutableTLHs = tlhs.toMutableList()

        var successCount = 0
        var failureCount = 0

        val iterator = mutableTLHs.iterator()
        while (iterator.hasNext()) {
            val tlh = iterator.next()
            try {
                insertTrainLocHistory(tlh)
                successCount++
                iterator.remove()
            } catch (e: Exception) {
                failureCount++
            }
        }

        isLoading.value = false
        "Insert All Train Location Histories:\nSuccess: $successCount, Failed: $failureCount" to mutableTLHs
    }
}