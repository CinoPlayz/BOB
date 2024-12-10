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
import gui.generateData.engine.generateTLHs
import gui.generateData.engine.insertAllTLHsFromGeneratedListToDB
import gui.generateData.parts.TLHGenerateItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import models.Route
import models.Station
import models.TrainLocHistoryInsert
import utils.api.dao.getAllRoutes
import utils.api.dao.getAllStations

@Composable
fun GenerateDataTLHView(
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val isLoading = remember { mutableStateOf(true) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    var numberToGenerate by remember { mutableStateOf(1) }

    var tlhs by remember { mutableStateOf<List<TrainLocHistoryInsert>>(emptyList()) } // generated TrainLocHistories (TrainLocHistoryInsert)
    var allStations by remember { mutableStateOf<List<Station>>(emptyList()) } // generator - pick one at random
    var allStationsNames by remember { mutableStateOf<List<String>>(emptyList()) }
    var allRoutes by remember { mutableStateOf<List<Route>>(emptyList()) } // for route list
    var allRoutesNumbers by remember { mutableStateOf<List<Int>>(emptyList()) }

    val trainTypes = listOf(
        "AVT", "BUS", "EC", "EN", "IC", "ICS",
        "LP", "LPV", "LRG", "MO", "MV", "RG"
    )

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            isLoading.value = true
            try {
                allStations = withContext(Dispatchers.IO) { getAllStations() }
                allRoutes = withContext(Dispatchers.IO) { getAllRoutes() }
                allStationsNames = allStations.map { it.name }
                allRoutesNumbers = allRoutes.map { it.trainNumber }
            } catch (e: Exception) {
                feedbackMessage = "Error: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun updateTLH(newTLH: TrainLocHistoryInsert, index: Int) {
        val updatedList = tlhs.toMutableList()
        if (index in updatedList.indices) {
            updatedList[index] = newTLH
            tlhs = updatedList
        }
    }

    fun insertTLH(success: Boolean, index: Int) {
        if (success) {
            val updatedList = tlhs.toMutableList()
            if (index in updatedList.indices) {
                updatedList.removeAt(index)
                tlhs = updatedList
                feedbackMessage = "TLH datapoint successfully inserted in the database."
            }
        }
    }

    fun removeTLH(index: Int) {
        val updatedList = tlhs.toMutableList()
        if (index in updatedList.indices) {
            updatedList.removeAt(index)
            tlhs = updatedList
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
    } else if (tlhs.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Choose number of Train Location Histories to generate",
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
                        val (feedback, generatedTLHs) = generateTLHs(
                            tlhs = tlhs,
                            trainTypes = trainTypes,
                            numberToGenerate = numberToGenerate,
                            allStations = allStationsNames,
                            allRoutes = allRoutesNumbers,
                            isLoading = isLoading
                        )
                        tlhs = generatedTLHs
                        if (feedback != "") {
                            feedbackMessage = feedback
                        }
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(text = "Generate Train Location Histories")
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
                                    val (feedback, insertedTLHs) = insertAllTLHsFromGeneratedListToDB(
                                        tlhs = tlhs,
                                        isLoading = isLoading
                                    )
                                    tlhs = insertedTLHs
                                    feedbackMessage = feedback
                                }
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(text = "Save all to the database")
                        }
                        Button(
                            onClick = {
                                tlhs = emptyList()
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(text = "Reset")
                        }
                    }
                }
                itemsIndexed(tlhs) { index, tlh ->
                    TLHGenerateItem(
                        train = tlh,
                        index = index,
                        trainTypes = trainTypes,
                        allStations = allStationsNames,
                        allRoutes = allRoutesNumbers,
                        onInsertTrainLocHistory = { success, index ->
                            insertTLH(success, index)
                        },
                        onUpdateTrainLocHistory = { delayToUpdate, index ->
                            updateTLH(delayToUpdate, index)
                        },
                        onRemoveTrainLocHistory = { index ->
                            removeTLH(index)
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