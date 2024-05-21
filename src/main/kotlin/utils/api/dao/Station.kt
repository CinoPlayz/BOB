package utils.api.dao

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.result.Result
import models.Station

fun getAllStations(apiContext: ApiContext): List<Station>? {
    val stations = mutableListOf<Station>()
    Fuel.get("${apiContext.url}/stations")
        .header("Accept-Language", "en")
        .header(
            "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0"
        )
        .responseString { result ->
            when (result) {
                is Result.Failure -> println("Could get data from API: ${result.error.message}")
                is Result.Success -> {
                    result.value
                }

            }
        }
    return stations

}