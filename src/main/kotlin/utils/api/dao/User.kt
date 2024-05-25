package utils.api.dao

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import models.User
import models.UserInsert
import utils.context.appContextGlobal
import com.github.kittinunf.fuel.coroutines.awaitStringResult

import models.UserUpdate

private val json = Json { ignoreUnknownKeys = true }

suspend fun getAllUsers(): List<User> {
    val url = "${appContextGlobal.url}/users"

    val result = url
        .httpGet()
        .header("Accept-Language" to "en")
        .header(
            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0"
        )
        .header(Headers.AUTHORIZATION, "Bearer ${appContextGlobal.token}")
        .awaitStringResult()

    return when (result) {
        is Result.Failure -> {
            println("Could not get data from API: ${result.getException().message}")
            emptyList()
        }
        is Result.Success -> {
            json.decodeFromString(result.get())
        }
    }
}


suspend fun insertUser(user: UserInsert): Boolean {
    val url = "${appContextGlobal.url}/users/createFromApp"
    val body = Json.encodeToString(user)

    val (_, response, result) = Fuel.post(url)
        .header("Accept-Language", "en")
        .header(
            "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0"
        )
        .header(Headers.AUTHORIZATION, "Bearer ${appContextGlobal.token}")
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

suspend fun updateUser(user: UserUpdate): User {
    val id = user.id
    val url = "${appContextGlobal.url}/users/${id}/updateFromApp"
    val body = Json.encodeToString(user)

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

suspend fun deleteUser(id: String): Boolean {
    val url = "${appContextGlobal.url}/users/${id}"

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