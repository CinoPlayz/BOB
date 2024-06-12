package utils

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import utils.DatabaseUtil.connectDB
import utils.context.initializeAppContext

class ConnectDBUtilTest {
    @Test
    @DisplayName("Test MongoDB Connection")
    fun testMongoDBConnection() = runTest { // for local database
        initializeAppContext()
        val mongoClient = connectDB()

        val database = mongoClient!!.getDatabase("ZP")

        val collection = database.getCollection("users")

        val documents = collection.find()

        /*var documentCount = 0
        for (document in documents) {
            println(document.toJson())
            documentCount++
        }

        val expectedCount = 2 // number of users in the database
        assertEquals(expectedCount, documentCount)*/

        var documentExists = false
        for (document in documents) {
            println(document.toJson())
            documentExists = true
        }

        // At least one document is returned
        assertTrue(documentExists, "Expected to find at least one document in the collection")

        mongoClient.close()
    }
}