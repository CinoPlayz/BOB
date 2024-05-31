package utils

import com.mongodb.ConnectionString
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import utils.context.appContextGlobal

object DatabaseUtil {
    /*suspend fun connectDB(): MongoClient? {
        val dbUrl = appContextGlobal.dbUri
        return withContext(Dispatchers.IO) {
            MongoClients.create(ConnectionString(dbUrl))
        }
    }*/

    suspend fun connectDB(): MongoClient? {
        val dbUrl = appContextGlobal.get().dbUri
        return withContext(Dispatchers.IO) {
            try {
                val client = MongoClients.create(dbUrl)
                // Perform a network operation to ensure the connection is valid
                client.listDatabaseNames().first() // This will throw an exception if the connection fails
                client
            } catch (e: Exception) {
                println("Error creating MongoClient: ${e.message}")
                e.printStackTrace()
                null
            }
        }
    }
}
