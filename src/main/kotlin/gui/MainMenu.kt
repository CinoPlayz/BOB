package gui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gui.addData.AddDataMenu
import gui.dataProcessor.DataProcessor
import gui.login.logoutUserBackend
import gui.manageData.ManageDataMenu
import gui.scraper.Scraper
import kotlinx.coroutines.launch

enum class MenuState(val customName: String) {
    ADD_DATA("Add Data"),
    MANAGE_DATA("Manage Data"),
    SCRAPER("Scraper"),
    PROCESSOR("Data Processor"),
    GENERATOR("Generator"),
    ABOUT("About")
}

@Composable
fun Menu(
    menuState: MutableState<MenuState>,
    modifier: Modifier = Modifier,
    buttonPadding: Int = 10,
    textOffset: Int = 25,
    iconSize: Int = 20,
    fontSize: Int = 16, //
    iconTextSpace: Int = 6,
    onLogout: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

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
                    .clickable { menuState.value = MenuState.ADD_DATA }
                    .background(if (menuState.value == MenuState.ADD_DATA) Color.LightGray else Color.Transparent)
                    .padding(vertical = buttonPadding.dp)
                    //.wrapContentWidth(Alignment.CenterHorizontally)
                    .offset(x = textOffset.dp)

            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = MenuState.ADD_DATA.name,
                    modifier = Modifier
                        .size(size = iconSize.dp)
                        .align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.width(iconTextSpace.dp)) // spacing between icon and text
                Text(
                    text = MenuState.ADD_DATA.customName,
                    textAlign = TextAlign.Center,
                    fontSize = fontSize.sp,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
            Divider(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { menuState.value = MenuState.MANAGE_DATA }
                    .background(if (menuState.value == MenuState.MANAGE_DATA) Color.LightGray else Color.Transparent)
                    .background(color = Color.Transparent)
                    .padding(vertical = buttonPadding.dp)
                    .offset(x = textOffset.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.List,
                    contentDescription = MenuState.MANAGE_DATA.name,
                    modifier = Modifier
                        .size(size = iconSize.dp)
                        .align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.width(iconTextSpace.dp))
                Text(
                    text = MenuState.MANAGE_DATA.customName,
                    textAlign = TextAlign.Center,
                    fontSize = fontSize.sp,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { menuState.value = MenuState.PROCESSOR }
                    .background(if (menuState.value == MenuState.PROCESSOR) Color.LightGray else Color.Transparent)
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
                    .background(if (menuState.value == MenuState.SCRAPER) Color.LightGray else Color.Transparent)
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
                    .background(if (menuState.value == MenuState.GENERATOR) Color.LightGray else Color.Transparent)
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onLogout()
                        coroutineScope.launch {
                            logoutUserBackend()
                        }
                    }
                    .background(color = Color.Transparent)
                    .padding(vertical = buttonPadding.dp)
                    .offset(x = textOffset.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Logout",
                    modifier = Modifier
                        .size(size = iconSize.dp)
                        .align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.width(iconTextSpace.dp))
                Text(
                    text = "Logout",
                    textAlign = TextAlign.Center,
                    fontSize = fontSize.sp,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
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
            MenuState.ADD_DATA -> AddDataMenu()
            MenuState.MANAGE_DATA -> ManageDataMenu()
            MenuState.SCRAPER -> Scraper(modifier)
            MenuState.PROCESSOR -> DataProcessor()
            MenuState.GENERATOR -> TODO()
        }
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