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
import gui.CustomDropdownMenu
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import models.Coordinates
import models.TrainLocHistoryInsert
import utils.api.dao.insertTrainLocHistory
import java.time.LocalDateTime

@Composable
fun AddTrain(
    modifier: Modifier = Modifier,
    titleFontSize: Int = 20
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TitleText(
            text = "Add new train location history to database",
            fontSize = titleFontSize,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        InputTrainData(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
    }
}

@Composable
fun InputTrainData(
    modifier: Modifier = Modifier,
) {
    var trainNumber by remember { mutableStateOf("") }
    var trainType by remember { mutableStateOf("") }
    var routeFrom by remember { mutableStateOf("") }
    var routeTo by remember { mutableStateOf("") }
    var routeStartTime by remember { mutableStateOf("") }
    var nextStation by remember { mutableStateOf("") }
    var delay by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }

    val trainTypes = listOf("LP", "ICS")

    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    val onReset: () -> Unit = {
        trainNumber = ""
        trainType = ""
        routeFrom = ""
        routeTo = ""
        routeStartTime = ""
        nextStation = ""
        delay = ""
        latitude = ""
        longitude = ""
    }

    val coroutineScope = rememberCoroutineScope() // writeTrainToDB

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(state = rememberScrollState())
    ) {
        OutlinedTextField(
            value = trainNumber,
            onValueChange = { trainNumber = it },
            label = { Text("Train Number") },
            modifier = Modifier.fillMaxWidth()
        )

        CustomDropdownMenu(
            label = "Choose train type",
            options = trainTypes,
            onSelectionChange = { selectedTrainType ->
                trainType = selectedTrainType
            }
        )

        OutlinedTextField(
            value = routeFrom,
            onValueChange = { routeFrom = it },
            label = { Text("Route From") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = routeTo,
            onValueChange = { routeTo = it },
            label = { Text("Route To") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = routeStartTime,
            onValueChange = { routeStartTime = it },
            label = { Text("Route Start Time (HH:mm)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = nextStation,
            onValueChange = { nextStation = it },
            label = { Text("Next Station") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = delay,
            onValueChange = { delay = it },
            label = { Text("Current Delay") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = latitude,
            onValueChange = { latitude = it },
            label = { Text("Train Latitude") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = longitude,
            onValueChange = { longitude = it },
            label = { Text("Train Longitude") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                coroutineScope.launch {
                    val feedback = writeTrainToDB(
                        trainNumber = trainNumber,
                        trainType = trainType,
                        routeFrom = routeFrom,
                        routeTo = routeTo,
                        routeStartTime = routeStartTime,
                        nextStation = nextStation,
                        delay = delay,
                        latitude = latitude,
                        longitude = longitude,
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
            Text("Write train to database")
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

suspend fun writeTrainToDB(
    trainNumber: String,
    trainType: String,
    routeFrom: String,
    routeTo: String,
    routeStartTime: String,
    nextStation: String,
    delay: String,
    latitude: String,
    longitude: String,
    onReset: () -> Unit
): String {

    if (trainNumber.isEmpty() ||
        trainType.isEmpty() ||
        routeFrom.isEmpty() ||
        routeTo.isEmpty() ||
        routeStartTime.isEmpty() ||
        nextStation.isEmpty() ||
        delay.isEmpty() ||
        latitude.isEmpty() ||
        longitude.isEmpty()
    ) {
        return "Please fill in all fields."
    }

    val trainNumberInt = trainNumber.toIntOrNull() ?: return "Train number must be an integer."
    val delayInt = delay.toIntOrNull() ?: return "Delay must be an integer."

    val coordinates: Coordinates
    try {
        coordinates = Coordinates(latitude.toFloat(), longitude.toFloat())
    } catch (e: IllegalArgumentException) {
        return "Please enter valid coordinates."
    }

    val timeOfRequest = LocalDateTime.now()

    val trainLocHistoryInsert = TrainLocHistoryInsert(
        timeOfRequest = timeOfRequest,
        trainType = trainType,
        trainNumber = trainNumberInt.toString(),
        routeFrom = routeFrom,
        routeTo = routeTo,
        routeStartTime = routeStartTime,
        nextStation = nextStation,
        delay = delayInt,
        coordinates = coordinates
    )

    return try {
        coroutineScope {
            insertTrainLocHistory(trainLocHistoryInsert)
        }
        onReset()
        "Station successfully written to the database."
    } catch (e: Exception) {
        "Error writing station to the database. ${e.message}"
    }
}