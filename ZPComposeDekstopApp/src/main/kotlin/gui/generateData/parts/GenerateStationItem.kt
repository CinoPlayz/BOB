package gui.generateData.parts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import gui.scraper.insertStationFromScrapedListToDB
import gui.scraper.updateStationInScrapedList
import kotlinx.coroutines.launch
import models.StationInsert

@Composable
fun StationGenerateItem(
    station: StationInsert,
    index: Int,
    modifier: Modifier = Modifier,
    onInsertStation: (Boolean, Int) -> Unit, // Int = index
    onUpdateStation: (StationInsert, Int) -> Unit, // Int = index
    onRemoveStation: (Int) -> Unit // index
) {
    var editMode by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf(station.name) }
    var officialStationNumber by remember { mutableStateOf(station.officialStationNumber) }

    var coordinates by remember { mutableStateOf(station.coordinates) }
    var longitude by remember { mutableStateOf(coordinates.lng) }
    var longitudeText by remember { mutableStateOf(TextFieldValue(longitude.toString())) }
    var latitude by remember { mutableStateOf(coordinates.lat) }
    var latitudeText by remember { mutableStateOf(TextFieldValue(latitude.toString())) }

    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(station) {
        name = station.name
        officialStationNumber = station.officialStationNumber
        coordinates = station.coordinates
        longitude = station.coordinates.lng
        latitude = station.coordinates.lat
        longitudeText = TextFieldValue(station.coordinates.lng.toString())
        latitudeText = TextFieldValue(station.coordinates.lat.toString())
    }

    val onUpdateStationSuccess: (StationInsert, Int) -> Unit = { updatedStation, index ->
        onUpdateStation(updatedStation, index)
        editMode = false
    }

    val onInsertStationSuccess: (Boolean, Int) -> Unit = { success, index ->
        onInsertStation(success, index)
        editMode = false
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
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Station Name") },
                        modifier = Modifier
                            .fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = officialStationNumber,
                        onValueChange = { officialStationNumber = it },
                        label = { Text("Official Station Number") },
                        modifier = Modifier
                            .fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = latitudeText,
                        onValueChange = {
                            latitudeText = it
                            latitude = it.text.toFloatOrNull()
                        },
                        label = { Text("Latitude") },
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
                        label = { Text("Longitude") },
                        modifier = Modifier
                            .fillMaxWidth(0.8f),
                        singleLine = true,
                        isError = longitude == null
                    )
                } else {
                    Text("Station Name: ${station.name}", fontWeight = FontWeight.Bold)
                    Text("Official Station Number: ${station.officialStationNumber}")
                    Text("Coordinates:")
                    Text(
                        text = "Latitude: ${station.coordinates.lat}",
                        modifier = Modifier.padding(start = 16.dp)
                    )
                    Text(
                        text = "Longitude: ${station.coordinates.lng}",
                        modifier = Modifier.padding(start = 16.dp)
                    )
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
                                val feedback = updateStationInScrapedList(
                                    index = index,
                                    name = name,
                                    officialStationNumber = officialStationNumber,
                                    latitude = latitude,
                                    longitude = longitude,
                                    onSuccess = onUpdateStationSuccess
                                )

                                if (feedback.isNotEmpty()) { // show message only on error
                                    feedbackMessage = feedback
                                }
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
                            // Revert changes in input fields
                            name = station.name
                            officialStationNumber = station.officialStationNumber
                            coordinates = station.coordinates
                            longitude = station.coordinates.lng
                            latitude = station.coordinates.lat
                            longitudeText = TextFieldValue(station.coordinates.lng.toString())
                            latitudeText = TextFieldValue(station.coordinates.lat.toString())
                            editMode = false
                        }
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = "Cancel")
                    }
                }

                if (!editMode) {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                val feedback = insertStationFromScrapedListToDB(
                                    station = station,
                                    index = index,
                                    onSuccess = onInsertStationSuccess
                                )

                                if (feedback.isNotEmpty()) { // show message only on error
                                    feedbackMessage = feedback
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save To DB")
                    }
                }

                if (!editMode) {
                    IconButton(
                        onClick = {
                            onRemoveStation(index)
                        }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove from generated list")
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