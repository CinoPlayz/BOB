package gui.dataProcessor

import TitleText
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import kotlinx.coroutines.launch
import org.bson.Document
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun DataProcessorView(
    source: DataSourceInDB,
    database: MongoDatabase,
    modifier: Modifier = Modifier,
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val isLoading = remember { mutableStateOf(true) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var collection by remember { mutableStateOf<MongoCollection<Document>?>(null) }
    var maxTimeOfRequest by remember { mutableStateOf<LocalDateTime?>(null) }
    var minTimeOfRequest by remember { mutableStateOf<LocalDateTime?>(null) }
    var numberOfAllDocumentsInDB by remember { mutableStateOf<Long?>(null) }

    var fromYear by remember { mutableStateOf("2024") }
    var fromMonth by remember { mutableStateOf("04") }
    var fromDay by remember { mutableStateOf("15") }
    var toYear by remember { mutableStateOf("2024") }
    var toMonth by remember { mutableStateOf("04") }
    var toDay by remember { mutableStateOf("16") }

    // Reset after successful data processing
    val onSuccess: (String) -> Unit = { message ->
        feedbackMessage = message
        val maxDoc = collection!!.find()
            .sort(Document("timeOfRequest", -1))
            .limit(1)
            .first()
        maxTimeOfRequest = maxDoc?.getDate("timeOfRequest")?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
        val minDoc = collection!!.find()
            .sort(Document("timeOfRequest", 1))
            .limit(1)
            .first()
        minTimeOfRequest = minDoc?.getDate("timeOfRequest")?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
        numberOfAllDocumentsInDB = collection!!.countDocuments()
        fromYear = "2024"
        fromMonth = ""
        fromDay = ""
        toYear = "2024"
        toMonth = ""
        toDay = ""
    }

    val onFailure: (String) -> Unit = { message ->
        feedbackMessage = message
    }

    LaunchedEffect(Unit) {
        if (source == DataSourceInDB.OFFICIAL) {
            collection = database.getCollection("AktivniVlakiOdOfficial")
        } else if (source == DataSourceInDB.VLAKSI) {
            collection = database.getCollection("AktivniVlakiOdVlaksi")
        }
        try {
            val maxDoc = collection!!.find()
                .sort(Document("timeOfRequest", -1))
                .limit(1)
                .first()
            maxTimeOfRequest = maxDoc?.getDate("timeOfRequest")?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime()

            val minDoc = collection!!.find()
                .sort(Document("timeOfRequest", 1))
                .limit(1)
                .first()
            minTimeOfRequest = minDoc?.getDate("timeOfRequest")?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime()

            numberOfAllDocumentsInDB = collection!!.countDocuments()
        } catch (e: Exception) {
            feedbackMessage = "Error loading data from collection. ${e.message}"
        } finally {
            isLoading.value = false
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

    } else if (numberOfAllDocumentsInDB?.toInt() != 0 && numberOfAllDocumentsInDB != null) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "All Datapoints in DB:",
                fontWeight = FontWeight.Bold
            )
            Text("$numberOfAllDocumentsInDB")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Lowest Datapoint Date:",
                        fontWeight = FontWeight.Bold
                    )
                    Text("${minTimeOfRequest?.format(formatter)}")
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Highest Datapoint Date:",
                        fontWeight = FontWeight.Bold
                    )
                    Text("${maxTimeOfRequest?.format(formatter)}")
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("Choose time window between")
            Text("${minTimeOfRequest?.format(formatter)} and ${maxTimeOfRequest?.format(formatter)}")
            Text("for data conversion.")
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedTextField(
                    value = fromYear,
                    onValueChange = { newValue ->
                        fromYear = newValue.take(4)
                    },
                    label = { Text("YYYY") },
                    modifier = Modifier.weight(1f)
                )
                Text("-", modifier = Modifier.align(Alignment.CenterVertically))
                OutlinedTextField(
                    value = fromMonth,
                    onValueChange = { newValue ->
                        fromMonth = newValue.take(2)
                    },
                    label = { Text("MM") },
                    modifier = Modifier.weight(1f)
                )
                Text("-", modifier = Modifier.align(Alignment.CenterVertically))
                OutlinedTextField(
                    value = fromDay,
                    onValueChange = { newValue ->
                        fromDay = newValue.take(2)
                    },
                    label = { Text("DD") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(16.dp))
                OutlinedTextField(
                    value = toYear,
                    onValueChange = { newValue ->
                        toYear = newValue.take(4)
                    },
                    label = { Text("YYYY") },
                    modifier = Modifier.weight(1f)
                )
                Text("-", modifier = Modifier.align(Alignment.CenterVertically))
                OutlinedTextField(
                    value = toMonth,
                    onValueChange = { newValue ->
                        toMonth = newValue.take(2)
                    },
                    label = { Text("MM") },
                    modifier = Modifier.weight(1f)
                )
                Text("-", modifier = Modifier.align(Alignment.CenterVertically))
                OutlinedTextField(
                    value = toDay,
                    onValueChange = { newValue ->
                        toDay = newValue.take(2)
                    },
                    label = { Text("DD") },
                    modifier = Modifier.weight(1f)
                )
            }
            ProcessDataButton(
                source = source,
                database = database,
                collection = collection,
                minDateTime = minTimeOfRequest,
                maxDateTime = maxTimeOfRequest,
                fromYear = fromYear,
                fromMonth = fromMonth,
                fromDay = fromDay,
                toYear = toYear,
                toMonth = toMonth,
                toDay = toDay,
                onFailure = onFailure,
                onSuccess = onSuccess
            )
            /*Button(
                onClick = {
                    coroutineScope.launch {
                        val feedback = dataProcessorEngine(
                            source = source,
                            collection = collection,
                            fromYear = fromYear,
                            fromMonth = fromMonth,
                            fromDay = fromDay,
                            toYear = toYear,
                            toMonth = toMonth,
                            toDay = toDay,
                            onSuccess = onSuccess
                        )

                        feedbackMessage = feedback
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Text("Process data in selected time frame")
            }*/
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
                    text = "Database is empty.",
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
fun ProcessDataButton(
    source: DataSourceInDB,
    database: MongoDatabase,
    collection: MongoCollection<Document>?,
    minDateTime: LocalDateTime?,
    maxDateTime: LocalDateTime?,
    fromYear: String,
    fromMonth: String,
    fromDay: String,
    toYear: String,
    toMonth: String,
    toDay: String,
    onSuccess: (String) -> Unit,
    onFailure: (String) -> Unit,
) {
    var feedbackMessage = remember { mutableStateOf("") }
    var feedbackPopup by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Button(
        onClick = {
            coroutineScope.launch {
                if (collection != null) {
                    try {
                        when (val result = dataProcessorEngine(
                            source,
                            database,
                            collection,
                            minDateTime,
                            maxDateTime,
                            fromYear,
                            fromMonth,
                            fromDay,
                            toYear,
                            toMonth,
                            toDay,
                            updateFeedback = { feedbackMessage.value = it }
                        )) {
                            is Result.Success -> onSuccess(result.data)
                            is Result.Failure -> onFailure(result.message)
                        }
                        /*dataProcessorEngine(
                            source,
                            collection,
                            minDateTime,
                            maxDateTime,
                            fromYear,
                            fromMonth,
                            fromDay,
                            toYear,
                            toMonth,
                            toDay,
                            onSuccess,
                            onFailure,
                            updateFeedback = { feedbackMessage.value = it }
                        )*/
                    } catch (e: Exception) {
                        onSuccess("Error processing data: ${e.message}")
                    }
                } else {
                    onSuccess("No collection selected.")
                }
            }
        },
        enabled = collection != null
    ) {
        Text("Process data in selected time frame")
    }


    /*Button(
        onClick = {
            coroutineScope.launch {
                val feedback = dataProcessorEngine(
                    source = source,
                    collection = collection,
                    minDateTime = minDateTime,
                    maxDateTime = maxDateTime,
                    fromYear = fromYear,
                    fromMonth = fromMonth,
                    fromDay = fromDay,
                    toYear = toYear,
                    toMonth = toMonth,
                    toDay = toDay,
                    onSuccess = onSuccess,
                    updateFeedback = { feedbackMessage.value = it }
                )
                if (feedback != "") {
                    feedbackPopup = feedback
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    ) {
        Text("Process data in selected time frame")
    }*/


    Spacer(modifier = Modifier.height(16.dp))
    // Display processing feedback
    Text(feedbackMessage.value)

    feedbackPopup?.let { message ->
        AlertDialog(
            onDismissRequest = { feedbackPopup = null },
            text = { Text(message) },
            confirmButton = {
                Button(
                    onClick = { feedbackPopup = null },
                ) {
                    Text("OK")
                }
            }
        )
    }
}

