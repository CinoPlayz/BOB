package utils.api.dao

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.result.Result
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import models.Route
import models.RouteInsert
import utils.context.appContextGlobal

private val json = Json { ignoreUnknownKeys = true }

fun getAllRoutes(): List<Route> {
    var routes = mutableListOf<Route>()
    val req = Fuel.get("${appContextGlobal.url}/routes")
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
                    val getRoutes = json.decodeFromString<Array<Route>>(result.value)
                    routes = getRoutes.toMutableList()
                }

            }
        }

    //Blocking
    req.join()


    return routes
}

suspend fun insertRoute(route: RouteInsert): Boolean {
    val url = "${appContextGlobal.url}/routes"
    val body = Json.encodeToString(route)

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
            println(response.body().toString())
            throw IllegalStateException("Error Code: ${response.statusCode}")
        }
    }
}