package gui.login

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.result.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import utils.context.appContextGlobal

private val json = Json { ignoreUnknownKeys = true }

@Serializable
data class LoginData(val username: String, val password: String)

@Serializable
data class LoginDataTwoFA(val loginToken: String, val otpCode: String)

@Serializable
data class LoginResponse(val message: String? = null, val token: String? = null, val loginToken: String? = null)

suspend fun loginUser(
    username: String,
    password: String,
    onSuccess: (String) -> Unit,
    onTwoFA: (String) -> Unit,
    onFailure: (String) -> Unit,
) {
    withContext(Dispatchers.IO) {
        val url = "${appContextGlobal.get().url}/users/login"
        val loginData = LoginData(username, password)
        val body = Json.encodeToString(loginData)

        val (_, response, result) = Fuel.post(url)
            .header("Accept-Language", "en")
            .header(
                "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0"
            )
            .header(Headers.CONTENT_TYPE, "application/json")
            .jsonBody(body)
            .responseString()

        //println(response.statusCode)
        //println(response.body())
        //println(response.body().asString("application/json"))

        when (result) {
            is Result.Success -> {
                val responseBody = result.get()
                val loginResponse = Json.decodeFromString<LoginResponse>(responseBody)
                if (loginResponse.token != null && loginResponse.loginToken == null) {
                    onSuccess(loginResponse.token)
                } else if (loginResponse.token == null && loginResponse.loginToken != null) {
                    onTwoFA(loginResponse.loginToken)
                } else {
                    onFailure("Internal Error")
                }
            }

            is Result.Failure -> {
                when (response.statusCode) {
                    -1 -> {
                        onFailure("Error: Backend Server Offline")
                    }

                    500 -> {
                        onFailure("Invalid credentials")
                    }

                    else -> {
                        onFailure("Error: ${response.statusCode}")
                    }
                }
            }
        }
    }
}

suspend fun loginUserTwoFA(
    loginToken: String,
    totp: String,
    onSuccess: (String) -> Unit,
    onFailure: (String) -> Unit
) {
    withContext(Dispatchers.IO) {
        val url = "${appContextGlobal.get().url}/users/twoFaLogin"
        val loginData = LoginDataTwoFA(loginToken, totp)
        val body = Json.encodeToString(loginData)

        val (_, response, result) = Fuel.post(url)
            .header("Accept-Language", "en")
            .header(
                "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0"
            )
            .header(Headers.CONTENT_TYPE, "application/json")
            .jsonBody(body)
            .responseString()

        when (result) {
            is Result.Success -> {
                val responseBody = result.get()
                val loginResponse = Json.decodeFromString<LoginResponse>(responseBody)
                if (loginResponse.token != null) {
                    onSuccess(loginResponse.token)
                } else {
                    onFailure("Internal Error")
                }
            }
            is Result.Failure -> {
                when (response.statusCode) {
                    -1 -> {
                        onFailure("Error: Backend Server Offline")
                    }

                    400 -> {
                        onFailure("Enter TOTP Code")
                    }

                    401 -> {
                        onFailure("Invalid TOTP Code")
                    }

                    else -> {
                        onFailure("Error: ${response.statusCode}")
                    }
                }
            }
        }
    }
}