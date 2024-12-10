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
import kotlin.test.assertFailsWith

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class RouteAPITest {

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
    fun testInsertRoute() = runBlocking {
        // First insert 3 test stations
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

        val insertSuccess = utils.api.dao.insertRoute(route)

        assertTrue("Route insertion failed", insertSuccess)
    }

    @Test
    @Order(3)
    fun testInsertDuplicateRoute(): Unit = runBlocking {
        // Test Stations already in the database

        val dateNow = LocalDateTime.now()
        val start = RouteStop(
            station = "TestStation1",
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

        assertFailsWith<IllegalStateException>("Route insertion must fail") {
            utils.api.dao.insertRoute(route)
        }
    }

    @Test
    @Order(4)
    fun getInsertedTestRoute(): Unit = runBlocking {
        val routes = utils.api.dao.getAllRoutes()

        assert(routes.isNotEmpty()) { "Route list should not be empty" }

        val trainNumberToFind = 99999
        val insertedTestRoute = routes.find { it.trainNumber == trainNumberToFind }
        assertEquals(trainNumberToFind, insertedTestRoute?.trainNumber, "Route with trainNumber $trainNumberToFind not found")
    }

    @Test
    @Order(5)
    fun updateRoute() = runBlocking {
        val routes = utils.api.dao.getAllRoutes()

        assert(routes.isNotEmpty()) { "Route list should not be empty" }
        val trainNumberToFind = 99999
        val insertedTestRoute = routes.find { it.trainNumber == trainNumberToFind }

        val newTrainType = "LP"

        val updatedRoute = RouteUpdate(
            id = insertedTestRoute!!.id,
            trainType = newTrainType, // edit
            trainNumber = insertedTestRoute.trainNumber,
            validFrom = insertedTestRoute.validFrom,
            validUntil = insertedTestRoute.validUntil,
            canSupportBikes = false, // edit
            drivesOn = insertedTestRoute.drivesOn,
            start = insertedTestRoute.start,
            end = insertedTestRoute.end,
            middle = insertedTestRoute.middle,
            newMiddle = emptyList()
        )

        val returnedRoute = utils.api.dao.updateRoute(updatedRoute)

        assertEquals(newTrainType, returnedRoute.trainType, "Types must match")
    }

    @Test
    @Order(6)
    fun deleteRoute() = runBlocking {
        // Delete route
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
    }

    @Test
    @Order(7)
    fun logout() = runBlocking {
        logoutUserBackend()
    }
}