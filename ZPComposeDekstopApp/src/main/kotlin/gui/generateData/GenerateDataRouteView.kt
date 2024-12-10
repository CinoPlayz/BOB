package gui.generateData

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gui.CustomDropdownMenuInt
import gui.generateData.engine.generateRoutes
import gui.generateData.engine.insertAllRoutesFromGeneratedListToDB
import gui.generateData.parts.RouteGenerateItem
import gui.toNameIDPairs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import models.RouteInsert
import models.Station
import utils.api.dao.getAllStations

@Composable
fun GenerateDataRouteView(
    modifier: Modifier = Modifier
){
    val coroutineScope = rememberCoroutineScope()
    val isLoading = remember { mutableStateOf(true) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    var numberToGenerate by remember { mutableStateOf(1) }

    var routes by remember { mutableStateOf<List<RouteInsert>>(emptyList()) }
    var allStations by remember { mutableStateOf<List<Station>>(emptyList()) } // for start/end/middle stations list
    var allStationsPairs by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    val trainTypes = listOf(
        "AVT", "BUS", "EC", "EN", "IC", "ICS",
        "LP", "LPV", "LRG", "MO", "MV", "RG"
    )

    fun updateRoute(newRoute: RouteInsert, index: Int) {
        val updatedList = routes.toMutableList()
        if (index in updatedList.indices) {
            updatedList[index] = newRoute
            routes = updatedList
        }
    }

    fun insertRoute(success: Boolean, index: Int) {
        if (success) {
            val updatedList = routes.toMutableList()
            if (index in updatedList.indices) {
                updatedList.removeAt(index)
                routes = updatedList
                feedbackMessage = "Route successfully inserted in the database."
            }
        }
    }

    fun removeRoute(index: Int) {
        val updatedList = routes.toMutableList()
        if (index in updatedList.indices) {
            updatedList.removeAt(index)
            routes = updatedList
        }
    }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            isLoading.value = true
            try {
                allStations = withContext(Dispatchers.IO) { getAllStations() }
                allStationsPairs = allStations.toNameIDPairs()
            } catch (e: Exception) {
                feedbackMessage = "Error: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    if (isLoading.value) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .padding(16.dp)
            ) {
                CircularProgressIndicator()
            }
        }
    } else if (routes.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Choose number of Routes to generate",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    CustomDropdownMenuInt(
                        label = "Number to Generate",
                        options = (1..30).toList(),
                        initialSelection = numberToGenerate,
                        onSelectionChange = { selectedNumberToGenerate ->
                            numberToGenerate = selectedNumberToGenerate
                        }
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }
            Button(
                onClick = {
                    coroutineScope.launch {
                        val (feedback, generatedRoutes) = generateRoutes(
                            routes = routes,
                            trainTypes = trainTypes,
                            numberToGenerate = numberToGenerate,
                            allStations = allStationsPairs,
                            isLoading = isLoading
                        )
                        routes = generatedRoutes
                        if (feedback != "") {
                            feedbackMessage = feedback
                        }
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(text = "Generate Routes")
            }
        }
    } else {
        val state = rememberLazyListState()

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(16.dp),
                state = state
            ) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Generated Routes",
                            style = MaterialTheme.typography.h6,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                        )
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val (feedback, insertedRoutes) = insertAllRoutesFromGeneratedListToDB(
                                        routes = routes,
                                        isLoading = isLoading
                                    )
                                    routes = insertedRoutes
                                    feedbackMessage = feedback
                                }
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(text = "Save all to the database")
                        }
                        Button(
                            onClick = {
                                routes = emptyList()
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(text = "Reset")
                        }
                    }
                }
                itemsIndexed(routes) { index, route ->
                    RouteGenerateItem(
                        route = route,
                        index = index,
                        trainTypes = trainTypes,
                        allStations = allStationsPairs,
                        onInsertRoute = { success, index ->
                            insertRoute(success, index)
                        },
                        onUpdateRoute = { routeToUpdate, index ->
                            updateRoute(routeToUpdate, index)
                        },
                        onRemoveRoute = { index ->
                            removeRoute(index)
                        }
                    )
                }
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