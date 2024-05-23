package utils.api.dao

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import kotlinx.serialization.json.Json
import models.Route

private val json = Json { ignoreUnknownKeys = true }

fun getAllRoutes(apiContext: ApiContext): List<Route> {
    var routes = mutableListOf<Route>()
    val req = Fuel.get("${apiContext.url}/routes")
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
                    val getRoutes = json.decodeFromString<Array<Route>>(result.value)
                    routes = getRoutes.toMutableList()
                }

            }
        }

    //Blocking
    req.join()


    return routes
}