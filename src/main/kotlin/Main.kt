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
import gui.Content
import gui.Menu
import gui.MenuState
import gui.scraper.Scraper
import gui.addData.AddDataMenu
import gui.dataProcessor.DataProcessor
import gui.login.LoginScreen
import gui.manageData.ManageDataMenu
import utils.context.initializeAppContext

const val NAME = "BandOfBytes"

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
        initializeAppContext()
        App()
    }
}

@Composable
@Preview
fun App() {
    var isAuthenticated by remember { mutableStateOf(false) }

    if (isAuthenticated) {
        val menuState = remember { mutableStateOf(MenuState.ABOUT) }

        MaterialTheme {
            Row(modifier = Modifier.fillMaxSize()) {
                Menu(menuState, modifier = Modifier.weight(1f))
                Content(menuState, modifier = Modifier.weight(2f))
            }
        }
    } else {
        LoginScreen(onLoginSuccess = { isAuthenticated = true })
    }

    /*val menuState = remember { mutableStateOf(MenuState.ABOUT) }

    MaterialTheme {
        Row(modifier = Modifier.fillMaxSize()) {
            Menu(menuState, modifier = Modifier.weight(1f))
            Content(menuState, modifier = Modifier.weight(2f))
        }
    }*/
}




