package utils

import com.mongodb.ConnectionString
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import utils.context.appContextGlobal

object DatabaseUtil {
    suspend fun connectDB(): MongoClient? {
        val dbUrl = appContextGlobal.dbUri
        return withContext(Dispatchers.IO) {
            MongoClients.create(ConnectionString(dbUrl))
        }
    }
}
