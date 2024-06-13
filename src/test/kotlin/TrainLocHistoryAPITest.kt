package utils

import gui.login.logoutUserBackend
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.runBlocking
import models.Coordinates
import models.TrainLocHistoryInsert
import models.TrainLocHistoryUpdate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.TestMethodOrder
import utils.context.appContextGlobal
import utils.context.initializeAppContext
import java.io.File
import java.time.LocalDateTime
import kotlin.test.DefaultAsserter.assertTrue
import kotlin.test.Test

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class TrainLocHistoryAPITest {

    private val projectDirectory = System.getProperty("user.dir")
    private val configFile = File("$projectDirectory/src/test/config/config.env")
    private val configSecrets = dotenv {
        directory = configFile.parent
        filename = configFile.name
    }

    private fun onLoginSuccess(token: String, username: String) {
        val currentContext = appContextGlobal.get()
        val updatedContext = currentContext.copy(token = token, username = username)
        appContextGlobal.set(updatedContext)
    }

    private fun loginUser() = runBlocking {
        initializeAppContext()

        val username = configSecrets["USERNAME_BASIC_LOGIN"] ?: error("Username not found in .env file")
        val password = configSecrets["PASSWORD_BASIC_LOGIN"] ?: error("Password not found in .env file")

        gui.login.loginUser(
            username,
            password,
            onSuccess = { token ->
                onLoginSuccess(token, username)
            },
            onTwoFA = {
                // Not testing TwoFA here
            },
            onFailure = {
                throw Exception("Login failed.")
            }
        )
    }

    @Test
    @Order(1)
    fun login() {
        loginUser()
    }

    @Test
    @Order(2)
    fun testInsertTLH() = runBlocking {
        val tlh = TrainLocHistoryInsert(
            timeOfRequest = LocalDateTime.now(),
            trainType = "ICS",
            trainNumber = "99999",
            routeFrom = "Celje",
            routeTo = "Maribor",
            routeStartTime = "12:00:00",
            nextStation = "Pragersko",
            delay = 12,
            coordinates = Coordinates(lat = 46.390667F, lng = 15.657164F) // Pragersko: 46.390665104 15.657164038
        )

        val insertSuccess = utils.api.dao.insertTrainLocHistory(tlh)

        assertTrue("TrainLocHistory insertion failed", insertSuccess)
    }

    @Test
    @Order(3)
    fun testInsertDuplicateTLH() = runBlocking {
        val tlh = TrainLocHistoryInsert(
            timeOfRequest = LocalDateTime.now(),
            trainType = "ICS",
            trainNumber = "99999",
            routeFrom = "Celje",
            routeTo = "Maribor",
            routeStartTime = "12:00:00",
            nextStation = "Pragersko",
            delay = 12,
            coordinates = Coordinates(lat = 46.390667F, lng = 15.657164F) // Pragersko: 46.390665104 15.657164038
        )

        val insertSuccess = utils.api.dao.insertTrainLocHistory(tlh) // must succeed

        assertTrue("Duplicate TrainLocHistory insertion failed", insertSuccess)
    }

    @Test
    @Order(4)
    fun getInsertedTLH(): Unit = runBlocking {
        val tlhs = utils.api.dao.getAllTrainLocHistories()

        assert(tlhs.isNotEmpty()) { "TrainLocHistory list should not be empty" }

        val trainNumberToFind = "99999"
        val insertedTestTLH = tlhs.find { it.trainNumber == trainNumberToFind }

        assertEquals(insertedTestTLH!!.trainNumber, trainNumberToFind, "TrainLocHistory with trainNumber $trainNumberToFind not found")
    }

    @Test
    @Order(5)
    fun updateTLH() = runBlocking {
        val tlhs = utils.api.dao.getAllTrainLocHistories()

        assert(tlhs.isNotEmpty()) { "TrainLocHistory list should not be empty" }

        val trainNumberToFind = "99999"
        val insertedTestTLH = tlhs.find { it.trainNumber == trainNumberToFind }

        val updateTLH = TrainLocHistoryUpdate(
            id = insertedTestTLH!!.id,
            timeOfRequest = insertedTestTLH.timeOfRequest!!,
            trainType = insertedTestTLH.trainType,
            trainNumber = insertedTestTLH.trainNumber,
            routeFrom = insertedTestTLH.routeFrom,
            routeTo = insertedTestTLH.routeTo,
            routeStartTime = insertedTestTLH.routeStartTime,
            nextStation = insertedTestTLH.nextStation,
            delay = 24,
            coordinates = insertedTestTLH.coordinates,
        )

        val returnedTLH = utils.api.dao.updateTrainLocHistory(updateTLH)

        assertEquals(24, returnedTLH.delay, "TrainLocHistory delay minutes must match")
    }

    @Test
    @Order(6)
    fun deleteTLHs() = runBlocking {
        val tlhs = utils.api.dao.getAllTrainLocHistories()
        assert(tlhs.isNotEmpty()) { "TrainLocHistory list should not be empty" }

        // Find and delete multiple = tlh and duplicate tlh
        val tlhsToDelete = tlhs.filter {it.trainNumber == "99999"}

        tlhsToDelete.forEach { tlh ->
            val deleteTLHSuccess = utils.api.dao.deleteTrainLocHistory(tlh.id)
            assertTrue("TrainLocHistory deletion failed for ID: ${tlh.id}", deleteTLHSuccess)
        }
    }

    @Test
    @Order(7)
    fun logout() = runBlocking {
        logoutUserBackend()
    }
}