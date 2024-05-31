package gui.dataProcessor

import TitleText
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoDatabase
import utils.DatabaseUtil.connectDB

enum class DataSourceInDB(val customName: String) {
    OFFICIAL("Podatki SŽ"),
    VLAKSI("Podatki Vlak.Si"),
    RESET("Reset")
}

@Composable
fun DataProcessor(
    modifier: Modifier = Modifier,
    dataProcessorMenuState: MutableState<DataSourceInDB> = remember { mutableStateOf(DataSourceInDB.RESET) },
    buttonPadding: Int = 10,
    iconSize: Int = 20,
    fontSize: Int = 16,
    iconTextSpace: Int = 6,
) {
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var mongoClient by remember { mutableStateOf<MongoClient?>(null) }
    val isLoading = remember { mutableStateOf(true) }
    var database by remember { mutableStateOf<MongoDatabase?>(null) }

    LaunchedEffect(Unit) {
        isLoading.value = true
        try {
            mongoClient = connectDB()
            if (mongoClient != null) {
                database = mongoClient!!.getDatabase("ZP")
                //feedbackMessage = "Database connected successfully."
            } else {
                feedbackMessage = "Failed to connect to the database."
            }
        } catch (e: Exception) {
            feedbackMessage = "Error connecting to database. ${e.message}"
        } finally {
            isLoading.value = false
        }
    }

    // val collection = database?.getCollection("logs")

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

    } else if (database != null) {
        Column(modifier) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = modifier
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { dataProcessorMenuState.value = DataSourceInDB.OFFICIAL }
                            .background(if (dataProcessorMenuState.value == DataSourceInDB.OFFICIAL) Color.LightGray else Color.Transparent)
                            .padding(vertical = buttonPadding.dp)
                            .align(Alignment.CenterVertically)
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    ) {
                        Icon(
                            Icons.Default.Train,
                            contentDescription = DataSourceInDB.OFFICIAL.name,
                            modifier = Modifier
                                .size(size = iconSize.dp)
                                .align(Alignment.CenterVertically)
                        )
                        Spacer(modifier = Modifier.width(iconTextSpace.dp))
                        Text(
                            text = DataSourceInDB.OFFICIAL.customName,
                            textAlign = TextAlign.Center,
                            fontSize = fontSize.sp
                        )
                    }
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { dataProcessorMenuState.value = DataSourceInDB.VLAKSI }
                            .background(if (dataProcessorMenuState.value == DataSourceInDB.VLAKSI) Color.LightGray else Color.Transparent)
                            .padding(vertical = buttonPadding.dp)
                            .align(Alignment.CenterVertically)
                            .wrapContentWidth(Alignment.CenterHorizontally)

                    ) {
                        Icon(
                            Icons.Default.Subway,
                            contentDescription = DataSourceInDB.VLAKSI.name,
                            modifier = Modifier
                                .size(size = iconSize.dp)
                                .align(Alignment.CenterVertically)
                        )
                        Spacer(modifier = Modifier.width(iconTextSpace.dp))
                        Text(
                            text = DataSourceInDB.VLAKSI.customName,
                            textAlign = TextAlign.Center,
                            fontSize = fontSize.sp
                        )
                    }
                    Row(
                        modifier = Modifier
                            .width(50.dp)
                            .clickable { dataProcessorMenuState.value = DataSourceInDB.RESET }
                            .background(Color.Transparent)
                            .padding(vertical = buttonPadding.dp)
                            .align(Alignment.CenterVertically)
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = DataSourceInDB.RESET.name,
                            modifier = Modifier
                                .size(size = iconSize.dp)
                                .align(Alignment.CenterVertically)
                        )
                        Text(
                            text = "",
                            textAlign = TextAlign.Center,
                            fontSize = fontSize.sp
                        )
                    }
                }
            }
            Divider(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(
                        start = 0.dp,
                        top = 0.dp,
                        end = 0.dp,
                        bottom = 0.dp
                    )
            ) {
                when (dataProcessorMenuState.value) {
                    DataSourceInDB.OFFICIAL -> DataProcessorView(DataSourceInDB.OFFICIAL, database!!)
                    DataSourceInDB.VLAKSI -> DataProcessorView(DataSourceInDB.VLAKSI, database!!)
                    DataSourceInDB.RESET -> ProcessDataReset()
                }
            }
        }
    } else {
        Surface(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                TitleText(
                    text = "Database connection failed.",
                    fontSize = 20,
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
fun ProcessDataReset(
    modifier: Modifier = Modifier,
    titleFontSize: Int = 20,
) {
    Column(
        modifier = Modifier.fillMaxSize()
            .fillMaxSize()
            .wrapContentSize(Alignment.Center),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TitleText(
            text = "Choose data source to process",
            fontSize = titleFontSize,
        )
    }
}