package gui.scraper

import ResultData
import ResultRoute
import ResultStations
import SourceWebsite
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import getDataAndProcess
import getRoutesAndProcess
import getStationsAndProcess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import utils.api.dao.insertRoute

@Composable
fun ScraperFetchData(
    sourceWebsite: SourceWebsite,
    modifier: Modifier = Modifier
) {
    // State to hold the result of the operation
    val resultDataState = remember { mutableStateOf(ResultData()) }

    // State to hold the loading status
    val isLoading = remember { mutableStateOf(false) }

    // LaunchedEffect to trigger the data fetching operation
    LaunchedEffect(Unit) {
        isLoading.value = true // Set loading to true before fetching data
        try {
            // Coroutine call - data fetch
            withContext(Dispatchers.IO) { getDataAndProcess(sourceWebsite, resultDataState) }
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
        Box(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()) // Box with enabled scroll
        ) {
            Text("Result: ${resultDataState.value.listOfTrainLocHistory}")
        }
        /*resultState.value?.let { result ->
            Text("Result: $result", modifier = Modifier.padding(16.dp))
        }*/
    }
}

@Composable
fun ScraperFetchStations(
    modifier: Modifier = Modifier
) {
    // State to hold the result of the operation
    val resultStateStations = remember { mutableStateOf<ResultStations>(ResultStations()) }

    // State to hold the loading status
    val isLoading = remember { mutableStateOf(false) }

    // LaunchedEffect to trigger the data fetching operation
    LaunchedEffect(Unit) {
        isLoading.value = true // Set loading to true before fetching data
        try {
            // Coroutine call - data fetch
            withContext(Dispatchers.IO) { getStationsAndProcess(resultStateStations) }
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
        Box(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()) // Box with enabled scroll
        ) {
            Text("Result: ${resultStateStations.value.listOfStations}")
        }
        /*resultState.value?.let { result ->
            Text("Result: $result", modifier = Modifier.padding(16.dp))
        }*/
    }
}

@Composable
fun ScraperFetchRoutes(
    modifier: Modifier = Modifier
) {
    // State to hold the result of the operation
    val resultStateRoutes = remember { mutableStateOf<ResultRoute>(ResultRoute()) }

    // State to hold the loading status
    val isLoading = remember { mutableStateOf(false) }

    // LaunchedEffect to trigger the data fetching operation
    LaunchedEffect(Unit) {
        isLoading.value = true // Set loading to true before fetching data
        try {
            // Coroutine call - data fetch
            withContext(Dispatchers.IO) { getRoutesAndProcess(resultStateRoutes) }
            withContext(Dispatchers.IO) {
                println("Inserting routes...")
                resultStateRoutes.value.listOfRoutes.forEach {
                    try {
                        insertRoute(it)
                    } catch (e: Exception){
                        println("Cannot insert: ${it.trainType} ${it.trainNumber}")
                    }
                }
                println("Done inserting routes")

            }

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
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()) // Box with enabled scroll
        ) {
            Text("Failed to get: ${resultStateRoutes.value.listOfFailedRoutes}")
            Text("Result: ${resultStateRoutes.value.listOfRoutes}")

            /*lazy {
                resultStateRoutes.value.listOfRoutes.forEach {
                    //insertRoute(it)
                }
            }*/
        }
    }
}