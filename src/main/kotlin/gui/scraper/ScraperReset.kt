package gui.scraper

import TitleText
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

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