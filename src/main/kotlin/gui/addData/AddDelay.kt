package gui.addData

import TitleText
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import gui.RoutesDropdownMenu
import gui.StationsDropdownMenu
import gui.toNameIDPairs
import gui.toNumberIDPairs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import models.DelayInsert
import models.Route
import models.Station
import utils.api.dao.*
import java.time.LocalDateTime

@Composable
fun AddDelay(
    modifier: Modifier = Modifier,
    titleFontSize: Int = 20
) {
    val coroutineScope = rememberCoroutineScope()
    val isLoading = remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var allStations by remember { mutableStateOf<List<Station>>(emptyList()) } // for currentStation list
    var allStationsPairs by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var allRoutes by remember { mutableStateOf<List<Route>>(emptyList()) } // for route list
    var allRoutesPairs by remember { mutableStateOf<List<Pair<Int, String>>>(emptyList()) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            isLoading.value = true
            try {
                allStations = withContext(Dispatchers.IO) { getAllStations() }
                allRoutes = withContext(Dispatchers.IO) { getAllRoutes() }
                allStationsPairs = allStations.toNameIDPairs()
                allRoutesPairs = allRoutes.toNumberIDPairs()
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }
    if (isLoading.value) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .padding(16.dp)
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TitleText(
                text = "Add new delay to database",
                fontSize = titleFontSize,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            InputDelayData(
                allStations = allStationsPairs,
                allRoutes = allRoutesPairs,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
        }
    }
}

@Composable
fun InputDelayData(
    allStations: List<Pair<String, String>>, // name, id
    allRoutes: List<Pair<Int, String>>, // trainNumber, id
    modifier: Modifier = Modifier
) {
    var requestYear by remember { mutableStateOf("") }
    var requestMonth by remember { mutableStateOf("") }
    var requestDay by remember { mutableStateOf("") }
    var requestHour by remember { mutableStateOf("") }
    var requestMinute by remember { mutableStateOf("") }
    var requestSecond by remember { mutableStateOf("") }
    var dateTimeError by remember { mutableStateOf(false) }

    var selectedRoute by remember { mutableStateOf("") } // id
    var selectedCurrentStation by remember { mutableStateOf("") } // id

    var delayMinutes by remember { mutableStateOf<Int?>(null) }

    val coroutineScope = rememberCoroutineScope()
    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    val onReset: () -> Unit = {
        requestYear = ""
        requestMonth = ""
        requestDay = ""
        requestHour = ""
        requestMinute = ""
        requestSecond = ""
        dateTimeError = false
        selectedRoute = ""
        selectedCurrentStation = ""
        delayMinutes = null
    }

    fun updateDateTimeError() {
        try {
            val newYear = requestYear.toIntOrNull()
            val newMonth = requestMonth.toIntOrNull()
            val newDay = requestDay.toIntOrNull()
            val newHour = requestHour.toIntOrNull()
            val newMinute = requestMinute.toIntOrNull()
            val newSecond = requestSecond.toIntOrNull()

            if (newYear != null && newMonth != null && newDay != null && newHour != null && newMinute != null && newSecond != null) {
                LocalDateTime.of(newYear, newMonth, newDay, newHour, newMinute, newSecond)
                dateTimeError = false
            } else {
                dateTimeError = true
            }
        } catch (e: Exception) {
            dateTimeError = true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(state = rememberScrollState())
    ) {
        Text("Time of Request")
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = requestYear,
                onValueChange = { newYearString ->
                    requestYear = newYearString.take(4) // Limit input to 4 characters
                    updateDateTimeError()
                },
                isError = dateTimeError,
                label = { Text("YYYY") },
                modifier = Modifier.weight(1f),
            )
            Text("-", modifier = Modifier.align(Alignment.CenterVertically))
            OutlinedTextField(
                value = requestMonth,
                onValueChange = { newMonthString ->
                    requestMonth = newMonthString.take(2) // Limit input to 2 characters
                    updateDateTimeError()
                },
                isError = dateTimeError,
                label = { Text("MM") },
                modifier = Modifier.weight(1f),
            )
            Text("-", modifier = Modifier.align(Alignment.CenterVertically))
            OutlinedTextField(
                value = requestDay,
                onValueChange = { newDayString ->
                    requestDay = newDayString.take(2) // Limit input to 2 characters
                    updateDateTimeError()
                },
                isError = dateTimeError,
                label = { Text("DD") },
                modifier = Modifier.weight(1f),
            )
            Text(" ", modifier = Modifier.align(Alignment.CenterVertically))
            OutlinedTextField(
                value = requestHour,
                onValueChange = { newHourString ->
                    requestHour = newHourString.take(2) // Limit input to 2 characters
                    updateDateTimeError()
                },
                isError = dateTimeError,
                label = { Text("HH") },
                modifier = Modifier.weight(1f),
            )
            Text(":", modifier = Modifier.align(Alignment.CenterVertically))
            OutlinedTextField(
                value = requestMinute,
                onValueChange = { newMinuteString ->
                    requestMinute = newMinuteString.take(2) // Limit input to 2 characters
                    updateDateTimeError()
                },
                isError = dateTimeError,
                label = { Text("MM") },
                modifier = Modifier.weight(1f),
            )
            Text(":", modifier = Modifier.align(Alignment.CenterVertically))
            OutlinedTextField(
                value = requestSecond,
                onValueChange = { newSecondString ->
                    requestSecond = newSecondString.take(2) // Limit input to 2 characters
                    updateDateTimeError()
                },
                isError = dateTimeError,
                label = { Text("SS") },
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(8.dp))
        Text("Route Number")
        RoutesDropdownMenu(
            label = "Route Number",
            options = allRoutes,
            originalSelection = selectedRoute,
            onSelectionChange = { selectedNewRoute ->
                selectedRoute = selectedNewRoute
            }
        )
        Spacer(Modifier.height(8.dp))
        Text("Current Station")
        StationsDropdownMenu(
            label = "Current Station",
            options = allStations,
            originalSelection = selectedCurrentStation,
            onSelectionChange = { selectedNewStation ->
                selectedCurrentStation = selectedNewStation
            }
        )
        Spacer(Modifier.height(8.dp))
        Text("Train Delay (in minutes)")
        OutlinedTextField(
            value = delayMinutes?.toString() ?: "",
            onValueChange = { newValue ->
                delayMinutes = newValue.toIntOrNull() // null - used for check when updating
            },
            label = { Text("Train Delay (minutes)") },
            modifier = Modifier
                .fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    val feedback = writeDelayToDB(
                        requestYear = requestYear,
                        requestMonth = requestMonth,
                        requestDay = requestDay,
                        requestHour = requestHour,
                        requestMinute = requestMinute,
                        requestSecond = requestSecond,
                        stationId = selectedCurrentStation,
                        routeId = selectedRoute,
                        delayMinutes = delayMinutes,
                        onReset = onReset // Pass the reset callback
                    )

                    // Update feedback message
                    feedbackMessage = feedback
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            Text("Write station to database")
        }
    }

    feedbackMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { feedbackMessage = null }, // Reset feedback message on dismiss
            //title = { Text("Feedback") },
            text = { Text(message) },
            confirmButton = {
                Button(
                    onClick = { feedbackMessage = null }, // Reset feedback message on confirm
                ) {
                    Text("OK")
                }
            }
        )
    }
}

