import androidx.compose.runtime.MutableState
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.result.Result
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import models.*
import java.net.InetAddress
import java.nio.charset.Charset
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
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

private val json = Json { ignoreUnknownKeys = true }

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
                                    val listOfficial = json.decodeFromString<List<Official>>(resultGet.value)
                                    val requestOfficial = OfficialRequest(LocalDateTime.now(), listOfficial)
                                    result["parsed"] = requestOfficial.toListTrainLocHistory()
                                    result["parsedDelay"] = requestOfficial.toListTrainLocHistory()
                                    result["parsedDelay"] = requestOfficial.toListDelay()
                                    println(result["parsed"])
                                    println(result["parsedDelay"])
                                }
                                SourceWebsite.Vlaksi -> {
                                    val listVlakSi = json.decodeFromString<VlakiSi>(resultGet.value)
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

data class ResultStations(val error: String = "", val data: String = "", val timeOfRequest: String = "", val listOfStations: List<StationInsert> = listOf())

suspend fun getStationsAndProcess(
    resultState: MutableState<ResultStations>
) {
    return coroutineScope {
        launch {
            var result = ResultStations()
            try {
                val request = Fuel.get("https://potniski.sz.si/")

                val (_, response, resultGet) = request.header("Accept-Language", "en")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0")
                    .allowRedirects(true)
                    .responseString()

                when (resultGet) {
                    is Result.Failure -> {
                        println("${getCurrentTime()} - Error occurred while getting stations: ${resultGet.error.message}")

                        if (false) {
                            println("More details: ${resultGet.error.response.body().asString("text/html")}")
                        }

                        result = result.copy(error = "Error occurred while getting from Stations: ${resultGet.error.message}")

                    }
                    is Result.Success -> {
                        println("${getCurrentTime()} - Got stations!")

                        if (!isStringJson(resultGet.value)) {
                            result = result.copy(error = "Data not in JSON format")
                        } else {
                            result = result.copy(resultGet.value)
                            result = result.copy(timeOfRequest = getCurrentTime())

                            val doc: Document = Ksoup.parse(resultGet.value)
                            val selectHtml = doc.body().getElementById("entry-station")!!.html()
                            val options = Ksoup.parseBodyFragment(selectHtml).getAllElements()

                            val stationsHtml = mutableListOf<Pair<String, String>>()
                            options.forEach {
                                if(it.tagName() == "option" && it.attribute("value") != null && it.text() != ""){
                                    stationsHtml.add(Pair(it.attribute("value")!!.value, it.text()))
                                }
                            }

                            val sloveniaResult = getStationsLocation("[out:json][timeout:25]; area(id:3600218657)->.searchArea;  (   nwr[\"railway\"=\"station\"](area.searchArea);   nwr[\"railway\"=\"halt\"](area.searchArea); ); out geom;")
                            val croatiaResult = getStationsLocation("[out:json][timeout:25]; area(id:3600214885)->.searchArea;  (   nwr[\"railway\"=\"station\"](area.searchArea);   nwr[\"railway\"=\"halt\"](area.searchArea); ); out geom;")
                            val hardCoded = mapOf(
                                "homec pri kamniku" to Coordinates(46.179913f,14.590742f),
                                "litostroj" to Coordinates(46.077072f, 14.4910755f),
                                "ljubljana dolgi most" to Coordinates(46.03758f, 14.463934f),
                                "opčine/villa opicina" to Coordinates(45.6944f, 13.791676f),
                                "oriszentpeter" to Coordinates(46.828938f, 16.415344f),
                                "pliberk/bleiburg" to Coordinates(46.574406f, 14.787246f),
                                "podrožca/rosenbach" to Coordinates(46.531097f, 14.035852f),
                                "ponikve na dolenjskem" to Coordinates(45.897717f, 15.042699f),
                                "solkan" to Coordinates(45.972694f, 13.646529f),
                                "špilje/spielfeld-strass" to Coordinates(46.709904f, 15.629217f),
                                "trzin ind. cona" to Coordinates(46.11907f, 14.551221f),
                            )

                            val stations = mutableListOf<StationInsert>()
                            stationsHtml.forEach {
                                val stationName = changeStationName(it.second)

                                if(sloveniaResult.mapOfLocations.contains(stationName)){
                                    val coordinates = sloveniaResult.mapOfLocations[stationName] ?: Coordinates(0.0f, 0.0f)
                                    stations.add(StationInsert(name= it.second, officialStationNumber = it.first, coordinates = coordinates))
                                }
                                else if (croatiaResult.mapOfLocations.contains(stationName)){
                                    val coordinates = croatiaResult.mapOfLocations[stationName] ?: Coordinates(0.0f, 0.0f)
                                    stations.add(StationInsert(name= it.second, officialStationNumber = it.first, coordinates = coordinates))
                                }
                                else if (hardCoded.contains(stationName)){
                                    val coordinates = hardCoded[stationName] ?: Coordinates(0.0f, 0.0f)
                                    stations.add(StationInsert(name= it.second, officialStationNumber = it.first, coordinates = coordinates))
                                }
                                else {
                                    println("${it.second}   ---  $stationName")
                                }
                            }

                            result = result.copy(listOfStations = stations)

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

data class ResultStationsLocation(val error: String = "", val data: String = "", val timeOfRequest: String = "", val mapOfLocations: MutableMap<String, Coordinates> = mutableMapOf())

fun changeStationName(name: String): String{
    val stationNameLowerCase = name.lowercase(Locale.getDefault())
    var stationName = stationNameLowerCase

    val afterSpaceBracket = stationName.indexOf(" (")
    if(afterSpaceBracket != -1){
        stationName = stationName.removeRange(afterSpaceBracket..<stationName.length)
    }

    val afterBracket = stationName.indexOf("(")
    if(afterBracket != -1){
        stationName = stationName.removeRange(afterBracket..<stationName.length)
    }

    return stationName
}

fun getStationsLocation(query: String): ResultStationsLocation{
    var result = ResultStationsLocation()
    try {
        val request = Fuel.post("https://overpass-api.de/api/interpreter", listOf("data" to query))

        val (_, response, resultGet) = request.header("Accept-Language", "en")
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0")
            .allowRedirects(true)
            .responseString()

        when (resultGet) {
            is Result.Failure -> {
                println("${getCurrentTime()} - Error occurred while getting stations: ${resultGet.error.message}")

                if (true) {
                    println("More details: ${resultGet.error.response.body().asString("text/html")}")
                }

                result = result.copy(error = "Error occurred while getting from Stations: ${resultGet.error.message}")
            }
            is Result.Success -> {
                println("${getCurrentTime()} - Got Locations!")

                if (!isStringJson(resultGet.value)) {
                    result = result.copy(error= "Data not in JSON format")
                } else {
                    result = result.copy(data = resultGet.value)
                    result = result.copy(timeOfRequest =  getCurrentTime())

                    val json = Json { ignoreUnknownKeys = true }
                    val getRoutes = json.decodeFromString<OpenStreetMap>(resultGet.value)

                    val mapOfLocations = mutableMapOf<String, Coordinates>()

                    getRoutes.elements.forEach {
                        if(it.type == OpenStreetMapType.NODE){
                            val node = it as NodeSection
                            if(node.tags.nameSl != null){
                                val stationName = changeStationName(node.tags.nameSl)
                                mapOfLocations[stationName] = Coordinates(node.lat, node.lon)
                            } else if(node.tags.name != null){
                                val stationName = changeStationName(node.tags.name)
                                mapOfLocations[stationName] = Coordinates(node.lat, node.lon)
                            }
                        }

                        if(it.type == OpenStreetMapType.WAY){
                            val way = it as WaySection
                            if(way.tags.nameSl != null){
                                val stationName = changeStationName(way.tags.nameSl)
                                mapOfLocations[stationName] = Coordinates(way.geometry[0].lat, way.geometry[0].lat)
                            }
                            else if (way.tags.name != null) {
                                val stationName = changeStationName(way.tags.name)
                                mapOfLocations[stationName] = Coordinates(way.geometry[0].lat, way.geometry[0].lat)
                            }


                        }
                    }

                    //println("mapOfLocations: $mapOfLocations")
                    result = result.copy(mapOfLocations = mapOfLocations)

                }
            }
        }
    } catch (e: Exception) {
        println("${getCurrentTime()} - Exception occurred while making the request: ${e.message}")
        result = result.copy(error = "Exception occurred while making the request: ${e.message}")
    }

    return result
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
