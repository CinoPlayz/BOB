package gui.addData

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

enum class AddDataMenuState(val customName: String) {
    ADD_TRAIN("Add Train"),
    ADD_ROUTE("Add Route"),
    ADD_STATION("Add Station"),
    ADD_USER("Add user"),
    RESET("Reset")
}

@Composable
fun AddDataMenu(
    modifier: Modifier = Modifier,
    addDataMenuState: MutableState<AddDataMenuState> = remember { mutableStateOf(AddDataMenuState.RESET) },
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
                        .clickable { addDataMenuState.value = AddDataMenuState.ADD_TRAIN }
                        .background(if (addDataMenuState.value == AddDataMenuState.ADD_TRAIN) Color.LightGray else Color.Transparent)
                        .padding(vertical = buttonPadding.dp)
                        .align(Alignment.CenterVertically)
                        .wrapContentWidth(Alignment.CenterHorizontally) // align horizontally
                ) {
                    Icon(
                        Icons.Default.Train,
                        contentDescription = AddDataMenuState.ADD_TRAIN.name,
                        modifier = Modifier
                            .size(size = iconSize.dp)
                            .align(Alignment.CenterVertically)
                    )
                    Spacer(modifier = Modifier.width(iconTextSpace.dp)) // spacing between icon and text
                    Text(
                        text = AddDataMenuState.ADD_TRAIN.customName,
                        textAlign = TextAlign.Center,
                        fontSize = fontSize.sp
                    )
                }
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { addDataMenuState.value = AddDataMenuState.ADD_ROUTE }
                        .background(if (addDataMenuState.value == AddDataMenuState.ADD_ROUTE) Color.LightGray else Color.Transparent)
                        .padding(vertical = buttonPadding.dp)
                        .align(Alignment.CenterVertically)
                        .wrapContentWidth(Alignment.CenterHorizontally)

                ) {
                    Icon(
                        Icons.Default.Route,
                        contentDescription = AddDataMenuState.ADD_ROUTE.name,
                        modifier = Modifier
                            .size(size = iconSize.dp)
                            .align(Alignment.CenterVertically)
                    )
                    Spacer(modifier = Modifier.width(iconTextSpace.dp))
                    Text(
                        text = AddDataMenuState.ADD_ROUTE.customName,
                        textAlign = TextAlign.Center,
                        fontSize = fontSize.sp
                    )
                }
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { addDataMenuState.value = AddDataMenuState.ADD_STATION }
                        .background(if (addDataMenuState.value == AddDataMenuState.ADD_STATION) Color.LightGray else Color.Transparent)
                        .padding(vertical = buttonPadding.dp)
                        .align(Alignment.CenterVertically)
                        .wrapContentWidth(Alignment.CenterHorizontally) // align horizontally
                ) {
                    Icon(
                        Icons.Default.House,
                        contentDescription = AddDataMenuState.ADD_STATION.name,
                        modifier = Modifier
                            .size(size = iconSize.dp)
                            .align(Alignment.CenterVertically)
                    )
                    Spacer(modifier = Modifier.width(iconTextSpace.dp)) // spacing between icon and text
                    Text(
                        text = AddDataMenuState.ADD_STATION.customName,
                        textAlign = TextAlign.Center,
                        fontSize = fontSize.sp
                    )
                }
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { addDataMenuState.value = AddDataMenuState.ADD_USER }
                        .background(if (addDataMenuState.value == AddDataMenuState.ADD_USER) Color.LightGray else Color.Transparent)
                        .padding(vertical = buttonPadding.dp)
                        .align(Alignment.CenterVertically)
                        .wrapContentWidth(Alignment.CenterHorizontally) // align horizontally
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = AddDataMenuState.ADD_USER.name,
                        modifier = Modifier
                            .size(size = iconSize.dp)
                            .align(Alignment.CenterVertically)
                    )
                    Spacer(modifier = Modifier.width(iconTextSpace.dp)) // spacing between icon and text
                    Text(
                        text = AddDataMenuState.ADD_USER.customName,
                        textAlign = TextAlign.Center,
                        fontSize = fontSize.sp
                    )
                }
                Row(
                    modifier = Modifier
                        .width(50.dp)
                        .clickable { addDataMenuState.value = AddDataMenuState.RESET }
                        .background(Color.Transparent)
                        .padding(vertical = buttonPadding.dp)
                        .align(Alignment.CenterVertically)
                        .wrapContentWidth(Alignment.CenterHorizontally)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = AddDataMenuState.RESET.name,
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
            when (addDataMenuState.value) {
                AddDataMenuState.ADD_TRAIN -> AddTrain()
                AddDataMenuState.ADD_ROUTE -> AddRoute()
                AddDataMenuState.ADD_STATION -> AddStation()
                AddDataMenuState.ADD_USER -> AddUser()
                AddDataMenuState.RESET -> AddDataReset()
            }
        }
    }
}



fun AddStation() {
    TODO("Add station")
}

fun AddUser() {
    TODO("Add user")
}

@Composable
fun AddDataReset(
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
            text = "Choose data to enter",
            fontSize = titleFontSize,
            //modifier = Modifier.padding(bottom = 16.dp, top = 10.dp)
        )
    }
}