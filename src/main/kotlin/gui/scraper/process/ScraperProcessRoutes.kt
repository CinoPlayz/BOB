package gui.scraper.process

import androidx.compose.runtime.MutableState
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.result.Result
import models.RouteInsert
import models.RouteStop
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import utils.context.appContextGlobal
import java.net.URI
import java.time.LocalDateTime

data class ResultRoute(
    val error: String = "",
    val data: String = "",
    val timeOfRequest: String = "",
    val listOfRoutes: List<RouteInsert> = listOf(),
    val listOfFailedRoutes: List<Pair<String, String>> = listOf()
)

fun getRoutesAndProcess(resultState: MutableState<ResultRoute>) {
    var result = ResultRoute()
    try {
        val request = Fuel.get("https://potniski.sz.si/vozni-redi-po-relacijah-od-11-decembra-2022-do-9-decembra-2023/")

        val (_, response, resultGet) = request.header("Accept-Language", "en")
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0")
            .allowRedirects(true)
            .responseString()

        when (resultGet) {
            is Result.Failure -> {
                println("${getCurrentTime()} - Error occurred while getting routes: ${resultGet.error.message}")

                if (appContextGlobal.requestDetailReport) {
                    println("More details: ${resultGet.error.response.body().asString("text/html")}")
                }

                result = result.copy(error = "Error occurred while getting from routes: ${resultGet.error.message}")
            }

            is Result.Success -> {
                println("${getCurrentTime()} - Got Routes PDFs!")
                result = result.copy(data = resultGet.value)
                result = result.copy(timeOfRequest = getCurrentTime())

                val cookies = response.headers["Set-Cookie"].first()

                val doc: Document = Ksoup.parse(resultGet.value)
                val fileLinksElements = doc.body().getElementsByClass("filename")

                val pdfLinks = mutableListOf<String>()

                fileLinksElements.forEach {
                    if (it.tagName() == "a" && it.attribute("href") != null) {
                        pdfLinks.add(it.attribute("href")!!.value)
                    }
                }

                val routes = mutableListOf<RouteInsert>()
                val routesFailed = mutableListOf<Pair<String, String>>()

                pdfLinks.forEach { pdfLinkString ->
                    println("\nOpening: $pdfLinkString")
                    val pdfInBytes = URI.create(pdfLinkString).toURL().readBytes()
                    val document: PDDocument = Loader.loadPDF(pdfInBytes)

                    val text = PDFTextStripper().getText(document)
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
                            typesAllString.forEach { typeString ->
                                if (typeString != "Vrsta" && typeString != "vlaka" && typeString != "vlaka:") {
                                    listOfTypes.add(typeString)
                                }
                            }
                        }

                        if (line.contains("Št. vlaka")) {
                            val numbersAllString = line.split(" ")
                            numbersAllString.forEach { numberString ->
                                if (numberString != "Št." && numberString != "vlaka" && numberString != "vlaka:") {
                                    listOfNumbers.add(numberString)
                                }
                            }
                        }

                        if (line.contains("Velja od 10. 12. 2023 do 14. 12. 2024")) {
                            validFrom = LocalDateTime.of(2023, 12, 10, 0, 0, 0)
                            vaildUntil = LocalDateTime.of(2024, 12, 14, 0, 0, 0)
                        }
                    }

                    if (listOfTypes.count() == listOfNumbers.count()) {
                        listOfTypes.forEachIndexed { index, type ->
                            listOfTrainsOnRoute.add(Pair(type, listOfNumbers[index]))
                        }
                    } else {
                        println("Count of types and numbers doesn't match!!")
                        println(listOfNumbers)
                        println(listOfTypes)
                    }

                    listOfTrainsOnRoute.forEach {
                        val resultRouteDetails = getRouteDetails(
                            cookies = cookies,
                            trainType = it.first,
                            trainNumber = it.second,
                            validFrom = validFrom,
                            validUntil = vaildUntil
                        )

                        if (resultRouteDetails.error != "") {
                            println("Cannot insert because of error (${it.first} ${it.second})")
                            routesFailed.add(it)
                        } else {
                            if (resultRouteDetails.routeInsert != null) {
                                routes.add(resultRouteDetails.routeInsert)
                            }
                        }
                    }

                }

                val routesDistinct = routes.distinctBy { it.trainNumber }
                //println(routesDistinct)

                result = result.copy(listOfRoutes = routesDistinct)
                result = result.copy(listOfFailedRoutes = routesFailed)

            }
        }
    } catch (e: Exception) {
        println("${getCurrentTime()} - Exception occurred while making the request: ${e.message}")
        result = result.copy(error = "Exception occurred while making the request: ${e.message}")
    }

    resultState.value = result
}

data class ResultRouteDetails(
    val error: String = "",
    val data: String = "",
    val timeOfRequest: String = "",
    val routeInsert: RouteInsert? = null
)


