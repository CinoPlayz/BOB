package gui.manageData

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gui.*
import gui.addData.MiddleStopsInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import models.*
import utils.api.dao.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Composable
fun ManageRoutes(
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()

    var routes by remember { mutableStateOf<List<Route>>(emptyList()) }
    var allStations by remember { mutableStateOf<List<Station>>(emptyList()) } // for start/end/middle stations list
    var allStationsPairs by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    val trainTypes = listOf(
        "AVT", "BUS", "EC", "EN", "IC", "ICS",
        "LP", "LPV", "LRG", "MO", "MV", "RG"
    )

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    val isLoading = remember { mutableStateOf(true) }

    fun updateRoute(newRoute: Route) {
        routes = routes.map { if (it.id == newRoute.id) newRoute else it }
    }

    var routeToDelete by remember { mutableStateOf<Route?>(null) }

    fun deleteRoute(route: Route) {
        routeToDelete = route
    }

    routeToDelete?.let { route ->
        AlertDialog(
            onDismissRequest = {
                routeToDelete = null
            },
            title = {
                Text(text = "Delete Route")
            },
            text = {
                Text(text = "Are you sure you want to delete route No. ${route.trainNumber}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val feedback = deleteRouteFromDB(route.id) {
                                routes = routes.filterNot { it.id == route.id }
                            }
                            routeToDelete = null
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
                        routeToDelete = null
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
                allStationsPairs = allStations.toNameIDPairs()
                routes = withContext(Dispatchers.IO) { getAllRoutes() }
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
                Text("Failed to load routes: $errorMessage")
            }
        } else {
            val state = rememberLazyListState()

            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                if (routes.isEmpty()) {
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
                                text = "*** NO ROUTES ***",
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
                    items(routes) { route ->
                        RouteItem(
                            route = route,
                            trainTypes = trainTypes,
                            allStations = allStationsPairs,
                            onDeleteRoute = { routeToDelete -> deleteRoute(routeToDelete) },
                            onUpdateRoute = { routeToUpdate -> updateRoute(routeToUpdate) }
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
fun RouteItem(
    route: Route,
    trainTypes: List<String>,
    allStations: List<Pair<String, String>>, // name, id
    modifier: Modifier = Modifier,
    onDeleteRoute: (Route) -> Unit,
    onUpdateRoute: (Route) -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    var editMode by remember { mutableStateOf(false) }

    val allStationsRemoveItem = listOf("Remove" to "0")
    val allStationsRemove = allStationsRemoveItem + allStations

    var trainType by remember { mutableStateOf(route.trainType) }
    var trainNumber by remember { mutableStateOf<Int?>(route.trainNumber) }
    var trainNumberText by remember { mutableStateOf(trainNumber?.toString() ?: "") }

    var validFromYear by remember { mutableStateOf("${route.validFrom?.year}") }
    var validFromMonth by remember { mutableStateOf("${route.validFrom?.monthValue}") }
    var validFromDay by remember { mutableStateOf("${route.validFrom?.dayOfMonth}") }
    var validFromHour by remember { mutableStateOf("${route.validFrom?.hour}") }
    var validFromMinute by remember { mutableStateOf("${route.validFrom?.minute}") }
    var validFromSecond by remember { mutableStateOf("${route.validFrom?.second}") }
    var validFromDateTimeError by remember { mutableStateOf(false) }

    var validUntilYear by remember { mutableStateOf("${route.validUntil?.year}") }
    var validUntilMonth by remember { mutableStateOf("${route.validUntil?.monthValue}") }
    var validUntilDay by remember { mutableStateOf("${route.validUntil?.dayOfMonth}") }
    var validUntilHour by remember { mutableStateOf("${route.validUntil?.hour}") }
    var validUntilMinute by remember { mutableStateOf("${route.validUntil?.minute}") }
    var validUntilSecond by remember { mutableStateOf("${route.validUntil?.second}") }
    var validUntilDateTimeError by remember { mutableStateOf(false) }

    var canSupportBikes by remember { mutableStateOf(route.canSupportBikes) }

    var drivesOn by remember { mutableStateOf(route.drivesOn) }
    val drivesOnDays = remember {
        mutableStateMapOf<String, Boolean>().apply {
            daysOfWeek.forEachIndexed { index, day ->
                put(day, route.drivesOn.contains(index))
            }
        }
    }

    var selectedStartStation by remember { mutableStateOf(route.start.station) } // id
    var hourStartStation by remember { mutableStateOf("") } // HH:mm
    var minuteStartStation by remember { mutableStateOf("") }
    var startTimeError by remember { mutableStateOf(false) }

    var selectedEndStation by remember { mutableStateOf(route.end.station) } // id
    var hourEndStation by remember { mutableStateOf("") } // HH:mm
    var minuteEndStation by remember { mutableStateOf("") }
    var endTimeError by remember { mutableStateOf(false) }

    var middles by remember { mutableStateOf(route.middle) } // middle stations
    var newMiddles by remember { mutableStateOf(listOf<RouteStopInsert>()) }

    var createdAt by remember { mutableStateOf(route.createdAt) }
    var updatedAt by remember { mutableStateOf(route.updatedAt) }

    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(route) {
        trainType = route.trainType
        trainNumber = route.trainNumber
        validFromYear = route.validFrom?.year.toString()
        validFromMonth = route.validFrom?.monthValue.toString()
        validFromDay = route.validFrom?.dayOfMonth.toString()
        validFromHour = route.validFrom?.hour.toString()
        validFromMinute = route.validFrom?.minute.toString()
        validFromSecond = route.validFrom?.second.toString()
        validUntilYear = route.validUntil?.year.toString()
        validUntilMonth = route.validUntil?.monthValue.toString()
        validUntilDay = route.validUntil?.dayOfMonth.toString()
        validUntilHour = route.validUntil?.hour.toString()
        validUntilMinute = route.validUntil?.minute.toString()
        validUntilSecond = route.validUntil?.second.toString()
        canSupportBikes = route.canSupportBikes
        drivesOnDays.clear()
        daysOfWeek.forEachIndexed { index, day ->
            drivesOnDays[day] = route.drivesOn.contains(index)
        }
        selectedStartStation = route.start.station
        val (hourStartStationParsed, minuteStartStationParsed) = parseShortTime(route.start.time)
        hourStartStation = hourStartStationParsed
        minuteStartStation = minuteStartStationParsed
        selectedEndStation = route.end.station
        val (hourEndStationParsed, minuteEndStationParsed) = parseShortTime(route.end.time)
        hourEndStation = hourEndStationParsed
        minuteEndStation = minuteEndStationParsed
        middles = route.middle
        newMiddles = listOf()
        createdAt = route.createdAt
        updatedAt = route.updatedAt
    }

    val onUpdateRouteSuccess: (Route) -> Unit = { updatedRoute ->
        onUpdateRoute(updatedRoute)
        editMode = false
    }

    fun updateDateTimeValidFromError() {
        try {
            val newYear = validFromYear.toIntOrNull()
            val newMonth = validFromMonth.toIntOrNull()
            val newDay = validFromDay.toIntOrNull()
            val newHour = validFromHour.toIntOrNull()
            val newMinute = validFromMinute.toIntOrNull()
            val newSecond = validFromSecond.toIntOrNull()

            if (newYear != null && newMonth != null && newDay != null && newHour != null && newMinute != null && newSecond != null) {
                LocalDateTime.of(newYear, newMonth, newDay, newHour, newMinute, newSecond)
                validFromDateTimeError = false
            } else {
                validFromDateTimeError = true
            }
        } catch (e: Exception) {
            validFromDateTimeError = true
        }
    }

    fun updateDateTimeValidUntilError() {
        try {
            val newYear = validUntilYear.toIntOrNull()
            val newMonth = validUntilMonth.toIntOrNull()
            val newDay = validUntilDay.toIntOrNull()
            val newHour = validUntilHour.toIntOrNull()
            val newMinute = validUntilMinute.toIntOrNull()
            val newSecond = validUntilSecond.toIntOrNull()

            if (newYear != null && newMonth != null && newDay != null && newHour != null && newMinute != null && newSecond != null) {
                LocalDateTime.of(newYear, newMonth, newDay, newHour, newMinute, newSecond)
                validUntilDateTimeError = false
            } else {
                validUntilDateTimeError = true
            }
        } catch (e: Exception) {
            validUntilDateTimeError = true
        }
    }

    fun updateTimeErrorStart() {
        try {
            val newHour = hourStartStation.toIntOrNull()
            val newMinute = minuteStartStation.toIntOrNull()

            startTimeError = when {
                newHour == null || newHour !in 0..23 -> true
                newMinute == null || newMinute !in 0..59 -> true
                else -> false
            }
        } catch (e: Exception) {
            startTimeError = true
        }
    }

    fun updateTimeErrorEnd() {
        try {
            val newHour = hourEndStation.toIntOrNull()
            val newMinute = minuteEndStation.toIntOrNull()

            endTimeError = when {
                newHour == null || newHour !in 0..23 -> true
                newMinute == null || newMinute !in 0..59 -> true
                else -> false
            }
        } catch (e: Exception) {
            endTimeError = true
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
                    OutlinedTextField(
                        value = trainNumberText,
                        onValueChange = {
                            trainNumberText = it
                            trainNumber = it.toIntOrNull()
                        },
                        label = { Text("Route Number") },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    CustomDropdownMenu(
                        label = "Choose train type",
                        options = trainTypes,
                        initialSelection = trainType,
                        onSelectionChange = { selectedTrainType ->
                            trainType = selectedTrainType
                        }
                    )

                    Text("Valid From")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = validFromYear,
                            onValueChange = { newYearString ->
                                validFromYear = newYearString.take(4) // Limit input to 4 characters
                                updateDateTimeValidFromError()
                            },
                            isError = validFromDateTimeError,
                            label = { Text("YYYY") },
                            modifier = Modifier.weight(1f),
                        )
                        Text("-", modifier = Modifier.align(Alignment.CenterVertically))
                        OutlinedTextField(
                            value = validFromMonth,
                            onValueChange = { newMonthString ->
                                validFromMonth = newMonthString.take(2) // Limit input to 2 characters
                                updateDateTimeValidFromError()
                            },
                            isError = validFromDateTimeError,
                            label = { Text("MM") },
                            modifier = Modifier.weight(1f),
                        )
                        Text("-", modifier = Modifier.align(Alignment.CenterVertically))
                        OutlinedTextField(
                            value = validFromDay,
                            onValueChange = { newDayString ->
                                validFromDay = newDayString.take(2) // Limit input to 2 characters
                                updateDateTimeValidFromError()
                            },
                            isError = validFromDateTimeError,
                            label = { Text("DD") },
                            modifier = Modifier.weight(1f),
                        )
                        Text(" ", modifier = Modifier.align(Alignment.CenterVertically))
                        OutlinedTextField(
                            value = validFromHour,
                            onValueChange = { newHourString ->
                                validFromHour = newHourString.take(2) // Limit input to 2 characters
                                updateDateTimeValidFromError()
                            },
                            isError = validFromDateTimeError,
                            label = { Text("HH") },
                            modifier = Modifier.weight(1f),
                        )
                        Text(":", modifier = Modifier.align(Alignment.CenterVertically))
                        OutlinedTextField(
                            value = validFromMinute,
                            onValueChange = { newMinuteString ->
                                validFromMinute = newMinuteString.take(2) // Limit input to 2 characters
                                updateDateTimeValidFromError()
                            },
                            isError = validFromDateTimeError,
                            label = { Text("MM") },
                            modifier = Modifier.weight(1f),
                        )
                        Text(":", modifier = Modifier.align(Alignment.CenterVertically))
                        OutlinedTextField(
                            value = validFromSecond,
                            onValueChange = { newSecondString ->
                                validFromSecond = newSecondString.take(2) // Limit input to 2 characters
                                updateDateTimeValidFromError()
                            },
                            isError = validFromDateTimeError,
                            label = { Text("SS") },
                            modifier = Modifier.weight(1f),
                        )
                    }

                    Text("Valid Until")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = validUntilYear,
                            onValueChange = { newYearString ->
                                validUntilYear = newYearString.take(4) // Limit input to 4 characters
                                updateDateTimeValidUntilError()
                            },
                            isError = validUntilDateTimeError,
                            label = { Text("YYYY") },
                            modifier = Modifier.weight(1f),
                        )
                        Text("-", modifier = Modifier.align(Alignment.CenterVertically))
                        OutlinedTextField(
                            value = validUntilMonth,
                            onValueChange = { newMonthString ->
                                validUntilMonth = newMonthString.take(2) // Limit input to 2 characters
                                updateDateTimeValidUntilError()
                            },
                            isError = validUntilDateTimeError,
                            label = { Text("MM") },
                            modifier = Modifier.weight(1f),
                        )
                        Text("-", modifier = Modifier.align(Alignment.CenterVertically))
                        OutlinedTextField(
                            value = validUntilDay,
                            onValueChange = { newDayString ->
                                validUntilDay = newDayString.take(2) // Limit input to 2 characters
                                updateDateTimeValidUntilError()
                            },
                            isError = validUntilDateTimeError,
                            label = { Text("DD") },
                            modifier = Modifier.weight(1f),
                        )
                        Text(" ", modifier = Modifier.align(Alignment.CenterVertically))
                        OutlinedTextField(
                            value = validUntilHour,
                            onValueChange = { newHourString ->
                                validUntilHour = newHourString.take(2) // Limit input to 2 characters
                                updateDateTimeValidUntilError()
                            },
                            isError = validUntilDateTimeError,
                            label = { Text("HH") },
                            modifier = Modifier.weight(1f),
                        )
                        Text(":", modifier = Modifier.align(Alignment.CenterVertically))
                        OutlinedTextField(
                            value = validUntilMinute,
                            onValueChange = { newMinuteString ->
                                validUntilMinute = newMinuteString.take(2) // Limit input to 2 characters
                                updateDateTimeValidUntilError()
                            },
                            isError = validUntilDateTimeError,
                            label = { Text("MM") },
                            modifier = Modifier.weight(1f),
                        )
                        Text(":", modifier = Modifier.align(Alignment.CenterVertically))
                        OutlinedTextField(
                            value = validUntilSecond,
                            onValueChange = { newSecondString ->
                                validUntilSecond = newSecondString.take(2) // Limit input to 2 characters
                                updateDateTimeValidUntilError()
                            },
                            isError = validUntilDateTimeError,
                            label = { Text("SS") },
                            modifier = Modifier.weight(1f),
                        )
                    }

                    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Can Support Bikes:", style = MaterialTheme.typography.body1)
                            Spacer(modifier = Modifier.width(20.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = canSupportBikes,
                                    onClick = { canSupportBikes = true }
                                )
                                Text("Yes")
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = !canSupportBikes,
                                    onClick = { canSupportBikes = false }
                                )
                                Text("No")
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

                    Text("Departure Station")
                    StationsDropdownMenu(
                        label = "Departure Station Name",
                        options = allStations,
                        originalSelection = selectedStartStation,
                        onSelectionChange = { selectedNewStation ->
                            selectedStartStation = selectedNewStation
                        }
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(bottom = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = hourStartStation,
                            onValueChange = { newRouteStartTimeHour ->
                                hourStartStation = newRouteStartTimeHour.take(2)
                                updateTimeErrorStart()
                            },
                            isError = startTimeError,
                            label = { Text("Departure Time (HH)") },
                            modifier = Modifier.weight(1f),
                        )
                        Text(":", modifier = Modifier.align(Alignment.CenterVertically))
                        OutlinedTextField(
                            value = minuteStartStation,
                            onValueChange = { newRouteStartTimeMinute ->
                                minuteStartStation = newRouteStartTimeMinute.take(2)
                                updateTimeErrorStart()
                            },
                            isError = startTimeError,
                            label = { Text("Departure Time (MM)") },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Spacer(modifier = Modifier.height(height = 16.dp))
                    Text("Arrival Station")
                    StationsDropdownMenu(
                        label = "Arrival Station Name",
                        options = allStations,
                        originalSelection = selectedEndStation,
                        onSelectionChange = { selectedNewStation ->
                            selectedEndStation = selectedNewStation
                        }
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(bottom = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = hourEndStation,
                            onValueChange = { newRouteStartTimeHour ->
                                hourEndStation = newRouteStartTimeHour.take(2)
                                updateTimeErrorEnd()
                            },
                            isError = endTimeError,
                            label = { Text("Departure Time (HH)") },
                            modifier = Modifier.weight(1f),
                        )
                        Text(":", modifier = Modifier.align(Alignment.CenterVertically))
                        OutlinedTextField(
                            value = minuteEndStation,
                            onValueChange = { newRouteStartTimeMinute ->
                                minuteEndStation = newRouteStartTimeMinute.take(2)
                                updateTimeErrorEnd()
                            },
                            isError = endTimeError,
                            label = { Text("Departure Time (MM)") },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Spacer(modifier = Modifier.height(height = 16.dp))

                    if (middles != null) {
                        Column {
                            middles!!.forEachIndexed { index, middle ->

                                var selectedMiddleStation by remember { mutableStateOf(middle.station) } // id
                                var hourMiddleStation by remember { mutableStateOf("") } // HH:mm
                                var minuteMiddleStation by remember { mutableStateOf("") }
                                var middleTimeError by remember { mutableStateOf(false) }

                                LaunchedEffect(middle) {
                                    val (hourMiddleStationParsed, minuteMiddleStationParsed) = parseShortTime(middle.time)
                                    hourMiddleStation = hourMiddleStationParsed
                                    minuteMiddleStation = minuteMiddleStationParsed
                                }

                                fun updateTimeErrorMiddle() {
                                    try {
                                        val newHour = hourMiddleStation.toIntOrNull()
                                        val newMinute = minuteMiddleStation.toIntOrNull()

                                        middleTimeError = when {
                                            newHour == null || newHour !in 0..23 -> true
                                            newMinute == null || newMinute !in 0..59 -> true
                                            else -> false
                                        }
                                    } catch (e: Exception) {
                                        middleTimeError = true
                                    }
                                }

                                Text("Middle Station ${index + 1}:", fontWeight = FontWeight.Bold)
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    StationsDropdownMenu(
                                        label = "Middle Station Name",
                                        options = allStationsRemove,
                                        originalSelection = selectedMiddleStation,
                                        onSelectionChange = { selectedNewStation ->
                                            selectedMiddleStation = selectedNewStation
                                            // middle.copy(station = selectedNewStation)
                                            middles = middles!!.toMutableList().apply {
                                                this[index] = middle.copy(station = selectedNewStation)
                                            }
                                        }
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth(0.8f)
                                            .padding(bottom = 8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = hourMiddleStation,
                                            onValueChange = { newRouteStartTimeHour ->
                                                hourMiddleStation = newRouteStartTimeHour.take(2)
                                                updateTimeErrorMiddle()
                                                // middle.copy(time = "$hourMiddleStation:$minuteMiddleStation")
                                                middles = middles!!.toMutableList().apply {
                                                    this[index] = middle.copy(time = "$hourMiddleStation:$minuteMiddleStation")
                                                }

                                            },
                                            isError = middleTimeError,
                                            label = { Text("Departure Time (HH)") },
                                            modifier = Modifier.weight(1f),
                                        )
                                        Text(":", modifier = Modifier.align(Alignment.CenterVertically))
                                        OutlinedTextField(
                                            value = minuteMiddleStation,
                                            onValueChange = { newRouteStartTimeMinute ->
                                                minuteMiddleStation = newRouteStartTimeMinute.take(2)
                                                updateTimeErrorMiddle()
                                                middles = middles!!.toMutableList().apply {
                                                    this[index] = middle.copy(time = "$hourMiddleStation:$minuteMiddleStation")
                                                }
                                            },
                                            isError = middleTimeError,
                                            label = { Text("Departure Time (MM)") },
                                            modifier = Modifier.weight(1f),
                                        )
                                    }
                                }
                            }
                        }
                    }

                    MiddleStationsInput(
                        newMiddles = newMiddles,
                        allStations = allStationsRemove,
                        onAddMiddle = { newMiddles = newMiddles + RouteStopInsert() },
                        onUpdateMiddle = { index, updatedMiddle ->
                            newMiddles = newMiddles.toMutableList().apply {
                                this[index] = updatedMiddle
                            }
                        }
                    )

                } else {
                    Text("Route Number: ${route.trainNumber}", fontWeight = FontWeight.Bold)
                    Text("Train Type: ${route.trainType}")
                    Text("Valid From: ${route.validFrom?.plusHours(2)?.format(formatter)}")
                    Text("Valid Until: ${route.validUntil?.plusHours(2)?.format(formatter)}")
                    Text("Can Support Bikes: ${if (route.canSupportBikes) "Yes" else "No"}")
                    val drivesOnDaysString = route.drivesOn.mapNotNull { daysOfWeekMap[it] }.joinToString(", ")
                    Text("Drives On: $drivesOnDaysString")
                    Text("Departure Station:")
                    Text(
                        text = "Station Name: ${allStations.find { it.second == selectedStartStation }?.first}",
                        modifier = Modifier.padding(start = 16.dp)
                    )
                    Text(
                        text = "Departure Time: ${route.start.time}",
                        modifier = Modifier.padding(start = 16.dp)
                    )
                    Text("Arrival Station:")
                    Text(
                        text = "Station Name: ${allStations.find { it.second == selectedEndStation }?.first}",
                        modifier = Modifier.padding(start = 16.dp)
                    )
                    Text(
                        text = "Arrival Time: ${route.end.time}",
                        modifier = Modifier.padding(start = 16.dp)
                    )
                    Text("Middle Stations:")
                    middles.forEachIndexed { index, middle ->
                        Text(
                            text = "${index + 1}: ${allStations.find { it.second == middle.station }?.first} (${middle.time})",
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }

                    Text("Route Created On: ${route.createdAt.plusHours(2).format(formatter)}", fontSize = 12.sp)
                    Text("Route Last Updated On: ${route.updatedAt.plusHours(2).format(formatter)}", fontSize = 12.sp)
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
                                val feedback = updateRouteInDB(
                                    route = route,
                                    trainType = trainType,
                                    trainNumber = trainNumber,
                                    validFromYear = validFromYear,
                                    validFromMonth = validFromMonth,
                                    validFromDay = validFromDay,
                                    validFromHour = validFromHour,
                                    validFromMinute = validFromMinute,
                                    validFromSecond = validFromSecond,
                                    validUntilYear = validUntilYear,
                                    validUntilMonth = validUntilMonth,
                                    validUntilDay = validUntilDay,
                                    validUntilHour = validUntilHour,
                                    validUntilMinute = validUntilMinute,
                                    validUntilSecond = validUntilSecond,
                                    canSupportBikes = canSupportBikes,
                                    drivesOnDays = drivesOnDays,
                                    selectedStartStation = selectedStartStation,
                                    hourStartStation = hourStartStation,
                                    minuteStartStation = minuteStartStation,
                                    selectedEndStation = selectedEndStation,
                                    hourEndStation = hourEndStation,
                                    minuteEndStation = minuteEndStation,
                                    middles = middles,
                                    newMiddles = newMiddles,
                                    onSuccess = onUpdateRouteSuccess
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
                            trainType = route.trainType
                            trainNumber = route.trainNumber
                            validFromYear = route.validFrom?.year.toString()
                            validFromMonth = route.validFrom?.monthValue.toString()
                            validFromDay = route.validFrom?.dayOfMonth.toString()
                            validFromHour = route.validFrom?.hour.toString()
                            validFromMinute = route.validFrom?.minute.toString()
                            validFromSecond = route.validFrom?.second.toString()
                            validUntilYear = route.validUntil?.year.toString()
                            validUntilMonth = route.validUntil?.monthValue.toString()
                            validUntilDay = route.validUntil?.dayOfMonth.toString()
                            validUntilHour = route.validUntil?.hour.toString()
                            validUntilMinute = route.validUntil?.minute.toString()
                            validUntilSecond = route.validUntil?.second.toString()
                            canSupportBikes = route.canSupportBikes
                            drivesOnDays.clear()
                            daysOfWeek.forEachIndexed { index, day ->
                                drivesOnDays[day] = route.drivesOn.contains(index)
                            }
                            selectedStartStation = route.start.station
                            val (hourStartStationParsed, minuteStartStationParsed) = parseShortTime(route.start.time)
                            hourStartStation = hourStartStationParsed
                            minuteStartStation = minuteStartStationParsed
                            selectedEndStation = route.end.station
                            val (hourEndStationParsed, minuteEndStationParsed) = parseShortTime(route.end.time)
                            hourEndStation = hourEndStationParsed
                            minuteEndStation = minuteEndStationParsed
                            middles = route.middle
                            newMiddles = listOf()
                            createdAt = route.createdAt
                            updatedAt = route.updatedAt
                            editMode = false
                        }
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = "Cancel")
                    }
                }

                if (!editMode) {
                    IconButton(
                        onClick = {
                            onDeleteRoute(route)
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

suspend fun deleteRouteFromDB(
    id: String,
    onSuccess: () -> Unit
): String {
    return try {
        coroutineScope {
            deleteRoute(id)
        }
        onSuccess()
        "Route successfully deleted from the database."
    } catch (e: Exception) {
        "Error removing route from the database. ${e.message}"
    }
}

suspend fun updateRouteInDB(
    route: Route,
    trainType: String,
    trainNumber: Int?,
    validFromYear: String,
    validFromMonth: String,
    validFromDay: String,
    validFromHour: String,
    validFromMinute: String,
    validFromSecond: String,
    validUntilYear: String,
    validUntilMonth: String,
    validUntilDay: String,
    validUntilHour: String,
    validUntilMinute: String,
    validUntilSecond: String,
    canSupportBikes: Boolean,
    drivesOnDays: Map<String, Boolean>,
    selectedStartStation: String,
    hourStartStation: String,
    minuteStartStation: String,
    selectedEndStation: String,
    hourEndStation: String,
    minuteEndStation: String,
    middles: List<RouteStop>,
    newMiddles: List<RouteStopInsert>,
    onSuccess: (Route) -> Unit
): String {
    if (trainNumber == null || trainNumber == 0) {
        return ("Route Number invalid.")
    }

    if (validFromYear.isEmpty() || validFromMonth.isEmpty() || validFromDay.isEmpty() || validFromHour.isEmpty() || validFromMinute.isEmpty() || validFromSecond.isEmpty()) {
        return ("Valid From format invalid.")
    }

    if (validUntilYear.isEmpty() || validUntilMonth.isEmpty() || validUntilDay.isEmpty() || validUntilHour.isEmpty() || validUntilMinute.isEmpty() || validUntilSecond.isEmpty()) {
        return ("Valid Until format invalid.")
    }

    val validFromDateTime: LocalDateTime
    val validUntilDateTime: LocalDateTime

    try {
        validFromDateTime = LocalDateTime.of(validFromYear.toInt(), validFromMonth.toInt(), validFromDay.toInt(), validFromHour.toInt(), validFromMinute.toInt(), validFromSecond.toInt())
    } catch (e: DateTimeParseException) {
        return ("Valid From format invalid.")
    }

    try {
        validUntilDateTime = LocalDateTime.of(validUntilYear.toInt(), validUntilMonth.toInt(), validUntilDay.toInt(), validUntilHour.toInt(), validUntilMinute.toInt(), validUntilSecond.toInt())
    } catch (e: DateTimeParseException) {
        return ("Valid Until format invalid.")
    }

    val drivesOn: List<Int> = drivesOnDays
        .filter { it.value } // Filter only checked
        .keys
        .mapNotNull { day -> daysOfWeek.indexOf(day).takeIf { it != -1 } }
        .sorted() // Sort low -> high


    val hourStart = hourStartStation.toIntOrNull()
    val minuteStart = minuteStartStation.toIntOrNull()
    if (hourStart == null || hourStart !in 0..23) {
        return "Invalid Route Departure Time: $hourStartStation"
    }
    if (minuteStart == null || minuteStart !in 0..59) {
        return "Invalid Route Departure Time: $minuteStartStation"
    }

    val hourEnd = hourEndStation.toIntOrNull()
    val minuteEnd = minuteEndStation.toIntOrNull()
    if (hourEnd == null || hourEnd !in 0..23) {
        return "Invalid Route Arrival Time: $hourStartStation"
    }
    if (minuteEnd == null || minuteEnd !in 0..59) {
        return "Invalid Route Arrival Time: $minuteStartStation"
    }

    val start = RouteStop(
        station = selectedStartStation,
        time = "${addLeadingZero(hourStart.toString())}:${addLeadingZero(minuteStart.toString())}"
    )

    val end = RouteStop(
        station = selectedEndStation,
        time = "${addLeadingZero(hourEnd.toString())}:${addLeadingZero(minuteEnd.toString())}"
    )

    /*
    * Filter Existing Middle Stations
    * */
    val filteredMiddles = middles.filter { it.station != "0" } // exclude with station set to Remove

    // Check if times are in the format HH:mm and within the range
    val invalidTimes = filteredMiddles.filter { stop ->
        val parts = stop.time.split(":")
        val hours = parts.getOrNull(0)?.toIntOrNull()
        val minutes = parts.getOrNull(1)?.toIntOrNull()

        // Add leading zeros if necessary
        val formattedTime = "${hours?.toString()?.padStart(2, '0')}:${minutes?.toString()?.padStart(2, '0')}"

        !formattedTime.matches(Regex("""\d{2}:\d{2}""")) ||
                hours !in 0..23 ||
                minutes !in 0..59
    }

    // If there are any invalid times, return error
    if (invalidTimes.isNotEmpty()) {
        //val invalidTimeStops = invalidTimes.joinToString(", ") { it.station }
        return "Invalid time format in Middle Stations."
    }

    /*
    * Filter New Middle Stations
    * */
    val filteredNewMiddles = newMiddles.filter { it.station != "0" }

    val invalidTimesNew = filteredNewMiddles.filter { stop ->
        val parts = stop.time.split(":")
        val hours = parts.getOrNull(0)?.toIntOrNull()
        val minutes = parts.getOrNull(1)?.toIntOrNull()

        val formattedTime = "${hours?.toString()?.padStart(2, '0')}:${minutes?.toString()?.padStart(2, '0')}"

        !formattedTime.matches(Regex("""\d{2}:\d{2}""")) ||
                hours !in 0..23 ||
                minutes !in 0..59
    }

    if (invalidTimesNew.isNotEmpty()) {
        return "Invalid time format in New Middle Stations."
    }

    val routeUpdate = RouteUpdate(
        id = route.id,
        trainType = trainType,
        trainNumber = trainNumber,
        validFrom = validFromDateTime,
        validUntil = validUntilDateTime,
        canSupportBikes = canSupportBikes,
        drivesOn = drivesOn,
        start = start,
        end = end,
        middle = filteredMiddles,
        newMiddle = filteredNewMiddles
    )

    return try {
        var updatedRoute: Route
        coroutineScope {
            updatedRoute = updateRoute(routeUpdate)
        }
        onSuccess(updatedRoute)
        "Route successfully updated in the database."
    } catch (e: Exception) {
        "Error updating route in the database. ${e.message}"
    }
}

@Composable
fun MiddleStationsInput(
    newMiddles: List<RouteStopInsert>,
    allStations: List<Pair<String, String>>,
    onAddMiddle: () -> Unit,
    onUpdateMiddle: (Int, RouteStopInsert) -> Unit
) {
    Column(modifier = Modifier) {
        newMiddles.forEachIndexed { index, middle ->
            MiddleStationInput(
                middle = middle,
                allStations = allStations,
                onMiddleChange = { updatedMiddle -> onUpdateMiddle(index, updatedMiddle) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = { onAddMiddle() },
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp)
        ) {
            Text("Add New Middle Station")
        }
    }
}

@Composable
fun MiddleStationInput(
    middle: RouteStopInsert,
    allStations: List<Pair<String, String>>,
    onMiddleChange: (RouteStopInsert) -> Unit
) {
    var selectedMiddleStation by remember { mutableStateOf(middle.station) } // id
    var hourMiddleStation by remember { mutableStateOf("") } // HH:mm
    var minuteMiddleStation by remember { mutableStateOf("") }
    var middleTimeError by remember { mutableStateOf(false) }

    fun updateTimeErrorMiddle() {
        try {
            val newHour = hourMiddleStation.toIntOrNull()
            val newMinute = minuteMiddleStation.toIntOrNull()

            middleTimeError = when {
                newHour == null || newHour !in 0..23 -> true
                newMinute == null || newMinute !in 0..59 -> true
                else -> false
            }
        } catch (e: Exception) {
            middleTimeError = true
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        StationsDropdownMenu(
            label = "New Middle Station Name",
            options = allStations,
            originalSelection = selectedMiddleStation,
            onSelectionChange = { selectedNewStation ->
                selectedMiddleStation = selectedNewStation
                onMiddleChange(middle.copy(station = selectedNewStation))
            }
        )
        Row(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(bottom = 8.dp)
        ) {
            OutlinedTextField(
                value = hourMiddleStation,
                onValueChange = { newRouteStartTimeHour ->
                    hourMiddleStation = newRouteStartTimeHour.take(2)
                    updateTimeErrorMiddle()
                    val formattedHour = addLeadingZero(hourMiddleStation)
                    val formattedMinute = addLeadingZero(minuteMiddleStation)
                    onMiddleChange(middle.copy(time = "$formattedHour:$formattedMinute"))
                },
                isError = middleTimeError,
                label = { Text("Departure Time (HH)") },
                modifier = Modifier.weight(1f),
            )
            Text(":", modifier = Modifier.align(Alignment.CenterVertically))
            OutlinedTextField(
                value = minuteMiddleStation,
                onValueChange = { newRouteStartTimeMinute ->
                    minuteMiddleStation = newRouteStartTimeMinute.take(2)
                    updateTimeErrorMiddle()
                    val formattedHour = addLeadingZero(hourMiddleStation)
                    val formattedMinute = addLeadingZero(minuteMiddleStation)
                    onMiddleChange(middle.copy(time = "$formattedHour:$formattedMinute"))
                },
                isError = middleTimeError,
                label = { Text("Departure Time (MM)") },
                modifier = Modifier.weight(1f),
            )
        }
    }
}