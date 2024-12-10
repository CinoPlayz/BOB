package gui.scraper

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gui.scraper.process.SourceWebsite

enum class ScraperMenuState(val customName: String) {
    OFFICIAL("SZ Official"),
    VLAKSI("Vlak.si"),
    STATIONS("Stations"),
    ROUTES("Routes"),
    RESET("Reset")
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
                //Official
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
                //VlakSi
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
                //Stations
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { scraperMenuState.value = ScraperMenuState.STATIONS }
                        .background(if (scraperMenuState.value == ScraperMenuState.STATIONS) Color.LightGray else Color.Transparent)
                        .padding(vertical = buttonPadding.dp)
                        .align(Alignment.CenterVertically)
                        .wrapContentWidth(Alignment.CenterHorizontally)

                ) {
                    Icon(
                        Icons.Default.Apartment,
                        contentDescription = ScraperMenuState.STATIONS.name,
                        modifier = Modifier
                            .size(size = iconSize.dp)
                            .align(Alignment.CenterVertically)
                    )
                    Spacer(modifier = Modifier.width(iconTextSpace.dp))
                    Text(
                        text = ScraperMenuState.STATIONS.customName,
                        textAlign = TextAlign.Center,
                        fontSize = fontSize.sp
                    )
                }
                //Routes
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { scraperMenuState.value = ScraperMenuState.ROUTES }
                        .background(if (scraperMenuState.value == ScraperMenuState.ROUTES) Color.LightGray else Color.Transparent)
                        .padding(vertical = buttonPadding.dp)
                        .align(Alignment.CenterVertically)
                        .wrapContentWidth(Alignment.CenterHorizontally)

                ) {
                    Icon(
                        Icons.Default.Route,
                        contentDescription = ScraperMenuState.ROUTES.name,
                        modifier = Modifier
                            .size(size = iconSize.dp)
                            .align(Alignment.CenterVertically)
                    )
                    Spacer(modifier = Modifier.width(iconTextSpace.dp))
                    Text(
                        text = ScraperMenuState.ROUTES.customName,
                        textAlign = TextAlign.Center,
                        fontSize = fontSize.sp
                    )
                }
                //Reset
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
                ScraperMenuState.OFFICIAL -> ScraperData(SourceWebsite.Official)
                ScraperMenuState.VLAKSI -> ScraperData(SourceWebsite.Vlaksi)
                ScraperMenuState.STATIONS -> ScraperStations()
                ScraperMenuState.ROUTES -> ScraperRoutes()
                ScraperMenuState.RESET -> ScraperReset()
            }
        }
    }
}