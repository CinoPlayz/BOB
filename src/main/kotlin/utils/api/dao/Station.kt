package utils.api.dao

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import kotlinx.serialization.json.Json
import models.Station

private val json = Json { ignoreUnknownKeys = true }

fun getAllStations(apiContext: ApiContext): List<Station> {
    val stations = mutableListOf<Station>()
    val req = Fuel.get("${apiContext.url}/stations")
        .header("Accept-Language", "en")
        .header(
            "UserInsert-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0"
        )
        .responseString { result ->
            when (result) {
                is Result.Failure -> {
                    println("Could get data from API: ${result.error.message}")
                }
                is Result.Success -> {
                    val getStations = json.decodeFromString<List<Station>>(result.value)
                    stations.addAll(getStations)
                }

            }
        }

    //Blocking
    req.join()

    return stations
}