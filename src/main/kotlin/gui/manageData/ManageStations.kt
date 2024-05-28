package gui.manageData

import TitleText
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import models.*
import utils.api.dao.deleteStation
import utils.api.dao.getAllStations
import utils.api.dao.updateStation
import java.time.format.DateTimeFormatter

@Composable
fun ManageStations(
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()

    var stations by remember { mutableStateOf<List<Station>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isLoading = remember { mutableStateOf(true) }

    fun updateStation(newStation: Station) {
        stations = stations.map { if (it.id == newStation.id) newStation else it }
    }

    var stationToDelete by remember { mutableStateOf<Station?>(null) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    fun deleteStation(station: Station) {
        stationToDelete = station
    }

    stationToDelete?.let { station ->
        AlertDialog(
            onDismissRequest = {
                stationToDelete = null
            },
            title = {
                Text(text = "Delete Station")
            },
            text = {
                Text(text = "Are you sure you want to delete ${station.name}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val feedback = deleteStationFromDB(station.id) {
                                stations = stations.filterNot { it.id == station.id }
                            }
                            stationToDelete = null
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
                        stationToDelete = null
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
                stations = withContext(Dispatchers.IO) { getAllStations() }
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
                Text("Failed to load stations: $errorMessage")
            }
        } else {
            val state = rememberLazyListState()

            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                if (stations.isEmpty()) {
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
                                text = "*** NO STATIONS ***",
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
                    items(stations) { station ->
                        StationItem(
                            station = station,
                            onDeleteStation = { stationToDelete -> deleteStation(stationToDelete) },
                            onUpdateStation = { stationToUpdate -> updateStation(stationToUpdate) }
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
fun StationItem(
    station: Station,
    modifier: Modifier = Modifier,
    onDeleteStation: (Station) -> Unit,
    onUpdateStation: (Station) -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    var editMode by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf(station.name) }
    var officialStationNumber by remember { mutableStateOf(station.officialStationNumber) }

    var coordinates by remember { mutableStateOf(station.coordinates) }
    var longitude by remember { mutableStateOf(coordinates.lng) }
    var longitudeText by remember { mutableStateOf(TextFieldValue(longitude.toString())) }
    var latitude by remember { mutableStateOf(coordinates.lat) }
    var latitudeText by remember { mutableStateOf(TextFieldValue(latitude.toString())) }

    var createdAt by remember { mutableStateOf(station.createdAt) }
    var updatedAt by remember { mutableStateOf(station.updatedAt) }

    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(station) {
        name = station.name
        officialStationNumber = station.officialStationNumber
        coordinates = station.coordinates
        createdAt = station.createdAt
        updatedAt = station.updatedAt
    }

    val onUpdateStationSuccess: (Station) -> Unit = { updatedStation ->
        onUpdateStation(updatedStation)

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
                    TODO()
                } else {
                    Text("Station Name: $name", fontWeight = FontWeight.Bold)
                    Text("Official Station Number: $officialStationNumber")
                    Text("Coordinates:")
                    Text(
                        text = "Longitude: $longitude",
                        modifier = Modifier.padding(start = 16.dp)
                    )
                    Text(
                        text = "Latitude: $latitude",
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
                                val feedback = updateStationInDB(
                                    station = station,
                                    name = name,
                                    officialStationNumber = officialStationNumber,
                                    latitude = latitude,
                                    longitude = longitude,
                                    onSuccess = onUpdateStationSuccess
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
                            editMode = false
                        }
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = "Cancel")
                    }
                }

                if (!editMode) {
                    IconButton(
                        onClick = {
                            onDeleteStation(station)
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

suspend fun deleteStationFromDB(
    id: String,
    onSuccess: () -> Unit
): String {

    return try {
        coroutineScope {
            deleteStation(id)
        }
        onSuccess()
        "Station successfully deleted from the database."
    } catch (e: Exception) {
        "Error removing station from the database. ${e.message}"
    }
}

suspend fun updateStationInDB(
    station: Station,
    name: String,
    officialStationNumber: String,
    latitude: Float?,
    longitude: Float?,
    onSuccess: (Station) -> Unit
): String {

    if (name.isEmpty() ||
        officialStationNumber.isEmpty() ||
        latitude!!.isNaN() ||
        longitude!!.isNaN()
    ) {
        return ("Please fill in all fields.")
    }

    val newCoordinates = Coordinates(
        lat = latitude,
        lng = longitude
    )

    val stationUpdate = StationUpdate(
        id = station.id,
        name = name,
        officialStationNumber = officialStationNumber,
        coordinates = newCoordinates,
    )

    return try {
        var updatedStation: Station
        coroutineScope {
            updatedStation = updateStation(stationUpdate)
        }
        onSuccess(updatedStation)
        "Station successfully updated in the database."
    } catch (e: Exception) {
        "Error updating station in the database. ${e.message}"
    }
}
