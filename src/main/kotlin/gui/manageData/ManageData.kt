package gui.manageData

import TitleText
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ManageDataMenuState(val customName: String) {
    MANAGE_TRAINS("Trains"),
    MANAGE_ROUTES("Routes"),
    MANAGE_DELAYS("Delays"),
    MANAGE_STATIONS("Stations"),
    MANAGE_USERS("Users"),
    RESET("Reset")
}

@Composable
fun ManageDataMenu(
    modifier: Modifier = Modifier,
    manageDataMenuState: MutableState<ManageDataMenuState> = remember { mutableStateOf(ManageDataMenuState.RESET) },
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
                        //.clickable { manageDataMenuState.value = ManageDataMenuState.MANAGE_TRAINS }
                        .background(if (manageDataMenuState.value == ManageDataMenuState.MANAGE_TRAINS) Color.LightGray else Color.Transparent)
                        .padding(vertical = buttonPadding.dp)
                        .align(Alignment.CenterVertically)
                        .wrapContentWidth(Alignment.CenterHorizontally) // align horizontally
                ) {
                    Icon(
                        Icons.Default.Train,
                        contentDescription = ManageDataMenuState.MANAGE_TRAINS.name,
                        modifier = Modifier
                            .size(size = iconSize.dp)
                            .align(Alignment.CenterVertically)
                    )
                    Spacer(modifier = Modifier.width(iconTextSpace.dp)) // spacing between icon and text
                    Text(
                        text = ManageDataMenuState.MANAGE_TRAINS.customName,
                        textAlign = TextAlign.Center,
                        fontSize = fontSize.sp
                    )
                }
                Row(
                    modifier = Modifier
                        .weight(1f)
                        //.clickable { manageDataMenuState.value = ManageDataMenuState.MANAGE_ROUTES }
                        .background(if (manageDataMenuState.value == ManageDataMenuState.MANAGE_ROUTES) Color.LightGray else Color.Transparent)
                        .padding(vertical = buttonPadding.dp)
                        .align(Alignment.CenterVertically)
                        .wrapContentWidth(Alignment.CenterHorizontally)

                ) {
                    Icon(
                        Icons.Default.Route,
                        contentDescription = ManageDataMenuState.MANAGE_ROUTES.name,
                        modifier = Modifier
                            .size(size = iconSize.dp)
                            .align(Alignment.CenterVertically)
                    )
                    Spacer(modifier = Modifier.width(iconTextSpace.dp))
                    Text(
                        text = ManageDataMenuState.MANAGE_ROUTES.customName,
                        textAlign = TextAlign.Center,
                        fontSize = fontSize.sp
                    )
                }
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { manageDataMenuState.value = ManageDataMenuState.MANAGE_DELAYS }
                        .background(if (manageDataMenuState.value == ManageDataMenuState.MANAGE_DELAYS) Color.LightGray else Color.Transparent)
                        .padding(vertical = buttonPadding.dp)
                        .align(Alignment.CenterVertically)
                        .wrapContentWidth(Alignment.CenterHorizontally) // align horizontally
                ) {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = ManageDataMenuState.MANAGE_DELAYS.name,
                        modifier = Modifier
                            .size(size = iconSize.dp)
                            .align(Alignment.CenterVertically)
                    )
                    Spacer(modifier = Modifier.width(iconTextSpace.dp)) // spacing between icon and text
                    Text(
                        text = ManageDataMenuState.MANAGE_DELAYS.customName,
                        textAlign = TextAlign.Center,
                        fontSize = fontSize.sp
                    )
                }
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { manageDataMenuState.value = ManageDataMenuState.MANAGE_STATIONS }
                        .background(if (manageDataMenuState.value == ManageDataMenuState.MANAGE_STATIONS) Color.LightGray else Color.Transparent)
                        .padding(vertical = buttonPadding.dp)
                        .align(Alignment.CenterVertically)
                        .wrapContentWidth(Alignment.CenterHorizontally) // align horizontally
                ) {
                    Icon(
                        Icons.Default.House,
                        contentDescription = ManageDataMenuState.MANAGE_STATIONS.name,
                        modifier = Modifier
                            .size(size = iconSize.dp)
                            .align(Alignment.CenterVertically)
                    )
                    Spacer(modifier = Modifier.width(iconTextSpace.dp)) // spacing between icon and text
                    Text(
                        text = ManageDataMenuState.MANAGE_STATIONS.customName,
                        textAlign = TextAlign.Center,
                        fontSize = fontSize.sp
                    )
                }
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { manageDataMenuState.value = ManageDataMenuState.MANAGE_USERS }
                        .background(if (manageDataMenuState.value == ManageDataMenuState.MANAGE_USERS) Color.LightGray else Color.Transparent)
                        .padding(vertical = buttonPadding.dp)
                        .align(Alignment.CenterVertically)
                        .wrapContentWidth(Alignment.CenterHorizontally) // align horizontally
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = ManageDataMenuState.MANAGE_USERS.name,
                        modifier = Modifier
                            .size(size = iconSize.dp)
                            .align(Alignment.CenterVertically)
                    )
                    Spacer(modifier = Modifier.width(iconTextSpace.dp)) // spacing between icon and text
                    Text(
                        text = ManageDataMenuState.MANAGE_USERS.customName,
                        textAlign = TextAlign.Center,
                        fontSize = fontSize.sp
                    )
                }
                Row(
                    modifier = Modifier
                        .width(50.dp)
                        .clickable { manageDataMenuState.value = ManageDataMenuState.RESET }
                        .background(Color.Transparent)
                        .padding(vertical = buttonPadding.dp)
                        .align(Alignment.CenterVertically)
                        .wrapContentWidth(Alignment.CenterHorizontally)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = ManageDataMenuState.RESET.name,
                        modifier = Modifier
                            .size(size = iconSize.dp)
                            .align(Alignment.CenterVertically)
                    )
                    Text(
                        //text = AddDataMenuState.RESET.customName,
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
            when (manageDataMenuState.value) {
                ManageDataMenuState.MANAGE_TRAINS -> TODO()
                ManageDataMenuState.MANAGE_ROUTES -> TODO()
                ManageDataMenuState.MANAGE_DELAYS -> ManageDelays()
                ManageDataMenuState.MANAGE_STATIONS -> ManageStations()
                ManageDataMenuState.MANAGE_USERS -> ManageUsers()
                ManageDataMenuState.RESET -> ManageDataReset()
            }
        }
    }
}

@Composable
fun ManageDataReset(
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
            text = "Choose data to manage",
            fontSize = titleFontSize,
            //modifier = Modifier.padding(bottom = 16.dp, top = 10.dp)
        )
    }
}