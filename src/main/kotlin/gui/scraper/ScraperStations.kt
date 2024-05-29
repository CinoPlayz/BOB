package gui.scraper

import ResultStations
import TitleText
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.sp
import getStationsAndProcess
import gui.manageData.updateStationInDB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import models.Station
import models.StationInsert
import java.time.format.DateTimeFormatter

@Composable
fun ScraperStations(
    modifier: Modifier = Modifier
) {
    // State to hold the result of the operation
    val resultStations = remember { mutableStateOf<ResultStations>(ResultStations()) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    // State to hold the loading status
    val isLoading = remember { mutableStateOf(true) }

    // LaunchedEffect to trigger the data fetching operation
    LaunchedEffect(Unit) {
        try {
            // Coroutine call - data fetch
            withContext(Dispatchers.IO) { getStationsAndProcess(resultStations) }
            /*withContext(Dispatchers.IO) { resultStateStations.value.listOfStations.forEach {
                insertStation(it)
            }}*/

        } catch (e: Exception) {
            println("An error occurred: ${e.message}")
        } finally {
            isLoading.value = false // Set loading to false after fetching data
        }
    }

    // Loading indicator - data processing
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
        // Display data
        if (resultStations.value.error != "") {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Text("Failed to load stations: ${resultStations.value.error}")
            }
        } else {
            val state = rememberLazyListState()

            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                if (resultStations.value.listOfStations.isEmpty()) {
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
                    items(resultStations.value.listOfStations) { station ->
                        StationScraperItem(
                            station = station,

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
        /*Box(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()) // Box with enabled scroll
        ) {
            Text("Result: ${resultStations.value.listOfStations}")
        }*/
        /*resultState.value?.let { result ->
            Text("Result: $result", modifier = Modifier.padding(16.dp))
        }*/
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
fun StationScraperItem(
    station: StationInsert,
    modifier: Modifier = Modifier,
    //onInsertStation: (Station) -> Unit
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

    /*val onUpdateStationSuccess: (Station) -> Unit = { updatedStation ->
        onUpdateStation(updatedStation)
        editMode = false
    }*/

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
                            /*coroutineScope.launch {
                                val feedback = updateStationInDB(
                                    station = station,
                                    name = name,
                                    officialStationNumber = officialStationNumber,
                                    latitude = latitude,
                                    longitude = longitude,
                                    onSuccess = onUpdateStationSuccess
                                )

                                feedbackMessage = feedback
                            }*/
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
                            //onDeleteStation(station)
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