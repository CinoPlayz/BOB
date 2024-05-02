import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import com.google.gson.JsonElement
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import sun.misc.Signal
import java.net.InetAddress
import java.net.URLEncoder
import java.nio.charset.Charset
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.system.exitProcess
import kotlinx.coroutines.runBlocking
import java.net.HttpURLConnection
import java.util.IllegalFormatException

enum class SourceWebsite {
    Official,
    Vlaksi
}

fun getCurrentTime(): String {
    return DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now().atZone(ZoneId.of("Europe/Ljubljana")))
}

fun isStringJson(json: String?): Boolean {
    try {
        Gson().getAdapter(JsonElement::class.java).fromJson(json)
        return true
    } catch (ex: Exception) {
        return false
    }
}

fun isIpInBlackList(blackList: List<String>): Boolean {
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

suspend fun getDataAndProcess(source: SourceWebsite): Map<String, Any?> {
    return supervisorScope {
        val result = mutableMapOf<String, Any?>()

        Signal.handle(Signal("INT")) {
            exitProcess(0)
        }

        Signal.handle(Signal("TERM")) {
            exitProcess(0)
        }

        println("${getCurrentTime()} - Starting program!")

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
        val requestURL: String? = if (source == SourceWebsite.Official) {
            officialURL
        } else {
            vlaksiURL
        }

        println(requestURL)

        val regexURL = Regex("https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)")

        if (requestURL == null || !regexURL.matches(requestURL)) {
            println("${getCurrentTime()} - Malformed .env file! (URL is most likely malformed)")
            exitProcess(1)
        }

        val requestDetailedReport = requestDetailedReportString == "true"
        val blackList: List<String> = blackListString?.split(',') ?: emptyList()

        println("\n${getCurrentTime()} - Checking the IP against blacklist")

        if (isIpInBlackList(blackList)) {
            println("${getCurrentTime()} - Current IP is in the Blacklist!!!! Use a VPN")
            throw RuntimeException("Current IP is in the Blacklist")
        }

        println("${getCurrentTime()} - IP not in blacklist")

        println("\n${getCurrentTime()} - Making a request")

        val timeOfRequest = getCurrentTime()

        try {
            val request = if (source == SourceWebsite.Official) Fuel.post(requestURL, listOf("action" to "aktivni_vlaki")) else Fuel.get(requestURL)

            val (_, response, resultGet) = request.header("Accept-Language", "en")
                .header(
                    "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0"
                )
                .allowRedirects(true)
                .responseString()

            when (resultGet) {
                is Result.Failure -> {
                    println("${getCurrentTime()} - Error occurred while getting from $source: ${resultGet.error.message}")

                    if (requestDetailedReport) {
                        println("More details: ${resultGet.error.response.body().asString("text/html")}")
                    }

                    throw IllegalStateException("resultGet.Failure")
                }
                is Result.Success -> {
                    println("${getCurrentTime()} - Got data from $source!")
                    //println(resultGet.value)

                    if (!isStringJson(resultGet.value)) {
                        throw IllegalStateException("Data not in JSON format.")
                    }

                    result["data"] = resultGet.value
                    result["time_of_request"] = timeOfRequest
                    result["source_website"] = source.name
                }
            }
        } catch (e: Exception) {
            println("${getCurrentTime()} - Exception occurred while making the request: ${e.message}")
            result["error"] = "Exception occurred while making the request: ${e.message}"
        }
        result
    }
}

fun main() {
    runBlocking {
        try {
            val result = getDataAndProcess(SourceWebsite.Vlaksi) // or pass SourceWebsite.Vlaksi depending on your requirement
            // Process the result here
            println("Result: $result")
        } catch (e: Exception) {
            println("An error occurred: ${e.message}")
        }
    }
}
