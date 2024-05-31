package gui.login

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.result.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import utils.context.appContextGlobal

suspend fun logoutUserBackend() {
    withContext(Dispatchers.IO) {
        val url = "${appContextGlobal.get().url}/users/token"

        val (_, response, result) = Fuel.delete(url)
            .header("Accept-Language", "en")
            .header(
                "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0"
            )
            .header(Headers.AUTHORIZATION, "Bearer ${appContextGlobal.get().token}")
            .header(Headers.CONTENT_TYPE, "application/json")
            .responseString()

        when (result) {
            is Result.Failure -> {
                val currentContext = appContextGlobal.get()
                val updatedContext = currentContext.copy(token = "")
                appContextGlobal.set(updatedContext)
            }

            is Result.Success -> {
                val currentContext = appContextGlobal.get()
                val updatedContext = currentContext.copy(token = "")
                appContextGlobal.set(updatedContext)
            }

            /*println(response.statusCode)
            println(response.body())
            println(response.body().asString("application/json"))*/
        }
    }
}