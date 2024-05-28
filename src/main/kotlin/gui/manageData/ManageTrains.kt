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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import models.Delay
import models.Route
import models.Station
import models.TrainLocHistory
import utils.api.dao.getAllTrainLocHistories
import utils.api.dao.getAllRoutes
import utils.api.dao.getAllStations
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ManageTrains(
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()

    var trains by remember { mutableStateOf<List<TrainLocHistory>>(emptyList()) }
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
    modifier: Modifier = Modifier,
    onDeleteDelay: (Delay) -> Unit,
    onUpdateDelay: (Delay) -> Unit
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

    val coroutineScope = rememberCoroutineScope()

    Surface(
        modifier = modifier.padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        elevation = 4.dp
    ) {
        
    }

    TODO()
}