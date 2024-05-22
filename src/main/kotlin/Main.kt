import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.material.CircularProgressIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

const val NAME = "BandOfBytes"

enum class MenuState(val customName: String) {
    ADD_TRAIN("//Add train//"),
    ALL_TRAINS("//Trains//"),
    SCRAPER("Scraper"),
    PROCESSOR("//Data Processor//"),
    GENERATOR("//Generator//"),
    ABOUT("About")
}

enum class ScraperMenuState(val customName: String) {
    OFFICIAL("SZ Official"),
    VLAKSI("Vlak.si"),
    RESET("Reset")
}

// Testing feature/test

@Composable
@Preview
fun App() {
    val menuState = remember { mutableStateOf(MenuState.ABOUT) }

    MaterialTheme {
        Row(modifier = Modifier.fillMaxSize()) {
            Menu(menuState, modifier = Modifier.weight(1f))
            Content(menuState, modifier = Modifier.weight(2f))
        }
    }
}

@Composable
fun Menu(
    menuState: MutableState<MenuState>,
    modifier: Modifier = Modifier,
    buttonPadding: Int = 10,
    textOffset: Int = 25,
    iconSize: Int = 20,
    fontSize: Int = 16, //
    iconTextSpace: Int = 6
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(215.dp)
            .padding(10.dp)
            .clip(RoundedCornerShape(8.dp)) // Rounded corners for box (Corners of Row are contained within)
            .border(
                BorderStroke(1.dp, Color.LightGray),
                shape = RoundedCornerShape(8.dp) // Rounded corners with radius 8dp
            )
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(1.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    //.clickable { menuState.value = MenuState.ADD_TRAIN }
                    .background(color = Color.Transparent)
                    .padding(vertical = buttonPadding.dp)
                    //.wrapContentWidth(Alignment.CenterHorizontally)
                    .offset(x = textOffset.dp)

            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = MenuState.ADD_TRAIN.name,
                    modifier = Modifier
                        .size(size = iconSize.dp)
                        .align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.width(iconTextSpace.dp)) // spacing between icon and text
                Text(
                    text = MenuState.ADD_TRAIN.customName,
                    textAlign = TextAlign.Center,
                    fontSize = fontSize.sp,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
            Divider(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    //.clickable { menuState.value = MenuState.ALL_TRAINS }
                    .background(color = Color.Transparent)
                    .padding(vertical = buttonPadding.dp)
                    .offset(x = textOffset.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.List,
                    contentDescription = MenuState.ALL_TRAINS.name,
                    modifier = Modifier
                        .size(size = iconSize.dp)
                        .align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.width(iconTextSpace.dp))
                Text(
                    text = MenuState.ALL_TRAINS.customName,
                    textAlign = TextAlign.Center,
                    fontSize = fontSize.sp,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    //.clickable { menuState.value = MenuState.PROCESSOR }
                    .background(color = Color.Transparent)
                    .padding(vertical = buttonPadding.dp)
                    .offset(x = textOffset.dp)
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = MenuState.PROCESSOR.name,
                    modifier = Modifier
                        .size(size = iconSize.dp)
                        .align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.width(iconTextSpace.dp))
                Text(
                    text = MenuState.PROCESSOR.customName,
                    textAlign = TextAlign.Center,
                    fontSize = fontSize.sp,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
            Divider(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { menuState.value = MenuState.SCRAPER }
                    .background(color = Color.Transparent)
                    .padding(vertical = buttonPadding.dp)
                    .offset(x = textOffset.dp)
            ) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = MenuState.SCRAPER.name,
                    modifier = Modifier
                        .size(size = iconSize.dp)
                        .align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.width(iconTextSpace.dp))
                Text(
                    text = MenuState.SCRAPER.customName,
                    textAlign = TextAlign.Center,
                    fontSize = fontSize.sp,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
            Divider(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    //.clickable { menuState.value = MenuState.GENERATOR }
                    .background(color = Color.Transparent)
                    .padding(vertical = buttonPadding.dp)
                    .offset(x = textOffset.dp)
            ) {
                Icon(
                    Icons.Default.Autorenew,
                    contentDescription = MenuState.GENERATOR.name,
                    modifier = Modifier
                        .size(size = iconSize.dp)
                        .align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.width(iconTextSpace.dp))
                Text(
                    text = MenuState.GENERATOR.customName,
                    textAlign = TextAlign.Center,
                    fontSize = fontSize.sp,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
            Spacer(modifier = Modifier.weight(1f)) // Push About to the bottom
            Divider(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { menuState.value = MenuState.ABOUT }
                    .background(color = Color.Transparent)
                    .padding(vertical = buttonPadding.dp)
                    .offset(x = textOffset.dp)
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = MenuState.ABOUT.name,
                    modifier = Modifier
                        .size(size = iconSize.dp)
                        .align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.width(iconTextSpace.dp))
                Text(
                    text = MenuState.ABOUT.customName,
                    textAlign = TextAlign.Center,
                    fontSize = fontSize.sp,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }
    }
}

@Composable
fun Content(
    menuState: MutableState<MenuState>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(
                start = 0.dp, // No padding on the left side
                top = 10.dp,
                end = 10.dp,
                bottom = 10.dp
            )
            .clip(RoundedCornerShape(8.dp)) // Rounded corners for box (Corners of Row are contained within)
            .border(
                BorderStroke(1.dp, Color.LightGray),
                shape = RoundedCornerShape(8.dp) // Rounded corners with radius 8dp
            )
    ) {
        when (menuState.value) {
            MenuState.ABOUT -> AboutAppTab()
            MenuState.ADD_TRAIN -> TODO()
            MenuState.ALL_TRAINS -> TODO()
            MenuState.SCRAPER -> Scraper(modifier)
            MenuState.PROCESSOR -> TODO()
            MenuState.GENERATOR -> TODO()
        }
    }
}

@Composable
fun Scraper(
    modifier: Modifier = Modifier,
    scraperMenuState: MutableState<ScraperMenuState> = remember { mutableStateOf(ScraperMenuState.RESET) },
    buttonPadding: Int = 10,
    textOffset: Int = 25,
    iconSize: Int = 20,
    fontSize: Int = 16,
    iconTextSpace: Int = 6,
) {
    Column(modifier) {
        val functionProgress = remember { mutableStateOf("") }
        // Bar (row) at the top
        Surface(
            modifier = Modifier
                .fillMaxWidth()
            //.height(50.dp)
        ) {
            Row(
                modifier = modifier
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { scraperMenuState.value = ScraperMenuState.OFFICIAL }
                        .background(if (scraperMenuState.value == ScraperMenuState.OFFICIAL) Color.LightGray else Color.Transparent)
                        .padding(vertical = buttonPadding.dp)
                        .align(Alignment.CenterVertically)
                        .wrapContentWidth(Alignment.CenterHorizontally) // align horizontally
                ) {
                    Icon(
                        Icons.Default.Train,
                        contentDescription = ScraperMenuState.OFFICIAL.name,
                        modifier = Modifier
                            .size(size = iconSize.dp)
                            .align(Alignment.CenterVertically)
                    )
                    Spacer(modifier = Modifier.width(iconTextSpace.dp)) // spacing between icon and text
                    Text(
                        text = ScraperMenuState.OFFICIAL.customName,
                        textAlign = TextAlign.Center,
                        fontSize = fontSize.sp
                    )
                }
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { scraperMenuState.value = ScraperMenuState.VLAKSI }
                        .background(if (scraperMenuState.value == ScraperMenuState.VLAKSI) Color.LightGray else Color.Transparent)
                        .padding(vertical = buttonPadding.dp)
                        .align(Alignment.CenterVertically)
                        .wrapContentWidth(Alignment.CenterHorizontally)

                ) {
                    Icon(
                        Icons.Default.Subway,
                        contentDescription = ScraperMenuState.VLAKSI.name,
                        modifier = Modifier
                            .size(size = iconSize.dp)
                            .align(Alignment.CenterVertically)
                    )
                    Spacer(modifier = Modifier.width(iconTextSpace.dp))
                    Text(
                        text = ScraperMenuState.VLAKSI.customName,
                        textAlign = TextAlign.Center,
                        fontSize = fontSize.sp
                    )
                }
                Row(
                    modifier = Modifier
                        .width(50.dp)
                        .clickable { scraperMenuState.value = ScraperMenuState.RESET }
                        .background(Color.Transparent)
                        .padding(vertical = buttonPadding.dp)
                        .align(Alignment.CenterVertically)
                        .wrapContentWidth(Alignment.CenterHorizontally)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = ScraperMenuState.RESET.name,
                        modifier = Modifier
                            .size(size = iconSize.dp)
                            .align(Alignment.CenterVertically)
                    )
                    Text(
                        //text = ScraperMenuState.VLAKSI.customName,
                        text = "",
                        textAlign = TextAlign.Center,
                        fontSize = fontSize.sp
                    )
                }
            }
        }
        Divider(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
        // Content box that can be changed
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
            when (scraperMenuState.value) {
                ScraperMenuState.OFFICIAL -> ScraperGetAndProcessData(SourceWebsite.Official)
                ScraperMenuState.VLAKSI -> ScraperGetAndProcessData(SourceWebsite.Vlaksi)
                ScraperMenuState.RESET -> ScraperReset()
            }
            // You can change the content of this box dynamically
            // For example:
            // Text("Dynamic Content")
        }
    }
}

