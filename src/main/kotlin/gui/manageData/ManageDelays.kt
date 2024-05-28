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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import models.Delay
import models.Route
import models.Station
import models.User
import utils.api.dao.*
import java.time.format.DateTimeFormatter

@Composable
fun ManageDelays(
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()

    var delays by remember { mutableStateOf<List<Delay>>(emptyList()) }
    var allStations by remember { mutableStateOf<List<Station>>(emptyList()) } // for currentStation list
    var allStationsPairs by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var allRoutes by remember { mutableStateOf<List<Route>>(emptyList()) } // for route list
    var allRoutesPairs by remember { mutableStateOf<List<Pair<Int, String>>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isLoading = remember { mutableStateOf(true) }

    fun updateDelay(newDelay: Delay) {
        delays = delays.map { if (it.id == newDelay.id) newDelay else it }
    }

    var delayToDelete by remember { mutableStateOf<Delay?>(null) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    fun deleteDelay(delay: Delay) {
        delayToDelete = delay
    }

    delayToDelete?.let { delay ->
        AlertDialog(
            onDismissRequest = {
                delayToDelete = null
            },
            title = {
                Text(text = "Delete Delay")
            },
            text = {
                Text(text = "Are you sure you want to delete this delay?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val feedback = deleteDelayFromDB(delay.id) {
                                delays = delays.filterNot { it.id == delay.id }
                            }
                            delayToDelete = null
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
                        delayToDelete = null
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
                allStationsPairs = allStations.toNameIDPairs()
                allRoutesPairs = allRoutes.toNumberIDPairs()
                delays = withContext(Dispatchers.IO) { getAllDelays() }
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
                Text("Failed to load delays: $errorMessage")
            }
        } else {
            val state = rememberLazyListState()

            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                if (delays.isEmpty()) {
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
                                text = "*** NO DELAYS ***",
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
                    items(delays) { delay ->
                        DelayItem(
                            delay = delay,
                            allStations = allStationsPairs,
                            allRoutes = allRoutesPairs,
                            onDeleteDelay = { delayToDelete -> deleteDelay(delayToDelete) },
                            onUpdateDelay = { delayToUpdate -> updateDelay(delayToUpdate) }
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
fun DelayItem(
    delay: Delay,
    allStations: List<Pair<String, String>>, // name, id
    allRoutes: List<Pair<Int, String>>, // trainNumber, id
    modifier: Modifier = Modifier,
    onDeleteDelay: (Delay) -> Unit,
    onUpdateDelay: (Delay) -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    var editMode by remember { mutableStateOf(false) }

    var requestYear by remember { mutableStateOf("${delay.timeOfRequest.year}") }
    var requestMonth by remember { mutableStateOf("${delay.timeOfRequest.monthValue}") }
    var requestDay by remember { mutableStateOf("${delay.timeOfRequest.dayOfMonth}") }
    var requestHour by remember { mutableStateOf("${delay.timeOfRequest.hour}") }
    var requestMinute by remember { mutableStateOf("${delay.timeOfRequest.minute}") }
    var requestSecond by remember { mutableStateOf("${delay.timeOfRequest.second}") }
    var dateTimeError by remember { mutableStateOf(false) }

    var selectedRoute by remember { mutableStateOf(delay.route) } // id
    var selectedCurrentStation by remember { mutableStateOf(delay.currentStation) } //id

    var delayMinutes by remember { mutableStateOf(delay.delay) }

    var createdAt by remember { mutableStateOf(delay.createdAt) }
    var updatedAt by remember { mutableStateOf(delay.updatedAt) }

    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(delay) {
        requestYear = delay.timeOfRequest.year.toString()
        requestMonth = delay.timeOfRequest.monthValue.toString()
        requestDay = delay.timeOfRequest.dayOfMonth.toString()
        requestHour = delay.timeOfRequest.hour.toString()
        requestMinute = delay.timeOfRequest.minute.toString()
        requestSecond = delay.timeOfRequest.second.toString()
        selectedRoute = delay.route
        selectedCurrentStation = delay.currentStation
        delayMinutes = delay.delay
        createdAt = delay.createdAt
        updatedAt = delay.updatedAt
    }

    val onUpdateDelaySuccess: (Delay) -> Unit = { updatedDelay ->
        onUpdateDelay(updatedDelay)

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
                    Text("Time of Request: ${delay.timeOfRequest.format(formatter)}")
                    Text("Route Number: ${allRoutes.find { it.second == selectedRoute }?.first}")
                    Text("Current Station: ${allStations.find { it.second == selectedCurrentStation }?.first}")
                    Text("Train delay (minutes): ${delay.delay}")
                    Text("Delay Created On: ${delay.createdAt.format(formatter)}", fontSize = 12.sp)
                    Text("Delay Last Updated On: ${delay.updatedAt.format(formatter)}", fontSize = 12.sp)

                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Buttons
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

suspend fun deleteDelayFromDB(
    id: String,
    onSuccess: () -> Unit
): String {

    return try {
        coroutineScope {
            deleteDelay(id)
        }
        onSuccess()
        "Delay successfully deleted from the database."
    } catch (e: Exception) {
        "Error removing delay from the database. ${e.message}"
    }
}

suspend fun updateDelayInDB(
    number:String
) {
    TODO()
}

// Function to transform list of stations into list of pairs
fun List<Station>.toNameIDPairs(): List<Pair<String, String>> {
    return this.map { station ->
        station.name to station.id
    }
}

@Composable
fun StationsDropdownMenu(
    label: String,
    options: List<Pair<String, String>>, // name, id
    originalSelection: String = "",
    onSelectionChange: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(originalSelection) }
    var selectedName by remember(originalSelection) {
        mutableStateOf(options.find { it.second == originalSelection }?.first ?: "")
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = { }, // Disable text editing
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                IconButton(
                    onClick = { expanded = !expanded },
                ) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = "Dropdown Menu")
                }
            },
            modifier = Modifier
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
        ) {
            options.forEach { option ->
                DropdownMenuItem(onClick = {
                    selectedOption = option.second
                    selectedName = option.first
                    onSelectionChange(option.second)
                    expanded = false
                }) {
                    Text(text = option.first)
                }
            }
        }
    }
}

fun List<Route>.toNumberIDPairs(): List<Pair<Int, String>> {
    return this.map { route ->
        route.trainNumber to route.id
    }
}

@Composable
fun RoutesDropdownMenu(
    label: String,
    options: List<Pair<Int, String>>, // trainNumber, id
    originalSelection: String = "",
    onSelectionChange: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(originalSelection) }
    var selectedName by remember(originalSelection) {
        mutableStateOf(options.find { it.second == originalSelection }?.first?.toString() ?: "")
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = { }, // Disable text editing
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                IconButton(
                    onClick = { expanded = !expanded },
                ) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = "Dropdown Menu")
                }
            },
            modifier = Modifier
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
        ) {
            options.forEach { option ->
                DropdownMenuItem(onClick = {
                    selectedOption = option.second
                    selectedName = option.first.toString()
                    onSelectionChange(option.second)
                    expanded = false
                }) {
                    Text(text = option.first.toString())
                }
            }
        }
    }
}