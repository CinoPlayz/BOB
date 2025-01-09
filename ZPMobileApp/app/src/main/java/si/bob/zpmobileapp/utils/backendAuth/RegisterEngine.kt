package si.bob.zpmobileapp.utils.backendAuth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import si.bob.zpmobileapp.MyApp
import java.net.HttpURLConnection
import java.net.URL

suspend fun registerUser(
    app: MyApp,
    email: String,
    username: String,
    password: String,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    val baseUrl = app.sharedPrefs.getString(MyApp.BACKEND_URL_KEY, null)

    if (baseUrl.isNullOrEmpty()) {
        onFailure("Backend URL not configured.")
        return
    }

    val registerUrl = "$baseUrl/users"

    withContext(Dispatchers.IO) {
        try {
            val url = URL(registerUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            // Prepare JSON body
            val requestBody = JSONObject().apply {
                put("email", email)
                put("username", username)
                put("password", password)
            }

            // Write request body
            connection.outputStream.use { outputStream ->
                outputStream.write(requestBody.toString().toByteArray())
            }

            // Check response code
            if (connection.responseCode == HttpURLConnection.HTTP_CREATED) {
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } else {
                val errorStream = connection.errorStream
                val errorMessage = errorStream?.bufferedReader()?.readText() ?: "Unknown error occurred"
                withContext(Dispatchers.Main) {
                    onFailure(errorMessage)
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onFailure("Error: ${e.message}")
            }
        }
    }
}