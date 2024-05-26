/*val fileString = File("SZ.AktivniVlakiOdVlaksi66.json").readText(Charset.defaultCharset())


  val json = Json { ignoreUnknownKeys = true }
  val data = json.decodeFromString<List<DatabaseRequest>>(fileString)

  val trainLocation = mutableListOf<TrainLocHistoryInsert>()
  data.forEach {
      val urlDecodedData = getDecodedData(it.data)
      val dataVlakSi = json.decodeFromString<VlakiSi>(urlDecodedData)
      val convertedRequest = VlakiSiRequest(it.timeOfRequest.date, dataVlakSi)
      val listTrain = convertedRequest.toListTrainLocHistory()
      listTrain.forEach {
          trainLocation.add(it)
      }
  }

  File("output.json").writeText(json.encodeToString(trainLocation))

  trainLocation.forEach {
      insertTrainLocHistory(it)
  }*/

/*val fileString = File("SZ.AktivniVlakiOdVlaksi66.json").readText(Charset.defaultCharset())


val json = Json { ignoreUnknownKeys = true }
val data = json.decodeFromString<List<DatabaseRequest>>(fileString)

val delayList = mutableListOf<DelayInsert>()
data.forEach { it ->
    val urlDecodedData = getDecodedData(it.data)
    val dataVlakSi = json.decodeFromString<VlakiSi>(urlDecodedData)
    val convertedRequest = VlakiSiRequest(it.timeOfRequest.date, dataVlakSi)
    val listTrain = convertedRequest.toListDelay()
    listTrain.forEach {
        delayList.add(it)
    }
}

File("output.json").writeText(json.encodeToString(delayList))

delayList.forEach {
    insertDelay(it)
}*/