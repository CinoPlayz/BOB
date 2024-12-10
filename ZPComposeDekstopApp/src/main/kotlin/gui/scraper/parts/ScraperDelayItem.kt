package gui.scraper.parts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import gui.RoutesDropdownMenu
import gui.StationsDropdownMenu
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import models.DelayInsert
import utils.api.dao.insertDelay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun DelayScraperItem(
    delay: DelayInsert,
    index: Int,
    allStations: List<Pair<String, String>>, // name, id
    allRoutes: List<Pair<Int, String>>, // trainNumber, id
    modifier: Modifier = Modifier,
    onInsertDelay: (Boolean, Int) -> Unit, // Int = index
    onUpdateDelay: (DelayInsert, Int) -> Unit // Int = index
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    var editMode by remember { mutableStateOf(false) }

    var requestYear by remember { mutableStateOf("${delay.timeOfRequest?.year}") }
    var requestMonth by remember { mutableStateOf("${delay.timeOfRequest?.monthValue}") }
    var requestDay by remember { mutableStateOf("${delay.timeOfRequest?.dayOfMonth}") }
    var requestHour by remember { mutableStateOf("${delay.timeOfRequest?.hour}") }
    var requestMinute by remember { mutableStateOf("${delay.timeOfRequest?.minute}") }
    var requestSecond by remember { mutableStateOf("${delay.timeOfRequest?.second}") }
    var dateTimeError by remember { mutableStateOf(false) }

    var selectedRoute by remember { mutableStateOf(delay.route) } // id
    var selectedCurrentStation by remember { mutableStateOf(delay.currentStation) } // id

    var delayMinutes by remember { mutableStateOf<Int?>(delay.delay) }

    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(delay) {
        requestYear = delay.timeOfRequest?.year.toString()
        requestMonth = delay.timeOfRequest?.monthValue.toString()
        requestDay = delay.timeOfRequest?.dayOfMonth.toString()
        requestHour = delay.timeOfRequest?.hour.toString()
        requestMinute = delay.timeOfRequest?.minute.toString()
        requestSecond = delay.timeOfRequest?.second.toString()
        selectedRoute = delay.route
        selectedCurrentStation = delay.currentStation
        delayMinutes = delay.delay
    }

    val onUpdateDelaySuccess: (DelayInsert, Int) -> Unit = { updatedDelay, index ->
        onUpdateDelay(updatedDelay, index)
        editMode = false
    }

    val onInsertDelaySuccess: (Boolean, Int) -> Unit = { success, index ->
        onInsertDelay(success, index)
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
                    RoutesDropdownMenu(
                        label = "Route Number",
                        options = allRoutes,
                        originalSelection = selectedRoute,
                        onSelectionChange = { selectedNewRoute ->
                            selectedRoute = selectedNewRoute
                        }
                    )
                    StationsDropdownMenu(
                        label = "Current Station",
                        options = allStations,
                        originalSelection = selectedCurrentStation,
                        onSelectionChange = { selectedNewStation ->
                            selectedCurrentStation = selectedNewStation
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

                } else {
                    Text("Time of Request: ${delay.timeOfRequest?.format(formatter)}")
                    Text("Route Number: ${allRoutes.find { it.second == selectedRoute }?.first}")
                    Text("Current Station: ${allStations.find { it.second == selectedCurrentStation }?.first}")
                    Text("Train Delay (minutes): ${delay.delay}")
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
                                val feedback = updateDelayInScrapedList(
                                    index = index,
                                    requestYear = requestYear,
                                    requestMonth = requestMonth,
                                    requestDay = requestDay,
                                    requestHour = requestHour,
                                    requestMinute = requestMinute,
                                    requestSecond = requestSecond,
                                    stationId = selectedCurrentStation,
                                    routeId = selectedRoute,
                                    delayMinutes = delayMinutes,
                                    onSuccess = onUpdateDelaySuccess
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
                            // Reset input fields to initial values
                            requestYear = delay.timeOfRequest?.year.toString()
                            requestMonth = delay.timeOfRequest?.monthValue.toString()
                            requestDay = delay.timeOfRequest?.dayOfMonth.toString()
                            requestHour = delay.timeOfRequest?.hour.toString()
                            requestMinute = delay.timeOfRequest?.minute.toString()
                            requestSecond = delay.timeOfRequest?.second.toString()
                            selectedRoute = delay.route
                            selectedCurrentStation = delay.currentStation
                            delayMinutes = delay.delay
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
                                val feedback = insertDelayFromScrapedListToDB(
                                    delay = delay,
                                    index = index,
                                    onSuccess = onInsertDelaySuccess
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

suspend fun updateDelayInScrapedList(
    index: Int,
    requestYear: String,
    requestMonth: String,
    requestDay: String,
    requestHour: String,
    requestMinute: String,
    requestSecond: String,
    stationId: String,
    routeId: String,
    delayMinutes: Int?,
    onSuccess: (DelayInsert, Int) -> Unit // Int = index
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

    val delayUpdate = DelayInsert(
        timeOfRequest = newRequestTimeStamp,
        route = routeId,
        currentStation = stationId,
        delay = delayMinutes
    )

    return try {
       /* var updatedDelay: Delay
        coroutineScope {
            updatedDelay = updateDelay(delayUpdate)
        }*/
        onSuccess(delayUpdate, index)
        ""
    } catch (e: Exception) {
        "Error updating delay in the list. ${e.message}"
    }
}

suspend fun insertDelayFromScrapedListToDB(
    delay: DelayInsert,
    index: Int,
    onSuccess: (Boolean, Int) -> Unit
): String {
    return try {
        var success: Boolean
        coroutineScope {
            success = insertDelay(delay)
        }
        onSuccess(success, index)
        "" // show success in ScraperStations function
    } catch (e: Exception) {
        "Error inserting delay to the database. ${e.message}"
    }
}