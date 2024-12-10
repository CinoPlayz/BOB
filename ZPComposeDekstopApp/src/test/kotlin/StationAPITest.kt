package utils

import gui.login.loginUser
import gui.login.logoutUserBackend
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.runBlocking
import models.Coordinates
import models.StationInsert
import models.StationUpdate
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.TestMethodOrder
import utils.context.appContextGlobal
import utils.context.initializeAppContext
import java.io.File
import kotlin.test.DefaultAsserter.assertTrue
import kotlin.test.Test
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.Assertions.assertEquals

@TestMethodOrder(MethodOrderer.OrderAnnotation::class) // Must be exectued in correct order
class StationAPITest {

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

        loginUser(
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
    fun testInsertStation() = runBlocking {
        val station = StationInsert(
            name = "Test Station",
            officialStationNumber = "12345",
            coordinates = Coordinates(lat = 46.55472F, lng = 15.64667F) // Maribor: 46.55472 15.64667
        )

        val insertSuccess = utils.api.dao.insertStation(station)

        assertTrue("Station insertion failed", insertSuccess)
    }

    @Test
    @Order(3)
    fun testInsertDuplicateStation(): Unit = runBlocking {
        val station = StationInsert(
            name = "Test Station",
            officialStationNumber = "12345",
            coordinates = Coordinates(lat = 46.55472F, lng = 15.64667F) // Maribor: 46.55472 15.64667
        )

        assertFailsWith<IllegalStateException>("Station insertion must fail") {
            utils.api.dao.insertStation(station)
        }
    }

    @Test
    @Order(4)
    fun getInsertedTestStation(): Unit = runBlocking {
        val stations = utils.api.dao.getAllStations()

        assert(stations.isNotEmpty()) { "Stations list should not be empty" }

        val officialStationNumberToFind = "12345"
        val insertedTestStation = stations.find { it.officialStationNumber == officialStationNumberToFind }
        assertEquals(officialStationNumberToFind, insertedTestStation?.officialStationNumber, "Station with officialStationNumber $officialStationNumberToFind not found")
    }

    @Test
    @Order(5)
    fun updateStation() = runBlocking {
        val stations = utils.api.dao.getAllStations()

        assert(stations.isNotEmpty()) { "Stations list should not be empty" }
        val officialStationNumberToFind = "12345"
        val insertedTestStation = stations.find { it.officialStationNumber == officialStationNumberToFind }

        val newName = "Test Station Modified"

        val updatedStation = StationUpdate(
            id = insertedTestStation!!.id,
            name = newName,
            officialStationNumber = insertedTestStation.officialStationNumber,
            coordinates = insertedTestStation.coordinates
        )

        val returnedStation = utils.api.dao.updateStation(updatedStation)

        assertEquals(newName, returnedStation.name, "Names must match")
    }

    @Test
    @Order(6)
    fun deleteStation() = runBlocking {
        val stations = utils.api.dao.getAllStations()

        assert(stations.isNotEmpty()) { "Stations list should not be empty" }
        val officialStationNumberToFind = "12345"
        val insertedTestStation = stations.find { it.officialStationNumber == officialStationNumberToFind }

        val success = utils.api.dao.deleteStation(insertedTestStation!!.id)

        assertTrue("Station deletion failed", success)
    }

    @Test
    @Order(7)
    fun logout() = runBlocking {
        logoutUserBackend()
    }
}