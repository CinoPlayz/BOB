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
import gui.scraper.Scraper
import gui.addData.AddDataMenu
import gui.manageData.ManageDataMenu

const val NAME = "BandOfBytes"

enum class MenuState(val customName: String) {
    ADD_DATA("Add Data"),
    MANAGE_DATA("Manage Data"),
    SCRAPER("Scraper"),
    PROCESSOR("//Data Processor//"),
    GENERATOR("//Generator//"),
    ABOUT("About")
}

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
                    .clickable { menuState.value = MenuState.ADD_DATA }
                    .background(color = Color.Transparent)
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
            MenuState.ADD_DATA -> AddDataMenu()
            MenuState.MANAGE_DATA -> ManageDataMenu()
            MenuState.SCRAPER -> Scraper(modifier)
            MenuState.PROCESSOR -> TODO()
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
