package gui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun CustomDropdownMenu(
    label: String,
    options: List<String>,
    initialSelection: String? = null,
    onSelectionChange: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    //var selectedOption by remember { mutableStateOf("") }
    var selectedOption by remember { mutableStateOf(initialSelection ?: "") }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedOption,
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
                    selectedOption = option
                    onSelectionChange(option)
                    expanded = false
                }) {
                    Text(text = option)
                }
            }
        }
    }
}

@Composable
fun CustomDropdownMenuInt(
    label: String,
    options: List<Int>,
    initialSelection: Int? = null,
    onSelectionChange: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(initialSelection ?: 0) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedOption.toString(),
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
                    selectedOption = option
                    onSelectionChange(option)
                    expanded = false
                }) {
                    Text(text = option.toString())
                }
            }
        }
    }
}
