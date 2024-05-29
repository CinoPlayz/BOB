package gui.manageData

import TitleText
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gui.CustomDropdownMenu
import gui.CustomDropdownMenuInt
import gui.addLeadingZero
import gui.parseTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import models.*
import utils.api.dao.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ManageTrains(
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()

    var trains by remember { mutableStateOf<List<TrainLocHistory>>(emptyList()) }
    var allStations by remember { mutableStateOf<List<Station>>(emptyList()) } // for routeFrom/routeTo list
    var allStationsNames by remember { mutableStateOf<List<String>>(emptyList()) }
    var allRoutes by remember { mutableStateOf<List<Route>>(emptyList()) } // for route list
    var allRoutesNumbers by remember { mutableStateOf<List<Int>>(emptyList()) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val trainTypes = listOf(
        "AVT", "BUS", "EC", "EN", "IC", "ICS",
        "LP", "LPV", "LRG", "MO", "MV", "RG"
    )

    val isLoading = remember { mutableStateOf(true) }

    fun updateTrain(newTrain: TrainLocHistory) {
        trains = trains.map { if (it.id == newTrain.id) newTrain else it }
    }

    var trainToDelete by remember { mutableStateOf<TrainLocHistory?>(null) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    fun deleteTrain(train: TrainLocHistory) {
        trainToDelete = train
    }

    trainToDelete?.let { train ->
        AlertDialog(
            onDismissRequest = {
                trainToDelete = null
            },
            title = {
                Text(text = "Delete Train Location History")
            },
            text = {
                Text(text = "Are you sure you want to delete this Train Location History?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val feedback = deleteTrainFromDB(train.id) {
                                trains = trains.filterNot { it.id == train.id }
                            }
                            trainToDelete = null
                            feedbackMessage = feedback
                        }
                    }
                ) {
                    Text(text = "Yes")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        trainToDelete = null
                    }
                ) {
                    Text(text = "No")
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            isLoading.value = true
            try {
                allStations = withContext(Dispatchers.IO) { getAllStations() }
                allRoutes = withContext(Dispatchers.IO) { getAllRoutes() }
                allStationsNames = allStations.map { it.name }
                allRoutesNumbers = allRoutes.map { it.trainNumber }
                trains = withContext(Dispatchers.IO) { getAllTrainLocHistories() }
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
        if (errorMessage != null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Text("Failed to load train location histories: $errorMessage")
            }
        } else {
            val state = rememberLazyListState()

            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                if (trains.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            TitleText(
                                text = "*** NO TRAIN LOCATION HISTORIES ***",
                                fontSize = 20
                            )
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .padding(16.dp),
                    state = state
                ) {
                    items(trains) { train ->
                        TrainItem(
                            train = train,
                            trainTypes = trainTypes,
                            allStations = allStationsNames,
                            allRoutes = allRoutesNumbers,
                            onDeleteTrain = { trainToDelete -> deleteTrain(trainToDelete) },
                            onUpdateTrain = { trainToUpdate -> updateTrain(trainToUpdate) }
                        )
                    }
                }

                VerticalScrollbar(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight(),
                    adapter = rememberScrollbarAdapter(scrollState = state)
                )
            }
        }
    }

    feedbackMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { feedbackMessage = null },
            text = { Text(message) },
            confirmButton = {
                Button(
                    onClick = { feedbackMessage = null },
                ) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun TrainItem(
    train: TrainLocHistory,
    trainTypes: List<String>,
    allStations: List<String>, // name
    allRoutes: List<Int>, // trainNumber
    modifier: Modifier = Modifier,
    onDeleteTrain: (TrainLocHistory) -> Unit,
    onUpdateTrain: (TrainLocHistory) -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    var editMode by remember { mutableStateOf(false) }

    var requestYear by remember { mutableStateOf("${train.timeOfRequest?.year}") }
    var requestMonth by remember { mutableStateOf("${train.timeOfRequest?.monthValue}") }
    var requestDay by remember { mutableStateOf("${train.timeOfRequest?.dayOfMonth}") }
    var requestHour by remember { mutableStateOf("${train.timeOfRequest?.hour}") }
    var requestMinute by remember { mutableStateOf("${train.timeOfRequest?.minute}") }
    var requestSecond by remember { mutableStateOf("${train.timeOfRequest?.second}") }
    var dateTimeError by remember { mutableStateOf(false) }

    var trainType by remember { mutableStateOf(train.trainType) }
    var trainNumber by remember { mutableStateOf(train.trainNumber) }
    var routeFrom by remember { mutableStateOf(train.routeFrom) }
    var routeTo by remember { mutableStateOf(train.routeTo) }

    var routeStartTime by remember { mutableStateOf(train.routeStartTime) }
    var routeStartTimeHour by remember { mutableStateOf("") }
    var routeStartTimeMinute by remember { mutableStateOf("") }
    var routeStartTimeSecond by remember { mutableStateOf("") }
    var timeError by remember { mutableStateOf(false) }

    var nextStation by remember { mutableStateOf(train.nextStation) }

    var delayMinutes by remember { mutableStateOf<Int?>(train.delay) }

    var coordinates by remember { mutableStateOf(train.coordinates) }
    var longitude by remember { mutableStateOf(coordinates.lng) }
    var longitudeText by remember { mutableStateOf(TextFieldValue(longitude.toString())) }
    var latitude by remember { mutableStateOf(coordinates.lat) }
    var latitudeText by remember { mutableStateOf(TextFieldValue(latitude.toString())) }

    var createdAt by remember { mutableStateOf(train.createdAt) }
    var updatedAt by remember { mutableStateOf(train.updatedAt) }

    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(train) {
        requestYear = train.timeOfRequest?.year.toString()
        requestMonth = train.timeOfRequest?.monthValue.toString()
        requestDay = train.timeOfRequest?.dayOfMonth.toString()
        requestHour = train.timeOfRequest?.hour.toString()
        requestMinute = train.timeOfRequest?.minute.toString()
        requestSecond = train.timeOfRequest?.second.toString()
        trainType = train.trainType
        trainNumber = train.trainNumber
        routeFrom = train.routeFrom
        routeTo = train.routeTo
        routeStartTime = train.routeStartTime
        val (hours, minutes, seconds) = parseTime(routeStartTime)
        routeStartTimeHour = hours
        routeStartTimeMinute = minutes
        routeStartTimeSecond = seconds
        nextStation = train.nextStation
        delayMinutes = train.delay
        coordinates = train.coordinates
        longitude = train.coordinates.lng
        latitude = train.coordinates.lat
        longitudeText = TextFieldValue(train.coordinates.lng.toString())
        latitudeText = TextFieldValue(train.coordinates.lat.toString())
        createdAt = train.createdAt
        updatedAt = train.updatedAt
    }

    val onUpdateTrainSuccess: (TrainLocHistory) -> Unit = { updatedTrain ->
        onUpdateTrain(updatedTrain)
        editMode = false
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

    fun updateTimeError() {
        try {
            val newHour = routeStartTimeHour.toIntOrNull()
            val newMinute = routeStartTimeMinute.toIntOrNull()
            val newSecond = routeStartTimeSecond.toIntOrNull()

            timeError = when {
                newHour == null || newHour !in 0..23 -> true
                newMinute == null || newMinute !in 0..59 -> true
                newSecond == null || newSecond !in 0..59 -> true
                else -> false
            }
        } catch (e: Exception) {
            timeError = true
        }
    }

    val coroutineScope = rememberCoroutineScope()

    Surface(
        modifier = modifier.padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        elevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.BottomEnd
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                if (editMode) {
                    Text("Time of Request")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
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
                    CustomDropdownMenu(
                        label = "Choose train type",
                        options = trainTypes,
                        initialSelection = trainType,
                        onSelectionChange = { selectedTrainType ->
                            trainType = selectedTrainType
                        }
                    )

                    CustomDropdownMenuInt(
                        label = "Choose Train Number",
                        options = allRoutes,
                        initialSelection = trainNumber.toInt(),
                        onSelectionChange = { selectedTrainNumber ->
                            trainNumber = selectedTrainNumber.toString()
                        }
                    )

                    CustomDropdownMenu(
                        label = "Choose Departure Station",
                        options = allStations,
                        initialSelection = routeFrom,
                        onSelectionChange = { selectedRouteFrom ->
                            routeFrom = selectedRouteFrom
                        }
                    )

                    CustomDropdownMenu(
                        label = "Choose Destination Station",
                        options = allStations,
                        initialSelection = routeTo,
                        onSelectionChange = { selectedRouteTo ->
                            routeTo = selectedRouteTo
                        }
                    )

                    Text("Route Departure Time")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = routeStartTimeHour,
                            onValueChange = { newRouteStartTimeHour ->
                                routeStartTimeHour = newRouteStartTimeHour.take(2)
                                updateTimeError()
                            },
                            isError = timeError,
                            label = { Text("HH") },
                            modifier = Modifier.weight(1f),
                        )
                        Text(":", modifier = Modifier.align(Alignment.CenterVertically))
                        OutlinedTextField(
                            value = routeStartTimeMinute,
                            onValueChange = { newRouteStartTimeMinute ->
                                routeStartTimeMinute = newRouteStartTimeMinute.take(2)
                                updateTimeError()
                            },
                            isError = timeError,
                            label = { Text("MM") },
                            modifier = Modifier.weight(1f),
                        )
                        Text(":", modifier = Modifier.align(Alignment.CenterVertically))
                        OutlinedTextField(
                            value = routeStartTimeSecond,
                            onValueChange = { newRouteStartTimeSecond ->
                                routeStartTimeSecond = newRouteStartTimeSecond.take(2)
                                updateTimeError()
                            },
                            isError = timeError,
                            label = { Text("SS") },
                            modifier = Modifier.weight(1f),
                        )
                    }

                    CustomDropdownMenu(
                        label = "Choose Upcoming Station",
                        options = allStations,
                        initialSelection = nextStation,
                        onSelectionChange = { selectedNextStation ->
                            nextStation = selectedNextStation
                        }
                    )

                    OutlinedTextField(
                        value = delayMinutes?.toString() ?: "",
                        onValueChange = { newValue ->
                            delayMinutes = newValue.toIntOrNull() // null - used for check when updating
                        },
                        label = { Text("Train Delay (minutes)") },
                        modifier = Modifier
                            .fillMaxWidth(0.8f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = latitudeText,
                        onValueChange = {
                            latitudeText = it
                            latitude = it.text.toFloatOrNull()
                        },
                        label = { Text("Current Train Coordinates - Latitude") },
                        modifier = Modifier
                            .fillMaxWidth(0.8f),
                        singleLine = true,
                        isError = latitude == null
                    )
                    OutlinedTextField(
                        value = longitudeText,
                        onValueChange = {
                            longitudeText = it
                            longitude = it.text.toFloatOrNull() // ?: 0f // to float or null
                        },
                        label = { Text("Current Train Coordinates - Longitude") },
                        modifier = Modifier
                            .fillMaxWidth(0.8f),
                        singleLine = true,
                        isError = longitude == null
                    )

                } else {
                    Text("Time of Request: ${train.timeOfRequest?.format(formatter)}")
                    Text("Train Type: ${train.trainType}")
                    Text("Train Number: ${train.trainNumber}")
                    Text("Departure Station: ${train.routeFrom}")
                    Text("Destination Station: ${train.routeTo}")
                    Text("Route Departure Time: ${train.routeStartTime}")
                    Text("Upcoming Station: ${train.nextStation}")
                    Text("Train Delay (minutes): ${train.delay}")
                    Text("Current Train Coordinates:")
                    Text(
                        text = "Latitude: ${train.coordinates.lat}",
                        modifier = Modifier.padding(start = 16.dp)
                    )
                    Text(
                        text = "Longitude: ${train.coordinates.lng}",
                        modifier = Modifier.padding(start = 16.dp)
                    )
                    Text("TrainLocHistory Created On: ${train.createdAt.plusHours(2).format(formatter)}", fontSize = 12.sp)
                    Text("TrainLocHistory Last Updated On: ${train.updatedAt.plusHours(2).format(formatter)}", fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = {
                        if (editMode) {
                            coroutineScope.launch {
                                val feedback = updateTrainInDB(
                                    train = train,
                                    requestYear = requestYear,
                                    requestMonth = requestMonth,
                                    requestDay = requestDay,
                                    requestHour = requestHour,
                                    requestMinute = requestMinute,
                                    requestSecond = requestSecond,
                                    trainType = trainType,
                                    trainNumber = trainNumber,
                                    routeFrom = routeFrom,
                                    routeTo = routeTo,
                                    routeStartTimeHour = routeStartTimeHour,
                                    routeStartTimeMinute = routeStartTimeMinute,
                                    routeStartTimeSecond = routeStartTimeSecond,
                                    nextStation = nextStation,
                                    latitude = latitude,
                                    longitude = longitude,
                                    delayMinutes = delayMinutes,
                                    onSuccess = onUpdateTrainSuccess
                                )

                                feedbackMessage = feedback
                            }
                        } else {
                            editMode = true
                        }
                    },
                ) {
                    if (editMode) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    } else {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                }

                if (editMode) {
                    IconButton(
                        onClick = {
                            // Reset input fields to initial values
                            requestYear = train.timeOfRequest?.year.toString()
                            requestMonth = train.timeOfRequest?.monthValue.toString()
                            requestDay = train.timeOfRequest?.dayOfMonth.toString()
                            requestHour = train.timeOfRequest?.hour.toString()
                            requestMinute = train.timeOfRequest?.minute.toString()
                            requestSecond = train.timeOfRequest?.second.toString()
                            trainType = train.trainType
                            trainNumber = train.trainNumber
                            routeFrom = train.routeFrom
                            routeTo = train.routeTo
                            routeStartTime = train.routeStartTime
                            val (hours, minutes, seconds) = parseTime(routeStartTime)
                            routeStartTimeHour = hours
                            routeStartTimeMinute = minutes
                            routeStartTimeSecond = seconds
                            nextStation = train.nextStation
                            delayMinutes = train.delay
                            coordinates = train.coordinates
                            longitude = coordinates.lng
                            longitudeText = TextFieldValue(longitude.toString())
                            latitude = coordinates.lat
                            latitudeText = TextFieldValue(latitude.toString())
                            createdAt = train.createdAt
                            updatedAt = train.updatedAt
                            editMode = false
                        }
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = "Cancel")
                    }
                }

                if (!editMode) {
                    IconButton(
                        onClick = {
                            onDeleteTrain(train)
                        }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
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

suspend fun deleteTrainFromDB(
    id: String,
    onSuccess: () -> Unit
): String {
    return try {
        coroutineScope {
            deleteTrainLocHistory(id)
        }
        onSuccess()
        "Train location history successfully deleted from the database."
    } catch (e: Exception) {
        "Error removing train location history from the database. ${e.message}"
    }
}

suspend fun updateTrainInDB(
    train: TrainLocHistory,
    requestYear: String,
    requestMonth: String,
    requestDay: String,
    requestHour: String,
    requestMinute: String,
    requestSecond: String,
    trainType: String,
    trainNumber: String,
    routeFrom: String,
    routeTo: String,
    routeStartTimeHour: String,
    routeStartTimeMinute: String,
    routeStartTimeSecond: String,
    nextStation: String,
    latitude: Float?,
    longitude: Float?,
    delayMinutes: Int?,
    onSuccess: (TrainLocHistory) -> Unit
): String {
    var newRequestTimeStamp: LocalDateTime
    try {
        newRequestTimeStamp = LocalDateTime.of(
            requestYear.toInt(),
            requestMonth.toInt(),
            requestDay.toInt(),
            requestHour.toInt(),
            requestMinute.toInt(),
            requestSecond.toInt()
        )
        newRequestTimeStamp = newRequestTimeStamp.plusHours(2)
    } catch (e: IllegalArgumentException) {
        return ("Time of Request Format Invalid.")
    }

    if (delayMinutes == null) {
        return ("Train Delay Format Invalid.")
    }

    if (trainType.isEmpty() ||
        trainNumber.isEmpty() ||
        routeFrom.isEmpty() ||
        routeTo.isEmpty() ||
        routeStartTimeHour.isEmpty() ||
        routeStartTimeMinute.isEmpty() ||
        routeStartTimeSecond.isEmpty() ||
        nextStation.isEmpty()
    ) {
        return ("Please fill in all fields.")
    }

    if (latitude == null || longitude == null) {
        return ("Please check Latitude and Longitude fields.")
    }

    val newCoordinates = Coordinates(
        lat = latitude,
        lng = longitude
    )

    val hourStart = routeStartTimeHour.toIntOrNull()
    val minuteStart = routeStartTimeMinute.toIntOrNull()
    val secondStart = routeStartTimeSecond.toIntOrNull()
    if (hourStart == null || hourStart !in 0..23) {
        return "Invalid Route Departure Time: $routeStartTimeHour"
    }
    if (minuteStart == null || minuteStart !in 0..59) {
        return "Invalid Route Departure Time: $routeStartTimeMinute"
    }
    if (secondStart == null || secondStart !in 0..59) {
        return "Invalid Route Departure Time: $routeStartTimeMinute"
    }

    val routeStartTime = "${addLeadingZero(hourStart.toString())}:${addLeadingZero(minuteStart.toString())}:${addLeadingZero(secondStart.toString())}"

    val trainLocHistoryUpdate = TrainLocHistoryUpdate(
        id = train.id,
        timeOfRequest = newRequestTimeStamp,
        trainType = trainType,
        trainNumber = trainNumber,
        routeFrom = routeFrom,
        routeTo = routeTo,
        routeStartTime = routeStartTime,
        nextStation = nextStation,
        delay = delayMinutes,
        coordinates = newCoordinates
    )

    return try {
        var updatedTrainLocHistory: TrainLocHistory
        coroutineScope {
            updatedTrainLocHistory = updateTrainLocHistory(trainLocHistoryUpdate)
        }
        onSuccess(updatedTrainLocHistory)
        "Train location history successfully updated in the database."
    } catch (e: Exception) {
        "Error updating train location history in the database. ${e.message}"
    }
}