fun getRouteDetails(
    cookies: String,
    trainType: String,
    trainNumber: String,
    validFrom: LocalDateTime,
    validUntil: LocalDateTime
): ResultRouteDetails {
    var result = ResultRouteDetails()
    try {
        val requestTrainDetails = Fuel.post(
            "https://potniski.sz.si/wp-admin/admin-ajax.php",
            listOf("action" to "train_details", "data[train]" to trainNumber)
        ).header(Headers.COOKIE to cookies)

        val (_, responseTrainDetails, resultTrainDetails) = requestTrainDetails.header("Accept-Language", "en")
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:124.0) Gecko/20100101 Firefox/124.0")
            .allowRedirects(true)
            .responseString()

        when (resultTrainDetails) {
            is Result.Failure -> {
                println("${getCurrentTime()} - Error occurred while getting route details: ${resultTrainDetails.error.message}")

                if (appContextGlobal.requestDetailReport) {
                    println("More details: ${resultTrainDetails.error.response.body().asString("text/html")}")
                }

                result =
                    result.copy(error = "Error occurred while getting from route details: ${resultTrainDetails.error.message}")
            }

            is Result.Success -> {
                println("${getCurrentTime()} - Got Route Details ($trainType $trainNumber)!")
                result = result.copy(data = resultTrainDetails.value)
                result = result.copy(timeOfRequest = getCurrentTime())

                val returnedHtml = json.decodeFromString<String>(resultTrainDetails.value)
                var htmlStringParsed = returnedHtml.replace("\n", "")
                htmlStringParsed = htmlStringParsed.replace("\\", "")

                val tableBody = Ksoup.parseBodyFragment(htmlStringParsed).getElementsByTag("tbody")

                val tableRows = tableBody[0].getElementsByTag("tr")

                val routeStops = mutableListOf<RouteStop>()
                tableRows.forEach { it ->
                    val tableData = it.getElementsByTag("td")
                    val station = tableData.first()?.text() ?: ""
                    var time = tableData[2].text()
                    if (time == "") {
                        time = tableData[1].text()
                    }
                    routeStops.add(RouteStop(station, time))
                }

                val drivesOnString = Ksoup.parseBodyFragment(htmlStringParsed).getElementsByClass("mb-1").first()
                    ?.getElementsByTag("span")?.first()?.text() ?: ""

                var drivesOn = listOf(0, 1, 2, 3, 4, 5, 6, 7)

                if (drivesOnString.contains("Ne vozi ob sobotah, nedeljah in praznikih-dela prostih dneh v RS")) {
                    drivesOn = listOf(1, 2, 3, 4, 5)
                } else if (drivesOnString.contains("Vozi ob torkih, petkih in nedeljah")) {
                    drivesOn = listOf(2, 5, 0)
                } else if (drivesOnString.contains("Ne vozi ob nedeljah in praznikih-dela prostih dneh v RS")) {
                    drivesOn = listOf(1, 2, 3, 4, 5, 6)
                } else if (drivesOnString.contains("Ne vozi ob sobotah")) {
                    drivesOn = listOf(0, 1, 2, 3, 4, 5, 7)
                }

                //Only happens once (special edge case)
                if (routeStops.isEmpty()) {
                    routeStops.add(RouteStop("Zidani Most", "11:56"))
                    routeStops.add(RouteStop("Ljubljana", "12:43"))
                }

                val routeStopsMiddle = mutableListOf<RouteStop>()
                routeStops.forEachIndexed { index, routeStop ->
                    if (index != 0 && index != (routeStops.size - 1)) {
                        routeStopsMiddle.add(routeStop)
                    }
                }

                val routeInsert = RouteInsert(
                    trainType = trainType,
                    trainNumber = trainNumber.toInt(),
                    validFrom = validFrom,
                    validUntil = validUntil,
                    canSupportBikes = !cannotSupportBikes.contains(trainNumber),
                    drivesOn = drivesOn,
                    start = routeStops.first(),
                    middle = routeStopsMiddle,
                    end = routeStops.last()
                )
                result = result.copy(routeInsert = routeInsert)

            }
        }
    } catch (e: Exception) {
        println("${getCurrentTime()} - Exception occurred while making the request: ${e.message}")
        result = result.copy(error = "Exception occurred while making the request: ${e.message}")
    }

    return result
}

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
    "4517",
    "603",
    "3217",
    "4502",
    "602",
    "603",
    "3217",
    "26725",
    "26025",
    "2602",
    "26365",
    "26765",
    "26505",
    "2705",
    "2717",
    "2707",
    "2752",
    "2680",
    "26825",
    "26015",
    "2621",
    "2653",
    "2633",
    "2702",
    "2671",
    "26195",
    "27515",
    "2706",
    "26755",
    "26635",
    "2718",
    "2615",
    "26775",
    "2679",
    "1618",
    "26795",
    "3164",
    "3120",
    "3182",
    "3165",
    "3141",
    "3179",
    "1247",
    "1246",
    "1152",
    "2900",
    "4432",
    "2523",
    "2992",
    "2800",
    "643",
    "311",
    "512",
    "30",
    "2982",
    "2525",
    "3709",
    "2802",
    "2902",
    "4434",
    "14",
    "158",
    "2004",
    "4436",
    "2519",
    "2914",
    "36",
    "4456",
    "518",
    "2008",
    "1640",
    "2505",
    "20",
    "2920",
    "2922",
    "2924",
    "2926",
    "526",
    "2002",
    "607",
    "2801",
    "2901",
    "2993",
    "11",
    "3616",
    "3712",
    "2903",
    "2981",
    "2500",
    "4435",
    "2803",
    "31",
    "35",
    "517",
    "2807",
    "2518",
    "2991",
    "2520",
    "1641",
    "1615",
    "2937",
    "1643",
    "159",
    "523",
    "2927",
    "4455",
    "2931",
    "1153",
    "310",
    "2500",
    "2504",
    "2526",
    "2518",
    "2520",
    "1640",
    "1246",
    "2523",
    "643",
    "2525",
    "2519",
    "2505",
    "1641",
    "1643",
    "9557",
    "2516",
    "2506",
    "2504",
    "1640",
    "9561",
    "2510",
    "1246",
    "643",
    "9550",
    "2517",
    "2519",
    "2507",
    "2505",
    "2515",
    "1641",
    "1643",
    "9568",
    "3701",
    "3712",
    "3512",
    "44201",
    "44202",
    "44203",
    "2750",
    "7707",
    "27521",
    "27522",
    "27513",
    "27913",
    "44211",
    "44212",
    "44213",
    "7706",
    "27531",
    "27532",
    "27533",
    "4517",
    "4502"
)