package utils.api.dao

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.result.Result
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import models.UserInsert

suspend fun insertUser(user: UserInsert): Boolean {
    val url = "${apiContextGlobal.url}/users"
    val body = Json.encodeToString(user)

    val (_, response, result) = Fuel.post(url)
        .header("Accept-Language", "en")
        .header(
            "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0"
        )
        .header(Headers.AUTHORIZATION, "Bearer ${apiContextGlobal.token}")
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