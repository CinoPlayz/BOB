package gui.scraper.process

import androidx.compose.runtime.MutableState
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import models.*
import utils.context.appContextGlobal
import java.time.LocalDateTime

val json = Json { ignoreUnknownKeys = true }

data class ResultData(
    val error: String = "",
    val data: String = "",
    val timeOfRequest: String = "",
    val source: String = "",
    val listOfTrainLocHistory: List<TrainLocHistoryInsert> = listOf(),
    val listOfDelay: List<DelayInsert> = listOf()
)

suspend fun getDataAndProcess(
    source: SourceWebsite,
    resultState: MutableState<ResultData>
) {
    return coroutineScope {
        launch {
            var result = ResultData()

            val requestURL: String = if (source == SourceWebsite.Official) appContextGlobal.officialUrl else appContextGlobal.vlakSiUrl
            val requestDetailedReport = appContextGlobal.requestDetailReport
            val blackList = appContextGlobal.blacklist

            println("\n${getCurrentTime()} - Checking the IP against blacklist")

            if (isIpInBlackList(blackList)) {
                println("${getCurrentTime()} - Current IP is in the Blacklist!!!! Use a VPN")
                result = result.copy(error = "Current IP is in the Blacklist")
                resultState.value = result
                return@launch
            }

            println("${getCurrentTime()} - IP not in blacklist")

            println("\n${getCurrentTime()} - Making a request")

            try {
                val request = if (source == SourceWebsite.Official) Fuel.post(
                    requestURL,
                    listOf("action" to "aktivni_vlaki")
                ) else Fuel.get(requestURL)

                val (_, response, resultGet) = request.header("Accept-Language", "en")
                    .header(
                        "User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0"
                    )
                    .allowRedirects(true)
                    .responseString()

                when (resultGet) {
                    is Result.Failure -> {
                        println("${getCurrentTime()} - Error occurred while getting from $source: ${resultGet.error.message}")

                        if (requestDetailedReport) {
                            println("More details: ${resultGet.error.response.body().asString("text/html")}")
                        }

                        result = result.copy(error = "Error occurred while getting from $source: ${resultGet.error.message}")
                    }

                    is Result.Success -> {
                        println("${getCurrentTime()} - Got data from $source!")

                        if (!isStringJson(resultGet.value)) {
                            result = result.copy(error = "Data not in JSON format")
                        } else {
                            result = result.copy(data = resultGet.value)
                            result = result.copy(timeOfRequest = getCurrentTime())
                            result = result.copy(source = source.name)

                            when (source) {
                                SourceWebsite.Official -> {
                                    val listOfficial = json.decodeFromString<List<Official>>(resultGet.value)
                                    val requestOfficial = OfficialRequest(LocalDateTime.now(), listOfficial)

                                    result = result.copy(listOfTrainLocHistory = requestOfficial.toListTrainLocHistory())
                                    result = result.copy(listOfDelay = requestOfficial.toListDelay())
                                }

                                SourceWebsite.Vlaksi -> {
                                    val listVlakSi = json.decodeFromString<VlakiSi>(resultGet.value)
                                    val requestVlakSi = VlakiSiRequest(LocalDateTime.now(), listVlakSi)
                                    result = result.copy(listOfTrainLocHistory = requestVlakSi.toListTrainLocHistory())
                                    result = result.copy(listOfDelay = requestVlakSi.toListDelay())
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                println("${getCurrentTime()} - Exception occurred while making the request: ${e.message}")
                result = result.copy(error = "Exception occurred while making the request: ${e.message}")
            }
            resultState.value = result
        }
    }
}