package utils.api.dao

import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File

data class ApiContext(val url: String, val token: String)

// Lazy initialization: apiContextGlobal will be initialized only once, on its first usage.
val apiContextGlobal: ApiContext by lazy {
    runBlocking {
        readConfiguration() ?: throw IllegalStateException("Failed to initialize ApiContext")
    }
}

private suspend fun readConfiguration(): ApiContext? = withContext(Dispatchers.IO) {
    try {
        val projectDirectory = System.getProperty("user.dir")
        val configFile = File("$projectDirectory/config/secrets.env")

        if (!configFile.exists()) {
            throw IllegalArgumentException("Database connection config file not found.")
        }

        val configSecrets = dotenv {
            directory = configFile.parent
            filename = configFile.name
        }

        val url: String = configSecrets["API_BASE_URL"] ?: throw IllegalStateException("API_BASE_URL not found in configuration")
        val token: String = configSecrets["API_BASE_TOKEN"] ?: throw IllegalStateException("API_BASE_TOKEN not found in configuration")

        ApiContext(url, token)
    } catch (e: Exception) {
        println("Error reading database configuration: ${e.message}")
        null
    }
}