@Composable
fun ScraperGetAndProcessData(
    sourceWebsite: SourceWebsite,
    modifier: Modifier = Modifier
) {
    // State to hold the result of the operation
    val resultState = remember { mutableStateOf<Map<String, Any?>?>(null) }
    val resultStateStations = remember { mutableStateOf<Map<String, Any?>?>(null) }

    // State to hold the loading status
    val isLoading = remember { mutableStateOf(false) }

    // LaunchedEffect to trigger the data fetching operation
    LaunchedEffect(Unit) {
        isLoading.value = true // Set loading to true before fetching data
        try {
            // Coroutine call - data fetch
            withContext(Dispatchers.IO) { getDataAndProcess(sourceWebsite, resultState) }
            withContext(Dispatchers.IO) { getStationsAndProcess(resultStateStations) }

        } catch (e: Exception) {
            println("An error occurred: ${e.message}")
        } finally {
            isLoading.value = false // Set loading to false after fetching data
        }
    }

    // Loading indicator - data processing
    if (isLoading.value) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .padding(16.dp)
        ) {
            CircularProgressIndicator()
        }
    } else {
        // Display data
        Box(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()) // Box with enabled scroll
        ) {
            resultState.value?.let { result ->
                Text("Result: $result")
            }
        }
        /*resultState.value?.let { result ->
            Text("Result: $result", modifier = Modifier.padding(16.dp))
        }*/
    }
}

