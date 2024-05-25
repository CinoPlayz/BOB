import androidx.compose.runtime.MutableState
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.result.Result
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import models.*
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import utils.context.appContextGlobal
import java.net.InetAddress
import java.net.URI
import java.nio.charset.Charset
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

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

private val json = Json { ignoreUnknownKeys = true }

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

            val requestURL: String =
                if (source == SourceWebsite.Official) appContextGlobal.officialUrl else appContextGlobal.vlakSiUrl
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

                        result =
                            result.copy(error = "Error occurred while getting from $source: ${resultGet.error.message}")

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

                                    result =
                                        result.copy(listOfTrainLocHistory = requestOfficial.toListTrainLocHistory())
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

data class ResultStations(
    val error: String = "",
    val data: String = "",
    val timeOfRequest: String = "",
    val listOfStations: List<StationInsert> = listOf()
)

suspend fun getStationsAndProcess(
    resultState: MutableState<ResultStations>
) {
    return coroutineScope {
        launch {
            var result = ResultStations()
            try {
                val request = Fuel.get("https://potniski.sz.si/")

                val (_, response, resultGet) = request.header("Accept-Language", "en")
                    .header(
                        "User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0"
                    )
                    .allowRedirects(true)
                    .responseString()

                when (resultGet) {
                    is Result.Failure -> {
                        println("${getCurrentTime()} - Error occurred while getting stations: ${resultGet.error.message}")

                        if (appContextGlobal.requestDetailReport) {
                            println("More details: ${resultGet.error.response.body().asString("text/html")}")
                        }

                        result =
                            result.copy(error = "Error occurred while getting from Stations: ${resultGet.error.message}")

                    }

                    is Result.Success -> {
                        println("${getCurrentTime()} - Got stations!")

                        if (!isStringJson(resultGet.value)) {
                            result = result.copy(error = "Data not in JSON format")
                        } else {
                            result = result.copy(data = resultGet.value)
                            result = result.copy(timeOfRequest = getCurrentTime())

                            val doc: Document = Ksoup.parse(resultGet.value)
                            val selectHtml = doc.body().getElementById("entry-station")!!.html()
                            val options = Ksoup.parseBodyFragment(selectHtml).getAllElements()

                            val stationsHtml = mutableListOf<Pair<String, String>>()
                            options.forEach {
                                if (it.tagName() == "option" && it.attribute("value") != null && it.text() != "") {
                                    stationsHtml.add(Pair(it.attribute("value")!!.value, it.text()))
                                }
                            }

                            val sloveniaResult =
                                getStationsLocation("[out:json][timeout:25]; area(id:3600218657)->.searchArea;  (   nwr[\"railway\"=\"station\"](area.searchArea);   nwr[\"railway\"=\"halt\"](area.searchArea); ); out geom;")
                            val croatiaResult =
                                getStationsLocation("[out:json][timeout:25]; area(id:3600214885)->.searchArea;  (   nwr[\"railway\"=\"station\"](area.searchArea);   nwr[\"railway\"=\"halt\"](area.searchArea); ); out geom;")
                            val hardCoded = mapOf(
                                "homec pri kamniku" to Coordinates(46.179913f, 14.590742f),
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

                                if (sloveniaResult.mapOfLocations.contains(stationName)) {
                                    val coordinates =
                                        sloveniaResult.mapOfLocations[stationName] ?: Coordinates(0.0f, 0.0f)
                                    stations.add(
                                        StationInsert(
                                            name = it.second,
                                            officialStationNumber = it.first,
                                            coordinates = coordinates
                                        )
                                    )
                                } else if (croatiaResult.mapOfLocations.contains(stationName)) {
                                    val coordinates =
                                        croatiaResult.mapOfLocations[stationName] ?: Coordinates(0.0f, 0.0f)
                                    stations.add(
                                        StationInsert(
                                            name = it.second,
                                            officialStationNumber = it.first,
                                            coordinates = coordinates
                                        )
                                    )
                                } else if (hardCoded.contains(stationName)) {
                                    val coordinates = hardCoded[stationName] ?: Coordinates(0.0f, 0.0f)
                                    stations.add(
                                        StationInsert(
                                            name = it.second,
                                            officialStationNumber = it.first,
                                            coordinates = coordinates
                                        )
                                    )
                                } else {
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

data class ResultStationsLocation(
    val error: String = "",
    val data: String = "",
    val timeOfRequest: String = "",
    val mapOfLocations: MutableMap<String, Coordinates> = mutableMapOf()
)

fun changeStationName(name: String): String {
    val stationNameLowerCase = name.lowercase(Locale.getDefault())
    var stationName = stationNameLowerCase

    val afterSpaceBracket = stationName.indexOf(" (")
    if (afterSpaceBracket != -1) {
        stationName = stationName.removeRange(afterSpaceBracket..<stationName.length)
    }

    val afterBracket = stationName.indexOf("(")
    if (afterBracket != -1) {
        stationName = stationName.removeRange(afterBracket..<stationName.length)
    }

    return stationName
}

fun getStationsLocation(query: String): ResultStationsLocation {
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

                if (appContextGlobal.requestDetailReport) {
                    println("More details: ${resultGet.error.response.body().asString("text/html")}")
                }

                result = result.copy(error = "Error occurred while getting from Stations: ${resultGet.error.message}")
            }

            is Result.Success -> {
                println("${getCurrentTime()} - Got Locations!")

                if (!isStringJson(resultGet.value)) {
                    result = result.copy(error = "Data not in JSON format")
                } else {
                    result = result.copy(data = resultGet.value)
                    result = result.copy(timeOfRequest = getCurrentTime())

                    val json = Json { ignoreUnknownKeys = true }
                    val getStationsLocation = json.decodeFromString<OpenStreetMap>(resultGet.value)

                    val mapOfLocations = mutableMapOf<String, Coordinates>()

                    getStationsLocation.elements.forEach {
                        if (it.type == OpenStreetMapType.NODE) {
                            val node = it as NodeSection
                            if (node.tags.nameSl != null) {
                                val stationName = changeStationName(node.tags.nameSl)
                                mapOfLocations[stationName] = Coordinates(node.lat, node.lon)
                            } else if (node.tags.name != null) {
                                val stationName = changeStationName(node.tags.name)
                                mapOfLocations[stationName] = Coordinates(node.lat, node.lon)
                            }
                        }

                        if (it.type == OpenStreetMapType.WAY) {
                            val way = it as WaySection
                            if (way.tags.nameSl != null) {
                                val stationName = changeStationName(way.tags.nameSl)
                                mapOfLocations[stationName] = Coordinates(way.geometry[0].lat, way.geometry[0].lat)
                            } else if (way.tags.name != null) {
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

data class ResultRoute(
    val error: String = "",
    val data: String = "",
    val timeOfRequest: String = "",
    val listOfRoutes: List<RouteInsert> = listOf()
)

fun getRoutesAndProcess(): ResultRoute {

    /*val fileString = File("SZ.AktivniVlakiOdVlaksi66.json").readText(Charset.defaultCharset())


    val json = Json { ignoreUnknownKeys = true }
    val data = json.decodeFromString<List<VlakiSiRequestDefault>>(fileString)

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

    var result = ResultRoute()
    try {
        val request = Fuel.get("https://potniski.sz.si/vozni-redi-po-relacijah-od-11-decembra-2022-do-9-decembra-2023/")

        val (_, response, resultGet) = request.header("Accept-Language", "en")
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0")
            .allowRedirects(true)
            .responseString()

        when (resultGet) {
            is Result.Failure -> {
                println("${getCurrentTime()} - Error occurred while getting stations: ${resultGet.error.message}")

                if (appContextGlobal.requestDetailReport) {
                    println("More details: ${resultGet.error.response.body().asString("text/html")}")
                }

                result = result.copy(error = "Error occurred while getting from Stations: ${resultGet.error.message}")
            }

            is Result.Success -> {
                println("${getCurrentTime()} - Got Locations!")
                result = result.copy(data = resultGet.value)
                result = result.copy(timeOfRequest = getCurrentTime())

                val json = Json { ignoreUnknownKeys = true }
                val doc: Document = Ksoup.parse(resultGet.value)
                val fileLinksElements = doc.body().getElementsByClass("filename")

                val pdfLinks = mutableListOf<String>()

                fileLinksElements.forEach {
                    if (it.tagName() == "a" && it.attribute("href") != null) {
                        pdfLinks.add(it.attribute("href")!!.value)
                    }
                }


                println("Opening: ${pdfLinks[0]}")
                val pdfInBytes = URI.create(pdfLinks[0]).toURL().readBytes()
                val document: PDDocument = Loader.loadPDF(pdfInBytes)

                val text = PDFTextStripper().getText(document)
                println(text)
                document.close()

                val listOfTrainsOnRoute = mutableListOf<Pair<String, String>>()
                val listOfTypes = mutableListOf<String>()
                val listOfNumbers = mutableListOf<String>()
                var validFrom = LocalDateTime.now()
                var vaildUntil = LocalDateTime.now()
                val iteratorByLine = text.lineSequence().iterator()
                while (iteratorByLine.hasNext()) {
                    val line = iteratorByLine.next()

                    if (line.contains("Vrsta vlaka")) {
                        val typesAllString = line.split(" ")
                        typesAllString.forEach {
                            if (it != "Vrsta" && it != "vlaka" && it != "vlaka:") {
                                listOfTypes.add(it)
                            }
                        }
                    }

                    if (line.contains("Št. vlaka")) {
                        val typesAllString = line.split(" ")
                        typesAllString.forEach {
                            if (it != "Št." && it != "vlaka" && it != "vlaka:") {
                                listOfNumbers.add(it)
                            }
                        }
                    }

                    if (line.contains("Velja od 10. 12. 2023 do 14. 12. 2024")) {
                        validFrom = LocalDateTime.of(2023, 12, 10, 0, 0, 0)
                        vaildUntil = LocalDateTime.of(2024, 12, 14, 0, 0, 0)
                    }
                }

                if (listOfTypes.count() == listOfNumbers.count()) {
                    listOfTypes.forEachIndexed { index, s ->
                        listOfTrainsOnRoute.add(Pair(s, listOfNumbers[index]))
                    }
                } else {
                    println("Count of types and numbers doesn't match!!")
                    println(listOfNumbers)
                    println(listOfTypes)
                }

                println(listOfTrainsOnRoute)

                val routes = mutableListOf<RouteInsert>()


                /*pdfLinks.forEach {
                    println("Opening: $it")
                    val pdfInBytes = URI.create(it).toURL().readBytes()
                    val document: PDDocument = Loader.loadPDF(pdfInBytes)

                    val textStripper = PDFTextStripper().getText(document)

                    println(textStripper)

                    document.close()
                }*/


                //println("mapOfLocations: $mapOfLocations")
                //result = result.copy(mapOfLocations = mapOfLocations)


            }
        }
    } catch (e: Exception) {
        println("${getCurrentTime()} - Exception occurred while making the request: ${e.message}")
        result = result.copy(error = "Exception occurred while making the request: ${e.message}")
    }

    return result
}

val supportsBikes = listOf(
    "2006",
    "2251",
    "2201",
    "2253",
    "2441",
    "606",
    "2255",
    "2205",
    "1606",
    "1608",
    "2207",
    "2259",
    "247",
    "2261",
    "156",
    "2265",
    "2010",
    "2219",
    "629",
    "2221",
    "211",
    "2273",
    "150",
    "2227",
    "2275",
    "2229",
    "635",
    "502",
    "2277",
    "2233",
    "2281",
    "215",
    "2237",
    "2283",
    "1604",
    "604",
    "2007",
    "2200",
    "1605",
    "605",
    "2001",
    "214",
    "2210",
    "503",
    "2214",
    "2256",
    "2260",
    "2262",
    "151",
    "2224",
    "210",
    "246",
    "155",
    "2270",
    "2228",
    "2272",
    "617",
    "157",
    "2274",
    "2232",
    "1607",
    "2276",
    "2278",
    "2236",
    "2280",
    "2301",
    "2251",
    "2391",
    "2253",
    "2255",
    "2393",
    "2803",
    "2259",
    ""
)

val cannotSupportBikes = listOf(
    "311",
    "512",
    "14",
    "415",
    "2004",
    "36",
    "518",
    "2008",
    "1640",
    "2271",
    "20",
    "2223",
    "631",
    "2225",
    "213",
    "2279",
    "526",
    "2002",
    "310",
    "2202",
    "2204",
    "630",
    "2206",
    "607",
    "632",
    "2208",
    "2250",
    "11",
    "2252",
    "212",
    "31",
    "35",
    "2258",
    "517",
    "2264",
    "2268",
    "2226",
    "1611",
    "1641",
    "1615",
    "1643",
    "414",
    "523",
    "1613",
    "2801",
    "415",
    "2807",
    "2809",
    "2271",
    "631",
    "213",
    "2279",
    "159",
    "1153",
    "1152",
    "2202",
    "630",
    "632",
    "2250",
    "2802",
    "2252",
    "212",
    "158",
    "2258",
    "2264",
    "2266",
    "2268",
    "414",
    "2442",
    "212",
    "2410",
    "2412",
    "2414",
    "2416",
    "2418",
    "2446",
    "414",
    "2403",
    "2443",
    "2405",
    "2407",
    "415",
    "2413",
    "213",
    "2425",
    )

data class ResultRouteDetails(
    val error: String = "",
    val data: String = "",
    val timeOfRequest: String = "",
    val routeInsert: RouteInsert? = null
)


fun getRouteDetails(
    trainType: String,
    trainNumber: String,
    validFrom: LocalDateTime,
    validUntil: LocalDateTime
): ResultRouteDetails {
    var result = ResultRouteDetails()
    try {
        val requestTrainDetails = Fuel.post(
            "https://overpass-api.de/api/interpreter",
            listOf("action" to "train_details", "data[train]" to trainNumber)
        )

        val (_, responseTrainDetails, resultTrainDetails) = requestTrainDetails.header("Accept-Language", "en")
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0")
            .allowRedirects(true)
            .responseString()

        when (resultTrainDetails) {
            is Result.Failure -> {
                println("${getCurrentTime()} - Error occurred while getting stations: ${resultTrainDetails.error.message}")

                if (appContextGlobal.requestDetailReport) {
                    println("More details: ${resultTrainDetails.error.response.body().asString("text/html")}")
                }

                result =
                    result.copy(error = "Error occurred while getting from Stations: ${resultTrainDetails.error.message}")
            }

            is Result.Success -> {
                println("${getCurrentTime()} - Got Locations!")
                result = result.copy(data = resultTrainDetails.value)
                result = result.copy(timeOfRequest = getCurrentTime())

                val returnedHtml = json.decodeFromString<String>(resultTrainDetails.value)
                var htmlStringParsed = returnedHtml.replace("\n", "")
                htmlStringParsed = htmlStringParsed.replace("\\", "")

                val tableBody = Ksoup.parseBodyFragment(htmlStringParsed).getElementsByTag("tbody")

                val tableRows = tableBody[0].getElementsByTag("tr")

                tableRows.forEach {
                    val tableData = it.getElementsByTag("td")


                }

                //val routeInsert = RouteInsert(trainType = trainType, trainNumber = trainNumber, validFrom = validFrom, validUntil = validUntil)
                //result = result.copy(routeInsert = routeInsert)

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
