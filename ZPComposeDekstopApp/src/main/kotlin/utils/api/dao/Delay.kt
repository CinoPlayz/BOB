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

fun getAllDelays(): List<Delay> {
    val delays = mutableListOf<Delay>()
    val req = Fuel.get("${appContextGlobal.get().url}/delays")
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
                    val getDelays = json.decodeFromString<List<Delay>>(result.value)
                    delays.addAll(getDelays)
                }

            }
        }
    req.join()

    return delays
}

suspend fun insertDelay(delay: DelayInsert): Boolean {
    val url = "${appContextGlobal.get().url}/delays"
    val body = Json.encodeToString(delay)

    val (_, response, result) = Fuel.post(url)
        .header("Accept-Language", "en")
        .header(
            "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0"
        )
        .header(Headers.AUTHORIZATION, "Bearer ${appContextGlobal.get().token}")
        .header(Headers.CONTENT_TYPE, "application/json")
        .jsonBody(body)
        .responseString()


    return when (result) {
        is Result.Success -> {
            val statusCode = response.statusCode
            statusCode in 200..299 // Check for successful status code
        }

        is Result.Failure -> {
            throw IllegalStateException("Error Code: ${response.statusCode}")
        }
    }
}

suspend fun updateDelay(delay: DelayUpdate): Delay {
    val id = delay.id
    val url = "${appContextGlobal.get().url}/delays/${id}"
    val body = Json.encodeToString(delay)

    val (_, response, result) = Fuel.put(url)
        .header("Accept-Language", "en")
        .header(
            "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0"
        )
        .header(Headers.AUTHORIZATION, "Bearer ${appContextGlobal.get().token}")
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

suspend fun deleteDelay(id: String): Boolean {
    val url = "${appContextGlobal.get().url}/delays/${id}"

    val (_, response, result) = Fuel.delete(url)
        .header("Accept-Language", "en")
        .header(
            "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0"
        )
        .header(Headers.AUTHORIZATION, "Bearer ${appContextGlobal.get().token}")
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