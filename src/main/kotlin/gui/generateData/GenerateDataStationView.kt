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
import gui.generateData.engine.generateStations
import gui.generateData.engine.insertAllStationsFromGeneratedListToDB
import gui.generateData.parts.StationGenerateItem
import kotlinx.coroutines.launch
import models.StationInsert

@Composable
fun GenerateDataStationView(
    modifier: Modifier = Modifier
){
    val coroutineScope = rememberCoroutineScope()
    val isLoading = remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    var numberToGenerate by remember { mutableStateOf(1) }

    var stations by remember { mutableStateOf<List<StationInsert>>(emptyList()) } // generated stations

    fun updateStation(newStation: StationInsert, index: Int) {
        val updatedList = stations.toMutableList()
        if (index in updatedList.indices) {
            updatedList[index] = newStation
            stations = updatedList
        }
    }

    fun insertStation(success: Boolean, index: Int) {
        if (success) {
            val updatedList = stations.toMutableList()
            if (index in updatedList.indices) {
                updatedList.removeAt(index)
                stations = updatedList
                feedbackMessage = "Station successfully inserted in the database."
            }
        }
    }

    fun removeStation(index: Int) {
        val updatedList = stations.toMutableList()
        if (index in updatedList.indices) {
            updatedList.removeAt(index)
            stations = updatedList
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
    } else if (stations.isEmpty()) {
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
                        val (feedback, generatedStations) = generateStations(
                            stations = stations,
                            numberToGenerate = numberToGenerate,
                            isLoading = isLoading
                        )
                        stations = generatedStations
                        if (feedback != "") {
                            feedbackMessage = feedback
                        }
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(text = "Generate Stations")
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
                                    val (feedback, insertedStations) = insertAllStationsFromGeneratedListToDB(
                                        stations = stations,
                                        isLoading = isLoading
                                    )
                                    stations = insertedStations
                                    feedbackMessage = feedback
                                }
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(text = "Save all to the database")
                        }
                        Button(
                            onClick = {
                                stations = emptyList()
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(text = "Reset")
                        }
                    }
                }
                itemsIndexed(stations) { index, station ->
                    StationGenerateItem(
                        station = station,
                        index = index,
                        onInsertStation = { success, index ->
                            insertStation(success, index)
                        },
                        onUpdateStation = { stationToUpdate, index ->
                            updateStation(stationToUpdate, index)
                        },
                        onRemoveStation = { index ->
                            removeStation(index)
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