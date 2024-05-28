package gui.addData

import TitleText
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import models.Coordinates
import models.StationInsert
import utils.api.dao.insertStation

@Composable
fun AddStation(
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
            text = "Add new station to database",
            fontSize = titleFontSize,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        InputStationData(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
    }
}

@Composable
fun InputStationData(
    modifier: Modifier = Modifier
) {
    var stationName by remember { mutableStateOf("") }
    var officialStationNumber by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }

    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    val onReset: () -> Unit = {
        stationName = ""
        officialStationNumber = ""
        latitude = ""
        longitude = ""
    }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(state = rememberScrollState())
    ) {
        OutlinedTextField(
            value = stationName,
            onValueChange = { stationName = it },
            label = { Text("Station Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = officialStationNumber,
            onValueChange = { officialStationNumber = it },
            label = { Text("Official Station Number") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = latitude,
            onValueChange = { latitude = it },
            label = { Text("Station Latitude") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = longitude,
            onValueChange = { longitude = it },
            label = { Text("Station Longitude") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                coroutineScope.launch {
                    val feedback = writeStationToDB(
                        stationName = stationName,
                        officialStationNumber = officialStationNumber,
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

suspend fun writeStationToDB(
    stationName: String,
    officialStationNumber: String,
    latitude: String,
    longitude: String,
    onReset: () -> Unit
): String {

    if (stationName.isEmpty() ||
        officialStationNumber.isEmpty() ||
        latitude.isEmpty() ||
        longitude.isEmpty()
    ) {
        return "Please fill in all fields."
    }

    val coordinates: Coordinates
    try {
        coordinates = Coordinates(latitude.toFloat(), longitude.toFloat())
    } catch (e: IllegalArgumentException) {
        return "Please enter valid coordinates."
    }

    val officialStationNumberInt = officialStationNumber.toIntOrNull() ?: return "Official station number must be an integer."

    val stationInsert = StationInsert(
        name = stationName,
        officialStationNumber = officialStationNumberInt.toString(),
        coordinates = coordinates
    )

    return try {
        coroutineScope {
            insertStation(stationInsert)
        }
        onReset()
        "Station successfully written to the database."
    } catch (e: Exception) {
        "Error writing station to the database. ${e.message}"
    }
}