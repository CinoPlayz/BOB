import androidx.compose.runtime.MutableState
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.result.Result
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import models.Official
import models.OfficialRequest
import models.VlakiSi
import models.VlakiSiRequest
import java.net.InetAddress
import java.nio.charset.Charset
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.system.exitProcess

enum class SourceWebsite {
    Official,
    Vlaksi
}

fun getCurrentTime(): String {
    return DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now().atZone(ZoneId.of("Europe/Ljubljana")))
}

fun isStringJson(json: String?): Boolean {
    try {
        //Gson().getAdapter(JsonElement::class.java).fromJson(json)
        //TODO: Fix this
        return true
    } catch (ex: Exception) {
        return false
    }
}

suspend fun isIpInBlackList(blackList: List<String>): Boolean {
    val blackListOnlyIp = mutableListOf<String>()
    val regexDomain = Regex("[A-Za-z]")

    blackList.forEach {
        if (regexDomain.containsMatchIn(it)) {
            val ipOfDomain = InetAddress.getByName(it)
            println(ipOfDomain)
            blackListOnlyIp.add(ipOfDomain.hostAddress)
        } else {
            blackListOnlyIp.add(it)
        }
    }

    var gotMatch = false

    val res = Fuel.get("https://ipinfo.io/ip")
        .header("Accept-Language", "en")
        .header(
            "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0"
        )
        .responseString(Charset.defaultCharset())

    //println(res.second.headers)

    when (res.third) {
        is Result.Failure -> {
            gotMatch = true
            println("${getCurrentTime()} - Error occurred while getting IP: ${(res.third as Result.Failure<FuelError>).error.message}")
        }

        is Result.Success -> {
            blackListOnlyIp.forEach {
                if (it.contains(res.third.get())) {
                    gotMatch = true
                }
            }
        }
    }

    return gotMatch
}

suspend fun readConfiguration(source: SourceWebsite): Triple<String?, Boolean, List<String>> {
    println("${getCurrentTime()} - Reading from config/.env!")
    val configUrls = dotenv {
        directory = "config/"
        filename = "URLs.env"
    }
    val configBlackList = dotenv {
        directory = "config/"
        filename = "blacklist.env"
    }

    val officialURL = configUrls["OFFICIAL_URL"]
    val vlaksiURL = configUrls["VLAKSI_URL"]
    val requestDetailedReportString = configUrls["REQUEST_DETAIL_REPORT"]
    val blackListString = configBlackList["BLACKLIST"]
    val requestURL = if (source == SourceWebsite.Official) officialURL else vlaksiURL
    val requestDetailedReport = requestDetailedReportString == "true"
    val blackList = blackListString?.split(',') ?: emptyList()

    val regexURL = Regex("https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)")
    if (requestURL == null || !regexURL.matches(requestURL)) {
        println("${getCurrentTime()} - Malformed .env file! (URL is most likely malformed)")
        exitProcess(1)
    }
    return Triple(requestURL, requestDetailedReport, blackList)
}

suspend fun getDataAndProcess(
    source: SourceWebsite,
    resultState: MutableState<Map<String, Any?>?>
) {
    return coroutineScope {
        launch {
            val result = mutableMapOf<String, Any?>()

            println("${getCurrentTime()} - Starting program!")

            // Read configuration from files
            val (requestURL, requestDetailedReport, blackList) = readConfiguration(source)

            println("\n${getCurrentTime()} - Checking the IP against blacklist")

            if (isIpInBlackList(blackList)) {
                println("${getCurrentTime()} - Current IP is in the Blacklist!!!! Use a VPN")
                result["error"] = "Current IP is in the Blacklist"
                resultState.value = result
                return@launch
            }

            println("${getCurrentTime()} - IP not in blacklist")

            println("\n${getCurrentTime()} - Making a request")

            try {
                val request = if (source == SourceWebsite.Official) Fuel.post(requestURL!!, listOf("action" to "aktivni_vlaki")) else Fuel.get(requestURL!!)

                val (_, response, resultGet) = request.header("Accept-Language", "en")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0")
                    .allowRedirects(true)
                    .responseString()

                when (resultGet) {
                    is Result.Failure -> {
                        println("${getCurrentTime()} - Error occurred while getting from $source: ${resultGet.error.message}")

                        if (requestDetailedReport) {
                            println("More details: ${resultGet.error.response.body().asString("text/html")}")
                        }

                        result["error"] = "Error occurred while getting from $source: ${resultGet.error.message}"
                    }
                    is Result.Success -> {
                        println("${getCurrentTime()} - Got data from $source!")

                        if (!isStringJson(resultGet.value)) {
                            result["error"] = "Data not in JSON format"
                        } else {
                            result["data"] = resultGet.value
                            result["time_of_request"] = getCurrentTime()
                            result["source_website"] = source.name

                            when(source){
                                SourceWebsite.Official -> {
                                    val listOfficial = Json.decodeFromString<List<Official>>(resultGet.value)
                                    val requestOfficial = OfficialRequest(LocalDateTime.now(), listOfficial)
                                    result["parsed"] = requestOfficial.toListTrainLocHistory()
                                    result["parsedDelay"] = requestOfficial.toListTrainLocHistory()
                                    result["parsedDelay"] = requestOfficial.toListDelay()
                                    println(result["parsed"])
                                    println(result["parsedDelay"])
                                }
                                SourceWebsite.Vlaksi -> {
                                    val listVlakSi = Json.decodeFromString<VlakiSi>(resultGet.value)
                                    val requestVlakSi = VlakiSiRequest(LocalDateTime.now(), listVlakSi)
                                    result["parsed"] = requestVlakSi.toListTrainLocHistory()
                                    result["parsedDelay"] = requestVlakSi.toListDelay()
                                    println(result["parsed"])
                                    println(result["parsedDelay"])
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                println("${getCurrentTime()} - Exception occurred while making the request: ${e.message}")
                result["error"] = "Exception occurred while making the request: ${e.message}"
            }
            resultState.value = result
        }
    }
}

/*fun main() {
    runBlocking {
        try {
            // val result = getDataAndProcess(SourceWebsite.Vlaksi) // or pass SourceWebsite.Vlaksi depending on your requirement
            // Process the result here
            // println("Result: $result")
        } catch (e: Exception) {
            println("An error occurred: ${e.message}")
        }
    }
}*/
