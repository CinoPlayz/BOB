package utils.api.dao

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.result.Result
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import models.*
import utils.context.appContextGlobal

private val json = Json { ignoreUnknownKeys = true }

fun getAllStations(): List<Station> {
    val stations = mutableListOf<Station>()
    val req = Fuel.get("${appContextGlobal.url}/stations")
        .header("Accept-Language", "en")
        .header(
            "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0"
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

suspend fun insertStation(station: StationInsert): Boolean {
    val url = "${appContextGlobal.url}/stations"
    val body = Json.encodeToString(station)

    //println(apiContextGlobalTemp.url)
    //println(apiContextGlobalTemp.token)

    //println("Inserting $url")
    //println(body)

    val (_, response, result) = Fuel.post(url)
        .header("Accept-Language", "en")
        .header(
            "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0"
        )
        .header(Headers.AUTHORIZATION, "Bearer ${appContextGlobal.token}")
        .header(Headers.CONTENT_TYPE, "application/json")
        .jsonBody(body)
        .responseString()

    //println(response.statusCode)
    println(response.responseMessage)

    return when (result) {
        is Result.Success -> {
            //println("Success")
            val statusCode = response.statusCode
            statusCode in 200..299 // Check for successful status code
        }

        is Result.Failure -> {
            //println("Failure")
            throw IllegalStateException("Error: ${response.statusCode}")
        }
    }
}

suspend fun updateStation(station: StationUpdate): Station {
    val id = station.id
    val url = "${appContextGlobal.url}/stations/${id}"
    val body = Json.encodeToString(station)

    val (_, response, result) = Fuel.put(url)
        .header("Accept-Language", "en")
        .header(
            "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0"
        )
        .header(Headers.AUTHORIZATION, "Bearer ${appContextGlobal.token}")
        .header(Headers.CONTENT_TYPE, "application/json")
        .jsonBody(body)
        .responseString()

    return when (result) {
        is Result.Failure -> {
            throw IllegalStateException("Error Code: ${response.statusCode}")
        }

        is Result.Success -> {
            json.decodeFromString(result.get())
        }
    }
}

suspend fun deleteStation(id: String): Boolean {
    val url = "${appContextGlobal.url}/stations/${id}"

    val (_, response, result) = Fuel.delete(url)
        .header("Accept-Language", "en")
        .header(
            "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0"
        )
        .header(Headers.AUTHORIZATION, "Bearer ${appContextGlobal.token}")
        .header(Headers.CONTENT_TYPE, "application/json")
        .responseString()

    return when (result) {
        is Result.Failure -> {
            throw IllegalStateException("Error Code: ${response.statusCode}")
        }

        is Result.Success -> {
            val statusCode = response.statusCode
            statusCode in 200..299 // Check for successful status code (204)
        }
    }
}