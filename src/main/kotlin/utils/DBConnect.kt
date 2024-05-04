package utils

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.mongodb.ConnectionString

object DatabaseUtil {
    private suspend fun readConfiguration(): String? = withContext(Dispatchers.IO) {
        try {
            val configDBenv = dotenv {
                directory = "config/"
                filename = "database.env"
            }
            val dbUrl: String? = configDBenv["DBURL"]
            dbUrl
        } catch (e: Exception) {
            println("Error reading database configuration: ${e.message}")
            null
        }
    }

    suspend fun connectDB(): MongoClient? {
        val dbUrl = readConfiguration()
        return if (dbUrl != null) {
            withContext(Dispatchers.IO) {
                MongoClients.create(ConnectionString(dbUrl))
            }
        } else {
            println("Failed to read database configuration.")
            throw IllegalArgumentException("Failed to read database configuration.")
        }
    }
}
