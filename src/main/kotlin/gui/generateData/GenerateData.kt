package gui.generateData

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Train
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gui.TitleText

enum class GenerateDataMenuState(val customName: String) {
    TLH("Train Location History"),
    DELAY("Delay"),
    RESET("Reset")
}

@Composable
fun GenerateData(
    modifier: Modifier = Modifier,
    generateDataMenuState: MutableState<GenerateDataMenuState> = remember { mutableStateOf(GenerateDataMenuState.RESET) },
    buttonPadding: Int = 10,
    iconSize: Int = 20,
    fontSize: Int = 16,
    iconTextSpace: Int = 6,
) {
    Column(modifier) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = modifier
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { generateDataMenuState.value = GenerateDataMenuState.TLH }
                        .background(if (generateDataMenuState.value == GenerateDataMenuState.TLH) Color.LightGray else Color.Transparent)
                        .padding(vertical = buttonPadding.dp)
                        .align(Alignment.CenterVertically)
                        .wrapContentWidth(Alignment.CenterHorizontally)
                ) {
                    Icon(
                        Icons.Default.Train,
                        contentDescription = GenerateDataMenuState.TLH.name,
                        modifier = Modifier
                            .size(size = iconSize.dp)
                            .align(Alignment.CenterVertically)
                    )
                    Spacer(modifier = Modifier.width(iconTextSpace.dp))
                    Text(
                        text = GenerateDataMenuState.TLH.customName,
                        textAlign = TextAlign.Center,
                        fontSize = fontSize.sp
                    )
                }
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { generateDataMenuState.value = GenerateDataMenuState.DELAY }
                        .background(if (generateDataMenuState.value == GenerateDataMenuState.DELAY) Color.LightGray else Color.Transparent)
                        .padding(vertical = buttonPadding.dp)
                        .align(Alignment.CenterVertically)
                        .wrapContentWidth(Alignment.CenterHorizontally)

                ) {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = GenerateDataMenuState.DELAY.name,
                        modifier = Modifier
                            .size(size = iconSize.dp)
                            .align(Alignment.CenterVertically)
                    )
                    Spacer(modifier = Modifier.width(iconTextSpace.dp))
                    Text(
                        text = GenerateDataMenuState.DELAY.customName,
                        textAlign = TextAlign.Center,
                        fontSize = fontSize.sp
                    )
                }
                Row(
                    modifier = Modifier
                        .width(50.dp)
                        .clickable { generateDataMenuState.value = GenerateDataMenuState.RESET }
                        .background(Color.Transparent)
                        .padding(vertical = buttonPadding.dp)
                        .align(Alignment.CenterVertically)
                        .wrapContentWidth(Alignment.CenterHorizontally)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = GenerateDataMenuState.RESET.name,
                        modifier = Modifier
                            .size(size = iconSize.dp)
                            .align(Alignment.CenterVertically)
                    )
                    Text(
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
            when (generateDataMenuState.value) {
                GenerateDataMenuState.TLH -> GenerateDataTLHView()
                GenerateDataMenuState.DELAY -> GenerateDataDelayView()
                GenerateDataMenuState.RESET -> GenerateDataReset()
            }
        }
    }
}

@Composable
fun GenerateDataReset(
    modifier: Modifier = Modifier,
    titleFontSize: Int = 20,
) {
    Column(
        modifier = Modifier.fillMaxSize()
            .fillMaxSize()
            .wrapContentSize(Alignment.Center),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TitleText(
            text = "Choose data to generate",
            fontSize = titleFontSize,
        )
    }
}