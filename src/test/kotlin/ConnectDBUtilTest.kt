package utils

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import utils.DatabaseUtil.connectDB

class ConnectDBUtilTest {
    @Test
    @DisplayName("Test MongoDB Connection")
    fun testMongoDBConnection() = runTest { // for local database
        val mongoClient = connectDB()

        val database = mongoClient!!.getDatabase("vaja3")

        val collection = database.getCollection("logs")

        val documents = collection.find()

        var documentCount = 0
        for (document in documents) {
            println(document.toJson())
            documentCount++
        }

        val expectedCount = 24
        assertEquals(expectedCount, documentCount)

        mongoClient.close()
    }
}