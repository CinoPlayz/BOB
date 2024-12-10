package utils

import gui.login.logoutUserBackend
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.runBlocking
import models.*
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
class DelayAPITest {

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
    fun testInsertDealy() = runBlocking {
        // First: Prepare environment - Insert Stations and Route
        val station1 = StationInsert(
            name = "TestStation1",
            officialStationNumber = "12345",
            coordinates = Coordinates(lat = 46.55472F, lng = 15.64667F) // Maribor: 46.55472 15.64667
        )
        val station2 = StationInsert(
            name = "TestStation2",
            officialStationNumber = "12346",
            coordinates = Coordinates(lat = 46.55473F, lng = 15.64668F)
        )
        val station3 = StationInsert(
            name = "TestStation3",
            officialStationNumber = "12347",
            coordinates = Coordinates(lat = 46.55474F, lng = 15.64669F)
        )

        val insertStation1Success = utils.api.dao.insertStation(station1)
        val insertStation2Success = utils.api.dao.insertStation(station2)
        val insertStation3Success = utils.api.dao.insertStation(station3)

        assertTrue("Station1 insertion failed", insertStation1Success)
        assertTrue("Station2 insertion failed", insertStation2Success)
        assertTrue("Station3 insertion failed", insertStation3Success)

        val dateNow = LocalDateTime.now()
        val start = RouteStop(
            station = "TestStation1", // ID will be retrived by backend from common name
            time = "01:00"
        )
        val end = RouteStop(
            station = "TestStation2",
            time = "02:00"
        )
        val middle = RouteStop(
            station = "TestStation3",
            time = "01:30"
        )

        // Create new route
        val route = RouteInsert(
            trainType = "ICS",
            trainNumber = 99999,
            validFrom = dateNow.minusDays(1),
            validUntil = dateNow.plusDays(1),
            canSupportBikes = true,
            drivesOn = listOf(0, 1, 4, 6),
            start = start,
            end = end,
            middle = listOf(middle)
        )

        val insertRouteSuccess = utils.api.dao.insertRoute(route)

        assertTrue("Route insertion failed", insertRouteSuccess)

        // Get route and middleStation IDs
        val stations = utils.api.dao.getAllStations()
        val officialStationNumberToFind = "12347"
        val nextStation = stations.find { it.officialStationNumber == officialStationNumberToFind }

        val routes = utils.api.dao.getAllRoutes()
        val trainNumberToFind = 99999
        val insertedTestRoute = routes.find { it.trainNumber == trainNumberToFind }

        // Create new Delay
        val delay = DelayInsert(
            timeOfRequest = LocalDateTime.now(),
            route = insertedTestRoute!!.id,
            currentStation = nextStation!!.id,
            delay = 12
        )

        val insertDelaySuccess = utils.api.dao.insertDelay(delay)

        assertTrue("Delay insertion failed", insertDelaySuccess)
    }

    @Test
    @Order(3)
    fun testInsertDuplicateDelay() = runBlocking {
        // Test Stations and Route already in the database

        // Get route and middleStation IDs
        val stations = utils.api.dao.getAllStations()
        val officialStationNumberToFind = "12347" // middle
        val nextStation = stations.find { it.officialStationNumber == officialStationNumberToFind }

        val routes = utils.api.dao.getAllRoutes()
        val trainNumberToFind = 99999
        val insertedTestRoute = routes.find { it.trainNumber == trainNumberToFind }

        // Create new Delay
        val delay = DelayInsert(
            timeOfRequest = LocalDateTime.now(),
            route = insertedTestRoute!!.id,
            currentStation = nextStation!!.id,
            delay = 12
        )

        val insertDelaySuccess = utils.api.dao.insertDelay(delay) // must succeed

        assertTrue("Duplicate Delay insertion failed", insertDelaySuccess)
    }

    @Test
    @Order(4)
    fun getInsertedTestDelay(): Unit = runBlocking {

        // Find Delay using inserted test route ID
        val routes = utils.api.dao.getAllRoutes()
        val trainNumberToFind = 99999
        val insertedTestRoute = routes.find { it.trainNumber == trainNumberToFind }

        val delays = utils.api.dao.getAllDelays()

        assert(delays.isNotEmpty()) { "Delay list should not be empty" }

        val insertedTestDelay = delays.find { it.route == insertedTestRoute!!.id }
        assertEquals(insertedTestRoute!!.id, insertedTestDelay?.route, "Delay with route ID ${insertedTestRoute.id} not found")
    }

    @Test
    @Order(5)
    fun updateDelay() = runBlocking {
        // Find Delay using inserted test route ID
        val routes = utils.api.dao.getAllRoutes()
        val trainNumberToFind = 99999
        val insertedTestRoute = routes.find { it.trainNumber == trainNumberToFind }

        val delays = utils.api.dao.getAllDelays()

        assert(delays.isNotEmpty()) { "Delay list should not be empty" }

        val insertedTestDelay = delays.find { it.route == insertedTestRoute!!.id }

        val updatedDelay = DelayUpdate(
            id = insertedTestDelay!!.id,
            timeOfRequest = insertedTestDelay.timeOfRequest,
            route = insertedTestDelay.route,
            currentStation = insertedTestDelay.currentStation,
            delay = 24
        )

        val returnedDelay = utils.api.dao.updateDelay(updatedDelay)

        assertEquals(24, returnedDelay.delay, "Delay minutes must match")
    }

    @Test
    @Order(6)
    fun deleteDelay() = runBlocking {
        // Cleanup test route
        val routes = utils.api.dao.getAllRoutes()

        assert(routes.isNotEmpty()) { "Route list should not be empty" }
        val trainNumberToFind = 99999
        val insertedTestRoute = routes.find { it.trainNumber == trainNumberToFind }

        val deleteRouteSuccess = utils.api.dao.deleteRoute(insertedTestRoute!!.id)

        assertTrue("Route deletion failed", deleteRouteSuccess)

        // Cleanup test stations
        val stations = utils.api.dao.getAllStations()
        assert(stations.isNotEmpty()) { "Stations list should not be empty" }
        val insertedTestStation1 = stations.find { it.officialStationNumber == "12345" }
        val insertedTestStation2 = stations.find { it.officialStationNumber == "12346" }
        val insertedTestStation3 = stations.find { it.officialStationNumber == "12347" }

        val deleteStation1Success = utils.api.dao.deleteStation(insertedTestStation1!!.id)
        val deleteStation2Success = utils.api.dao.deleteStation(insertedTestStation2!!.id)
        val deleteStation3Success = utils.api.dao.deleteStation(insertedTestStation3!!.id)

        assertTrue("Station deletion failed", deleteStation1Success)
        assertTrue("Station deletion failed", deleteStation2Success)
        assertTrue("Station deletion failed", deleteStation3Success)

        // Delete delay
        val delays = utils.api.dao.getAllDelays()

        assert(delays.isNotEmpty()) { "Delay list should not be empty" }

        // Find and delete multiple = delay and duplicate delay
        val delaysToDelete = delays.filter { it.route == insertedTestRoute.id }

        delaysToDelete.forEach { delay ->
            val deleteDelaySuccess = utils.api.dao.deleteDelay(delay.id)
            assertTrue("Delay deletion failed for delay ID: ${delay.id}", deleteDelaySuccess)
        }
    }

    @Test
    @Order(7)
    fun logout() = runBlocking {
        logoutUserBackend()
    }
}