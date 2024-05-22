package gui.addData

import TitleText
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import gui.CustomDropdownMenu
import models.RouteStop
import java.time.format.DateTimeFormatter
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text


@Composable
fun AddRoute(
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
            text = "Add new route to database",
            fontSize = titleFontSize,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        InputRouteData(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )

    }
}

@Composable
fun InputRouteData(
    modifier: Modifier = Modifier,
) {
    var trainType by remember { mutableStateOf("") }
    var trainNumber by remember { mutableStateOf("") }

    var canSupportBikes by remember { mutableStateOf("") }

    var drivesOn by remember { mutableStateOf("") }

    var startStationName by remember { mutableStateOf("") }
    var startDepartureTime by remember { mutableStateOf("") }

    var endStationName by remember { mutableStateOf("") }
    var endArrivalTime by remember { mutableStateOf("") }

    //var middleStops by remember { mutableStateOf(listOf<RouteStop>()) }
    var middleStops by remember { mutableStateOf(listOf<MiddleStop>()) }

    val trainTypes = listOf("LP", "ICS")

    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    var validFromYear by remember { mutableStateOf("") }
    var validFromMonth by remember { mutableStateOf("") }
    var validFromDay by remember { mutableStateOf("") }
    var validFromHour by remember { mutableStateOf("") }
    var validFromMinute by remember { mutableStateOf("") }
    var validFromSecond by remember { mutableStateOf("00") }

    var validUntilYear by remember { mutableStateOf("") }
    var validUntilMonth by remember { mutableStateOf("") }
    var validUntilDay by remember { mutableStateOf("") }
    var validUntilHour by remember { mutableStateOf("") }
    var validUntilMinute by remember { mutableStateOf("") }
    var validUntilSecond by remember { mutableStateOf("00") }

    val daysOfWeek = listOf(
        "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Holidays"
    )
    val drivesOnDays = remember {
        mutableStateMapOf<String, Boolean>().apply {
            daysOfWeek.forEach { put(it, false) }
        }
    }

    val onReset: () -> Unit = {
        trainNumber = ""
        trainType = ""
        canSupportBikes = ""
        drivesOn = ""
        startStationName = ""
        endStationName = ""
        validFromYear = ""
        validFromMonth = ""
        validFromDay = ""
        validFromHour = ""
        validFromMinute = ""
        validFromSecond = "00"
        validUntilYear = ""
        validUntilMonth = ""
        validUntilDay = ""
        validUntilHour = ""
        validUntilMinute = ""
        validUntilSecond = "00"
        drivesOnDays.clear()
        //middleStops = ""
    }

    val coroutineScope = rememberCoroutineScope() // writeTrainToDB

    val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

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

        Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Can Support Bikes:", style = MaterialTheme.typography.body1)
                Spacer(modifier = Modifier.width(20.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = canSupportBikes == "Yes",
                        onClick = { canSupportBikes = "Yes" }
                    )
                    //Spacer(modifier = Modifier.width(4.dp))
                    Text("Yes", modifier = Modifier.clickable { canSupportBikes = "Yes" })
                }
                Spacer(modifier = Modifier.width(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = canSupportBikes == "No",
                        onClick = { canSupportBikes = "No" }
                    )
                    //Spacer(modifier = Modifier.width(4.dp))
                    Text("No", modifier = Modifier.clickable { canSupportBikes = "No" })
                }
            }
        }

        Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Text("Drives On:", style = MaterialTheme.typography.body1)

            daysOfWeek.forEach { day ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { drivesOnDays[day] = !drivesOnDays[day]!! },
                    //.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = drivesOnDays[day]!!,
                        onCheckedChange = { drivesOnDays[day] = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(day)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Valid From")
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = validFromYear,
                onValueChange = { validFromYear = it.take(4) }, // Limit input to 4 characters
                label = { Text("YYYY") },
                modifier = Modifier.weight(1f),
            )
            Text("-", modifier = Modifier.align(Alignment.CenterVertically))
            OutlinedTextField(
                value = validFromMonth,
                onValueChange = { validFromMonth = it.take(2) }, // Limit input to 2 characters
                label = { Text("MM") },
                modifier = Modifier.weight(1f),
            )
            Text("-", modifier = Modifier.align(Alignment.CenterVertically))
            OutlinedTextField(
                value = validFromDay,
                onValueChange = { validFromDay = it.take(2) }, // Limit input to 2 characters
                label = { Text("DD") },
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(16.dp))
            OutlinedTextField(
                value = validFromHour,
                onValueChange = { validFromHour = it.take(2) }, // Limit input to 2 characters
                label = { Text("HH") },
                modifier = Modifier.weight(1f),
            )
            Text(":", modifier = Modifier.align(Alignment.CenterVertically))
            OutlinedTextField(
                value = validFromMinute,
                onValueChange = { validFromMinute = it.take(2) }, // Limit input to 2 characters
                label = { Text("MM") },
                modifier = Modifier.weight(1f),
            )
            Text(":", modifier = Modifier.align(Alignment.CenterVertically))
            OutlinedTextField(
                value = validFromSecond,
                onValueChange = { validFromSecond = it.take(2) }, // Limit input to 2 characters
                label = { Text("SS") },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Valid Until")
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = validUntilYear,
                onValueChange = { validUntilYear = it.take(4) }, // Limit input to 4 characters
                label = { Text("YYYY") },
                modifier = Modifier.weight(1f),
            )
            Text("-", modifier = Modifier.align(Alignment.CenterVertically))
            OutlinedTextField(
                value = validUntilMonth,
                onValueChange = { validUntilMonth = it.take(2) }, // Limit input to 2 characters
                label = { Text("MM") },
                modifier = Modifier.weight(1f),
            )
            Text("-", modifier = Modifier.align(Alignment.CenterVertically))
            OutlinedTextField(
                value = validUntilDay,
                onValueChange = { validUntilDay = it.take(2) }, // Limit input to 2 characters
                label = { Text("DD") },
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(16.dp))
            OutlinedTextField(
                value = validUntilHour,
                onValueChange = { validUntilHour = it.take(2) }, // Limit input to 2 characters
                label = { Text("HH") },
                modifier = Modifier.weight(1f),
            )
            Text(":", modifier = Modifier.align(Alignment.CenterVertically))
            OutlinedTextField(
                value = validUntilMinute,
                onValueChange = { validUntilMinute = it.take(2) }, // Limit input to 2 characters
                label = { Text("MM") },
                modifier = Modifier.weight(1f),
            )
            Text(":", modifier = Modifier.align(Alignment.CenterVertically))
            OutlinedTextField(
                value = validUntilSecond,
                onValueChange = { validUntilSecond = it.take(2) }, // Limit input to 2 characters
                label = { Text("SS") },
                modifier = Modifier.weight(1f),
            )
        }

        Text("Start Station Details")
        OutlinedTextField(
            value = startStationName,
            onValueChange = { startStationName = it },
            label = { Text("Start Station Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = startDepartureTime,
            onValueChange = { startDepartureTime = it },
            label = { Text("Departure Time (HH:MM)") },
            modifier = Modifier.fillMaxWidth()
        )

        Text("End Station Details")
        OutlinedTextField(
            value = endStationName,
            onValueChange = { endStationName = it },
            label = { Text("End Station Name") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = endArrivalTime,
            onValueChange = { endArrivalTime = it },
            label = { Text("Arrival Time (HH:MM)") },
            modifier = Modifier.fillMaxWidth()
        )

        MiddleStopsInput(
            middleStops = middleStops,
            onAddStop = { middleStops = middleStops + MiddleStop() }
        )

        // Add a mechanism to input multiple middle stops (omitted here for brevity)

        /*Button(
            onClick = {
                coroutineScope.launch {
                    val feedback = writeRouteToDB(
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
            Text("Write data to database")
        }*/
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

data class MiddleStop(var stationName: String = "", var arrivalTime: String = "")

@Composable
fun MiddleStopsInput(
    middleStops: List<MiddleStop>,
    onAddStop: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Middle Stops"/*, style = MaterialTheme.typography.h6*/)

        /*LazyColumn(modifier = Modifier.fillMaxWidth()) {
            itemsIndexed(middleStops) { index, stop ->
                MiddleStopInput(stop)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }*/

        middleStops.forEach { stop ->
            MiddleStopInput(stop)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = { onAddStop() },
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp)
        ) {
            Text("Add Stop")
        }
    }
}

@Composable
fun MiddleStopInput(stop: MiddleStop) {
    var stationName by remember { mutableStateOf(stop.stationName) }
    var arrivalTime by remember { mutableStateOf(stop.arrivalTime) }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = stationName,
            onValueChange = {
                stationName = it
                stop.stationName = it // Update the state in the list
            },
            label = { Text("Stop Station Name") },
            modifier = Modifier.fillMaxWidth()
        )
        //Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = arrivalTime,
            onValueChange = {
                arrivalTime = it
                stop.arrivalTime = it // Update the state in the list
            },
            label = { Text("Arrival Time (HH:MM)") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

suspend fun writeRouteToDB() {
    TODO()
}