suspend fun writeDelayToDB(
    requestYear: String,
    requestMonth: String,
    requestDay: String,
    requestHour: String,
    requestMinute: String,
    requestSecond: String,
    stationId: String,
    routeId: String,
    delayMinutes: Int?,
    onReset: () -> Unit
): String {

    if (requestYear.isEmpty() || requestMonth.isEmpty() || requestDay.isEmpty() || requestHour.isEmpty() || requestMinute.isEmpty() || requestSecond.isEmpty()) {
        return ("Time of Request Format Invalid.")
    }

    if (stationId.isEmpty() || routeId.isEmpty()) {
        return ("Please select Current Station and/or Train Number.")
    }

    if (delayMinutes == null) {
        return ("Train Delay Format Invalid.")
    }

    var requestTimeStamp: LocalDateTime
    try {
        requestTimeStamp = LocalDateTime.of(
            requestYear.toInt(),
            requestMonth.toInt(),
            requestDay.toInt(),
            requestHour.toInt(),
            requestMinute.toInt(),
            requestSecond.toInt()
        )
        requestTimeStamp = requestTimeStamp.plusHours(2)
    } catch (e: IllegalArgumentException) {
        return ("Time of Request Format Invalid.")
    }

    val delayInsert = DelayInsert(
        timeOfRequest = requestTimeStamp,
        route = routeId,
        currentStation = stationId,
        delay = delayMinutes
    )

    return try {
        coroutineScope {
            insertDelay(delayInsert)
        }
        onReset()
        "Delay successfully written to the database."
    } catch (e: Exception) {
        "Error writing delay to the database. ${e.message}"
    }
}