@Composable
fun ScraperReset(
    modifier: Modifier = Modifier,
    titleFontSize: Int = 20,
    bodyFontSize: Int = 14
) {
    Column(
        modifier = Modifier.fillMaxSize()
            .fillMaxSize()
            .wrapContentSize(Alignment.Center),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TitleText(
            text = "Choose data source",
            fontSize = titleFontSize,
            //modifier = Modifier.padding(bottom = 16.dp, top = 10.dp)
        )
    }
}

@Composable
fun AboutAppTab(
    modifier: Modifier = Modifier,
    titleFontSize: Int = 20,
    bodyFontSize: Int = 14
) {
    Column(
        modifier = Modifier.fillMaxSize()
            .fillMaxSize()
            .wrapContentSize(Alignment.Center),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TitleText(
            text = "BandOfBytes",
            fontSize = titleFontSize,
            modifier = Modifier.padding(bottom = 16.dp, top = 10.dp)
        )
        BodyText("Nejc Rozman", bodyFontSize)
        BodyText("Jernej Denac", bodyFontSize)
        BodyText("Marko Kurnik", bodyFontSize)
    }
}

@Composable
fun TitleText(
    text: String,
    fontSize: Int,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        fontSize = fontSize.sp,
        textAlign = TextAlign.Center,
        fontFamily = FontFamily.SansSerif,
        modifier = modifier
    )
}

@Composable
fun BodyText(
    text: String,
    fontSize: Int
) {
    Text(
        text = text,
        fontSize = fontSize.sp,
        textAlign = TextAlign.Center,
        fontFamily = FontFamily.SansSerif
    )
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = NAME,
        state = rememberWindowState(
            position = WindowPosition.Aligned(Alignment.Center),
            size = DpSize(800.dp, 600.dp)
        ),
        undecorated = false,
        resizable = true
    ) {
        App()
    }
}
