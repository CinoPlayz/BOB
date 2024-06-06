package gui.generateData.engine

import androidx.compose.runtime.MutableState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import models.StationInsert
import kotlin.random.Random
import io.github.serpro69.kfaker.Faker
import io.github.serpro69.kfaker.faker
import io.github.serpro69.kfaker.fakerConfig
import utils.api.dao.insertStation

suspend fun generateStations(
    stations: List<StationInsert>,
    numberToGenerate: Int,
    isLoading: MutableState<Boolean>
): Pair<String, List<StationInsert>> {
    return withContext(Dispatchers.Default) {
        try {
            isLoading.value = true

            val mutableStations = stations.toMutableList()

            val random = Random(System.currentTimeMillis())
            val config = fakerConfig { locale = "fr" } // 282 cities
            val faker = Faker(config)
            // val faker = faker { }

            repeat(numberToGenerate) {
                val randomCoordinates = generateRandomCoordinates()
                val randomOfficialStationNumber = random.nextInt(1000, 99999)

                val stationInsert = StationInsert(
                    name = faker.address.city(),
                    officialStationNumber = randomOfficialStationNumber.toString().padStart(5, '0'), // add leading zero
                    coordinates = randomCoordinates
                )

                mutableStations.add(stationInsert)
            }

            isLoading.value = false
            "" to mutableStations
        } catch (e: Exception) {
            isLoading.value = false
            "Error: ${e.message}" to stations
        }
    }
}

suspend fun insertAllStationsFromGeneratedListToDB(
    stations: List<StationInsert>,
    isLoading: MutableState<Boolean>
): Pair<String, List<StationInsert>> {
    return withContext(Dispatchers.Default) {
        isLoading.value = true

        val mutableStations = stations.toMutableList()

        var successCount = 0
        var failureCount = 0

        val iterator = mutableStations.iterator()
        while (iterator.hasNext()) {
            val station = iterator.next()
            try {
                insertStation(station)
                successCount++
                iterator.remove()
            } catch (e: Exception) {
                failureCount++
            }
        }

        isLoading.value = false
        "Insert All Stations: Success: $successCount, Failed: $failureCount" to mutableStations
    }
}