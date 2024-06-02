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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gui.CustomDropdownMenuInt
import gui.generateData.parts.DelayGenerateItem
import gui.scraper.insertAllDelaysFromScrapedListToDB
import gui.scraper.parts.DelayScraperItem
import gui.toNameIDPairs
import gui.toNumberIDPairs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import models.Delay
import models.DelayInsert
import models.Route
import models.Station
import utils.api.dao.getAllRoutes
import utils.api.dao.getAllStations
import utils.api.dao.insertDelay
import utils.api.dao.updateDelay

@Composable
fun GenerateDataDelayView(
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val isLoading = remember { mutableStateOf(true) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    var numberToGenerate by remember { mutableStateOf(1) }

    var delays by remember { mutableStateOf<List<DelayInsert>>(emptyList()) } // generated delays (DelayInsert)
    var allStations by remember { mutableStateOf<List<Station>>(emptyList()) } // generator - pick one at random
    var allStationsPairs by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var allRoutes by remember { mutableStateOf<List<Route>>(emptyList()) } // generator - pick one at random
    var allRoutesPairs by remember { mutableStateOf<List<Pair<Int, String>>>(emptyList()) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            isLoading.value = true
            try {
                allStations = withContext(Dispatchers.IO) { getAllStations() }
                allRoutes = withContext(Dispatchers.IO) { getAllRoutes() }
                allStationsPairs = allStations.toNameIDPairs()
                allRoutesPairs = allRoutes.toNumberIDPairs()
            } catch (e: Exception) {
                feedbackMessage = "Error: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun updateDelay(newDelay: DelayInsert, index: Int) {
        val updatedList = delays.toMutableList()
        if (index in updatedList.indices) {
            updatedList[index] = newDelay
            delays = updatedList
        }
    }

    fun insertDelay(success: Boolean, index: Int) {
        if (success) {
            val updatedList = delays.toMutableList()
            if (index in updatedList.indices) {
                updatedList.removeAt(index)
                delays = updatedList
                feedbackMessage = "Delay datapoint successfully inserted in the database."
            }
        }
    }

    fun removeDelay(index: Int) {
        val updatedList = delays.toMutableList()
        if (index in updatedList.indices) {
            updatedList.removeAt(index)
            delays = updatedList
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
    } else if (delays.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Choose number of Delays to generate",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
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
                        val (feedback, generatedDelays) = generateDelays(
                            delays = delays,
                            numberToGenerate = numberToGenerate,
                            allStations = allStationsPairs,
                            allRoutes = allRoutesPairs,
                            isLoading = isLoading
                        )
                        delays = generatedDelays
                        if (feedback != "") {
                            feedbackMessage = feedback
                        }
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(text = "Generate Delays")
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
                            text = "Generated Delays",
                            style = MaterialTheme.typography.h6,
                            modifier = Modifier
                                //.padding(bottom = 8.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val (feedback, generatedDelays) = insertAllDelaysFromGeneratedListToDB(
                                        delays = delays,
                                        isLoading = isLoading
                                    )
                                    delays = generatedDelays
                                    feedbackMessage = feedback
                                }
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(text = "Save all to the database")
                        }
                    }
                }
                itemsIndexed(delays) { index, delay ->
                    DelayGenerateItem(
                        delay = delay,
                        index = index,
                        allStations = allStationsPairs,
                        allRoutes = allRoutesPairs,
                        onInsertDelay = { success, index ->
                            insertDelay(success, index)
                        },
                        onUpdateDelay = { delayToUpdate, index ->
                            updateDelay(delayToUpdate, index)
                        },
                        onRemoveDelay = { index ->
                            removeDelay(index)
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