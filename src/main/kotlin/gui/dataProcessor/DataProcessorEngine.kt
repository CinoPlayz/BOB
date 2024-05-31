package gui.dataProcessor

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import models.*
import org.bson.Document
import org.bson.types.ObjectId
import utils.api.dao.insertDelay
import utils.api.dao.insertTrainLocHistory
import utils.parsing.getDecodedData
import java.time.DateTimeException
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Failure(val message: String) : Result<Nothing>()
}

suspend fun dataProcessorEngine(
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
    //onSuccess: (String) -> Unit,
    //onFailure: (String) -> Unit,
    updateFeedback: (String) -> Unit
): Result<String> {
    return withContext(Dispatchers.IO) {
        if (fromYear.toIntOrNull() == null || fromMonth.toIntOrNull() == null || fromDay.toIntOrNull() == null) {
            return@withContext Result.Failure("From Date format invalid.")
        }
        if (toYear.toIntOrNull() == null || toMonth.toIntOrNull() == null || toDay.toIntOrNull() == null) {
            return@withContext Result.Failure("To Date format invalid.")
        }

        val fromTimeStamp: LocalDateTime
        try {
            fromTimeStamp = LocalDateTime.of(
                fromYear.toInt(),
                fromMonth.toInt(),
                fromDay.toInt(),
                "00".toInt(),
                "00".toInt(),
                "00".toInt()
            )
        } catch (e: DateTimeException) {
            return@withContext Result.Failure("From Date format invalid.")
        }

        val toTimeStamp: LocalDateTime
        try {
            toTimeStamp = LocalDateTime.of(
                toYear.toInt(),
                toMonth.toInt(),
                toDay.toInt(),
                "23".toInt(),
                "59".toInt(),
                "59".toInt()
            )
        } catch (e: DateTimeException) {
            return@withContext Result.Failure("To Date format invalid.")
        }

        if (fromTimeStamp > toTimeStamp) {
            return@withContext Result.Failure("From Date larger than To Date.")
        }

        updateFeedback("Getting documents from the database")
        val json = Json { ignoreUnknownKeys = true }
        val startTimestamp = Date.from(fromTimeStamp.atZone(ZoneId.systemDefault()).toInstant())
        val endTimestamp = Date.from(toTimeStamp.atZone(ZoneId.systemDefault()).toInstant())

        val query = Document("timeOfRequest", Document("\$gte", startTimestamp).append("\$lte", endTimestamp))

        val documents = collection?.find(query)?.toList()

        val numberOfDocuments = documents?.size
        if (numberOfDocuments == 0) {
            updateFeedback("No data to process in selected time frame.")
            return@withContext Result.Failure("No data to process in selected time frame.")
        }

        // Convert to JSON String and add start and stop [ and ] - for processing
        val jsonDocuments = "[" + documents?.joinToString(",") { document ->
            document.toJson()
        } + "]"
        val data = json.decodeFromString<List<DatabaseRequest>>(jsonDocuments) // save document data for processing
        val ids = documents?.map { it.getObjectId("_id").toString() } // save ids for moving processed documents
        val dataWithIDs = ids?.zip(data) // combine ids and data for processing

        //println(jsonDocuments)
        //println(data)
        /*if (ids != null) {
            print(ids.size)
        }*/

        //val documentsString = documents.toString()
        //val data = json.decodeFromString<List<DatabaseRequest>>(documentsString)

        //println(numberOfDocuments)


        var trainLocHistoryCounter = 0
        var delayCounter = 0
        var documentErrorCounter = 0

        if (source == DataSourceInDB.OFFICIAL) {
            var index = 1
            val targetCollection = database.getCollection("ProcessedVlakiOdOfficial")
            dataWithIDs?.forEach { (id, document) ->
                // Progress Update
                updateFeedback("Processing document $index of $numberOfDocuments.")
                index++

                val trainLocHistories = mutableListOf<TrainLocHistoryInsert>()
                val delays = mutableListOf<DelayInsert>()

                try {
                    val urlDecodedData = getDecodedData(document.data)
                    val dataOfficial: List<Official> = json.decodeFromString(urlDecodedData)
                    val convertedRequest = OfficialRequest(document.timeOfRequest.date, dataOfficial)
                    val listTLH = convertedRequest.toListTrainLocHistory()
                    val listDelay = convertedRequest.toListDelay()

                    listTLH.forEach {
                        // try-catch block for each insert (most common error: empty nextStation)
                        try {
                            insertTrainLocHistory(it) // insert to database
                            //trainLocHistories.add(it)
                        } catch (e: Exception) {
                            //println("TLH E: ${e.message}")
                        }
                        trainLocHistoryCounter++

                    }
                    /*listTLH.forEach {
                        insertTrainLocHistory(it) // insert to database
                        //trainLocHistories.add(it)
                        trainLocHistoryCounter++
                    }*/

                    listDelay.forEach {
                        try {
                            insertDelay(it) // insert to database
                            //delays.add(it)
                        } catch (e: Exception) {
                            //println("Delay E: ${e.message}")
                        }
                        delayCounter++
                    }

                    /*listDelay.forEach {
                        insertDelay(it) // insert to database
                        //delays.add(it)
                        delayCounter++
                    }*/

                    // Move processed document = mark as processed
                    moveAndDeleteDocumentInDB(
                        collection,
                        targetCollection,
                        id
                    )
                } catch (e: Exception) {
                    documentErrorCounter++

                    // Move processed document with error = mark as processed
                    moveAndDeleteDocumentInDB(
                        collection,
                        targetCollection,
                        id
                    )
                    //println("Error processing document: ${e.message}")
                    return@forEach
                }
            }
        }

        if (source == DataSourceInDB.VLAKSI) {
            var index = 1
            val targetCollection = database.getCollection("ProcessedVlakiOdVlaksi")
            dataWithIDs?.forEach { (id, document) ->
                updateFeedback("Processing document $index of $numberOfDocuments.")
                index++

                try {
                    val urlDecodedData = getDecodedData(document.data)
                    val dataVlakSi = json.decodeFromString<VlakiSi>(urlDecodedData)
                    val convertedRequest = VlakiSiRequest(document.timeOfRequest.date, dataVlakSi)
                    val listTLH = convertedRequest.toListTrainLocHistory()
                    val listDelay = convertedRequest.toListDelay()

                    listTLH.forEach {
                        // try-catch block for each insert (most common error: empty nextStation)
                        try {
                            insertTrainLocHistory(it) // insert to database
                        } catch (_: Exception) {
                        }
                        trainLocHistoryCounter++
                    }

                    listDelay.forEach {
                        try {
                            insertDelay(it) // insert to database
                        } catch (_: Exception) {
                        }
                        delayCounter++
                    }

                    // Move processed document = mark as processed
                    moveAndDeleteDocumentInDB(
                        collection,
                        targetCollection,
                        id
                    )
                } catch (e: Exception) {
                    documentErrorCounter++

                    // Move processed document with error = mark as processed
                    moveAndDeleteDocumentInDB(
                        collection,
                        targetCollection,
                        id
                    )
                    //println("Error processing document: ${e.message}")
                    return@forEach
                }
            }
        }

        /*// Process data
        val processingUpdates = mutableListOf<String>()

        // Simulate processing updates
        for (i in 1..3) {
            val update = "Processing step $i completed."
            processingUpdates.add(update)
            // Update feedback message
            updateFeedback(update)
            delay(1000) // Simulate delay (1 second)
        }
    */
        // Call onSuccess when processing is complete
        //onSuccess("Success")

        updateFeedback("Finished processing $numberOfDocuments documents.")
        return@withContext Result.Success(
            "Success:\n" +
                    "Documents processed: $numberOfDocuments\n" +
                    "Train Location History datapoints inserted: $trainLocHistoryCounter\n" +
                    "Delay datapoints inserted: $delayCounter\n" +
                    "Documents with error: $documentErrorCounter"
        )
    }

    //return ""
}

suspend fun moveAndDeleteDocumentInDB(
    sourceCollection: MongoCollection<Document>?,
    targetCollection: MongoCollection<Document>,
    documentId: String
): String {
    return withContext(Dispatchers.IO) {
        // println("remove document: $documentId")
        try {
            val objectId = ObjectId(documentId)
            val query = Document("_id", objectId)

            // Retrieve raw document from the source collection
            val document = sourceCollection?.find(query)?.firstOrNull() ?: return@withContext "Document with id $documentId not found in source collection."

            targetCollection.insertOne(document)

            sourceCollection.deleteOne(query) // Delete the document

            return@withContext "Document with id $documentId successfully moved to target collection."
        } catch (e: Exception) {
            return@withContext "Error occurred: ${e.message}"
        }
    }
}