package gui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import models.Route
import models.Station

// DaysOfWeekUtils.kt

val daysOfWeek = listOf(
    "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Holidays"
)

// Create a map from the daysOfWeek list
val daysOfWeekMap = daysOfWeek.mapIndexed { index, day -> day to index }.toMap()

// Function to transform list of stations into list of pairs
fun List<Station>.toNameIDPairs(): List<Pair<String, String>> {
    return this.map { station ->
        station.name to station.id
    }
}

@Composable
fun StationsDropdownMenu(
    label: String,
    options: List<Pair<String, String>>, // name, id
    originalSelection: String = "",
    onSelectionChange: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(originalSelection) }
    var selectedName by remember(originalSelection) {
        mutableStateOf(options.find { it.second == originalSelection }?.first ?: "")
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = { }, // Disable text editing
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                IconButton(
                    onClick = { expanded = !expanded },
                ) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = "Dropdown Menu")
                }
            },
            modifier = Modifier
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
        ) {
            options.forEach { option ->
                DropdownMenuItem(onClick = {
                    selectedOption = option.second
                    selectedName = option.first
                    onSelectionChange(option.second)
                    expanded = false
                }) {
                    Text(text = option.first)
                }
            }
        }
    }
}

fun List<Route>.toNumberIDPairs(): List<Pair<Int, String>> {
    return this.map { route ->
        route.trainNumber to route.id
    }
}

@Composable
fun RoutesDropdownMenu(
    label: String,
    options: List<Pair<Int, String>>, // trainNumber, id
    originalSelection: String = "",
    onSelectionChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val sortedOptions = remember(options) { options.sortedBy { it.first } } // sorted dropdown
    var selectedOption by remember { mutableStateOf(originalSelection) }
    var selectedName by remember(originalSelection) {
        mutableStateOf(options.find { it.second == originalSelection }?.first?.toString() ?: "")
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = { }, // Disable text editing
            readOnly = true,
            label = { Text(label) },

            trailingIcon = {
                IconButton(
                    onClick = { expanded = !expanded },
                ) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = "Dropdown Menu")
                }
            },
            modifier = Modifier
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
        ) {
            sortedOptions.forEach { option ->
                DropdownMenuItem(
                    onClick = {
                        selectedOption = option.second
                        selectedName = option.first.toString()
                        onSelectionChange(option.second)
                        expanded = false
                    },
                ) {
                    Text(text = option.first.toString())
                }
            }
        }
    }
}

fun parseTime(timeStr: String): Triple<String, String, String> {
    // Split the input string by the colon separator
    val parts = timeStr.split(":")

    // Assign the split parts to hours, minutes, and seconds
    val hours = parts[0]
    val minutes = parts[1]
    val seconds = parts[2]

    return Triple(hours, minutes, seconds)
}

fun addLeadingZero(value: String): String {
    return if (value.length == 1) "0$value" else value
}