package gui.scraper

import ResultData
import SourceWebsite
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import getDataAndProcess
import gui.scraper.parts.DelayScraperItem
import gui.scraper.parts.TrainLocHistoryScraperItem
import gui.toNameIDPairs
import gui.toNumberIDPairs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import models.DelayInsert
import models.Route
import models.Station
import models.TrainLocHistoryInsert
import utils.api.dao.getAllRoutes
import utils.api.dao.getAllStations

@Composable
fun ScraperData(
    sourceWebsite: SourceWebsite,
    modifier: Modifier = Modifier
) {
    // State to hold the result of the operation
    val resultData = remember { mutableStateOf(ResultData()) }
    var allStations by remember { mutableStateOf<List<Station>>(emptyList()) } // for routeFrom/routeTo list
    var allStationsNames by remember { mutableStateOf<List<String>>(emptyList()) }
    var allStationsPairs by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var allRoutes by remember { mutableStateOf<List<Route>>(emptyList()) } // for route list
    var allRoutesNumbers by remember { mutableStateOf<List<Int>>(emptyList()) }
    var allRoutesPairs by remember { mutableStateOf<List<Pair<Int, String>>>(emptyList()) }
    val trainTypes = listOf(
        "AVT", "BUS", "EC", "EN", "IC", "ICS",
        "LP", "LPV", "LRG", "MO", "MV", "RG"
    )

    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    // State to hold the loading status
    val isLoading = remember { mutableStateOf(true) }

    // Replace original TrainLocHistoryInsert in list with updated one after editing
    fun updateTrainLocHistory(newTLC: TrainLocHistoryInsert, index: Int) {
        val updatedList = resultData.value.listOfTrainLocHistory.toMutableList()
        if (index in updatedList.indices) {
            updatedList[index] = newTLC
            resultData.value = resultData.value.copy(listOfTrainLocHistory = updatedList)
        }
    }

    // Replace original DelayInsert in list with updated one after editing
    fun updateDelay(newDelay: DelayInsert, index: Int) {
        val updatedList = resultData.value.listOfDelay.toMutableList()
        if (index in updatedList.indices) {
            updatedList[index] = newDelay
            resultData.value = resultData.value.copy(listOfDelay = updatedList)
        }
    }

    // Delete TrainLocHistoryInsert from resultStations.value.listOfTrainLocHistory after successful insert
    fun insertTrainLocHistory(success: Boolean, index: Int) {
        if (success) {
            val updatedList = resultData.value.listOfTrainLocHistory.toMutableList()
            if (index in updatedList.indices) {
                updatedList.removeAt(index)
                resultData.value = resultData.value.copy(listOfTrainLocHistory = updatedList)
                feedbackMessage = "Train Location History datapoint successfully inserted in the database."
            }
        }
    }

    // Delete DelayInsert from resultStations.value.listOfDelay after successful insert
    fun insertDelay(success: Boolean, index: Int) {
        if (success) {
            val updatedList = resultData.value.listOfDelay.toMutableList()
            if (index in updatedList.indices) {
                updatedList.removeAt(index)
                resultData.value = resultData.value.copy(listOfDelay = updatedList)
                feedbackMessage = "Delay datapoint successfully inserted in the database."
            }
        }
    }

    // LaunchedEffect to trigger the data fetching operation
    LaunchedEffect(Unit) {
        try {
            // Coroutine call - data fetch
            allStations = withContext(Dispatchers.IO) { getAllStations() }
            allRoutes = withContext(Dispatchers.IO) { getAllRoutes() }
            allStationsNames = allStations.map { it.name }
            allRoutesNumbers = allRoutes.map { it.trainNumber }
            allStationsPairs = allStations.toNameIDPairs()
            allRoutesPairs = allRoutes.toNumberIDPairs()
            withContext(Dispatchers.IO) { getDataAndProcess(sourceWebsite, resultData) }
        } catch (e: Exception) {
            println("Error: ${e.message}")
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
        if (resultData.value.error != "") {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Text("Failed to load data: ${resultData.value.error}")
            }
        } else {
            val state = rememberLazyListState()

            println(resultData.value.listOfTrainLocHistory)
            println(resultData.value.listOfDelay)

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
                                text = "Train Location History Data",
                                style = MaterialTheme.typography.h6,
                                modifier = Modifier
                                    //.padding(bottom = 8.dp)
                                    .align(Alignment.CenterHorizontally)
                            )
                            if (resultData.value.listOfDelay.isEmpty()) {
                                Text(
                                    text = "*** NO TRAIN LOCATION HISTORY DATA ***",
                                    style = MaterialTheme.typography.h6,
                                    modifier = Modifier
                                        //.padding(bottom = 8.dp)
                                        .align(Alignment.CenterHorizontally)
                                )
                            } else {
                                Button(
                                    onClick = { /* Handle save all action */ },
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Text(text = "Save all to the database")
                                }
                            }
                        }
                    }
                    // indexed items for updating original result list
                    itemsIndexed(resultData.value.listOfTrainLocHistory) { index, tlh ->
                        TrainLocHistoryScraperItem(
                            train = tlh,
                            index = index,
                            trainTypes = trainTypes,
                            allStations = allStationsNames,
                            allRoutes = allRoutesNumbers,
                            onInsertTrainLocHistory = { success, index ->
                                insertTrainLocHistory(success, index)
                            },
                            onUpdateTrainLocHistory = { tlhToUpdate, index ->
                                updateTrainLocHistory(tlhToUpdate, index)
                            }
                        )
                    }

                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Delay Data",
                                style = MaterialTheme.typography.h6,
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .align(Alignment.CenterHorizontally)
                            )
                            if (resultData.value.listOfDelay.isEmpty()) {
                                Text(
                                    text = "*** NO DELAY DATA ***",
                                    style = MaterialTheme.typography.h6,
                                    modifier = Modifier
                                        .align(Alignment.CenterHorizontally)
                                )
                            } else {
                                Button(
                                    onClick = { /* Handle save all action */ },
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Text(text = "Save all to the database")
                                }
                            }

                        }
                    }

                    itemsIndexed(resultData.value.listOfDelay) { index, delay ->
                        DelayScraperItem(
                            delay = delay,
                            index = index,
                            allStations = allStationsPairs,
                            allRoutes = allRoutesPairs,
                            onInsertDelay = { success, index ->
                                insertDelay(success, index)
                            },
                            onUpdateDelay = { delayToUpdate, index ->
                                updateDelay(delayToUpdate, index)
                            }
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

