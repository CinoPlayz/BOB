package gui.addData

import TitleText
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import models.Token
import models.UserInsert
import org.bson.Document
import org.mindrot.jbcrypt.BCrypt
import utils.DatabaseUtil
import java.time.LocalDateTime

@Composable
fun AddUser(
    modifier: Modifier = Modifier,
    titleFontSize: Int = 20
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TitleText(
            text = "Add new user to database",
            fontSize = titleFontSize,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        InputUserData(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
    }
}

@Composable
fun InputUserData(
    modifier: Modifier = Modifier,
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    var password by remember { mutableStateOf("") }
    var passwordRepeat by remember { mutableStateOf("") }

    var role by remember { mutableStateOf("") }

    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isPasswordRepeatVisible by remember { mutableStateOf(false) }

    val onReset: () -> Unit = {
        username = ""
        email = ""
        password = ""
        passwordRepeat = ""
        role = ""
    }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(state = rememberScrollState())
    ) {
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(end = 8.dp)
            )

            PasswordVisibilityToggleButton(
                isPasswordVisible = isPasswordVisible,
                onToggle = { isPasswordVisible = !isPasswordVisible },
                modifier = Modifier
                    .fillMaxHeight()
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = passwordRepeat,
                onValueChange = { passwordRepeat = it },
                label = { Text("Repeat Password") },
                visualTransformation = if (isPasswordRepeatVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(end = 8.dp)
            )

            PasswordVisibilityToggleButton(
                isPasswordVisible = isPasswordRepeatVisible,
                onToggle = { isPasswordRepeatVisible = !isPasswordRepeatVisible },
                modifier = Modifier
                    .fillMaxHeight()
            )
        }

        Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("New user role:", style = MaterialTheme.typography.body1)
                Spacer(modifier = Modifier.width(20.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = role == "user",
                        onClick = { role = "user" }
                    )
                    Text("User")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = role == "moderator",
                        onClick = { role = "moderator" },
                        enabled = false // disable, role created as placeholder for future development
                    )
                    Text("Moderator")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = role == "admin",
                        onClick = { role = "admin" }
                    )
                    Text("Admin")
                }
            }
        }

        Button(
            onClick = {
                coroutineScope.launch {
                    val feedback = writeUserToDB(
                        username = username,
                        email = email,
                        password = password,
                        passwordRepeat = passwordRepeat,
                        role = role,
                        onReset = onReset // Pass the reset callback
                    )

                    feedbackMessage = feedback
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            Text("Write route to database")
        }
    }

    feedbackMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { feedbackMessage = null }, // Reset feedback message on dismiss
            //title = { Text("Feedback") },
            text = { Text(message) },
            confirmButton = {
                Button(
                    onClick = { feedbackMessage = null }, // Reset feedback message on confirm
                ) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun PasswordVisibilityToggleButton(
    isPasswordVisible: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon: ImageVector = if (isPasswordVisible) {
        Icons.Filled.VisibilityOff
    } else {
        Icons.Filled.Visibility
    }

    OutlinedButton(
        onClick = { onToggle() },
        modifier = modifier
    ) {
        Icon(icon, contentDescription = "Toggle password visibility")
    }
}

suspend fun writeUserToDB(
    username: String,
    email: String,
    password: String,
    passwordRepeat: String,
    role: String,
    onReset: () -> Unit
): String {

    if (username.isEmpty() ||
        email.isEmpty() ||
        password.isEmpty() ||
        passwordRepeat.isEmpty() ||
        role.isEmpty()
    ) {
        return ("Please fill in all fields.")
    }

    if (password != passwordRepeat) {
        return ("Passwords do not match.")
    }

    val emailRegex = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
    if (!emailRegex.matches(email)) {
        return ("Invalid email format.")
    }

    val salt = BCrypt.gensalt(10) // ekvivalent ZPBackend
    val passwordHash = BCrypt.hashpw(password, salt)

    val userInsert = UserInsert(
        username = username,
        email = email,
        password = passwordHash,
        tokens = emptyList(),
        twoFactorAuthenticationEnabled = false,
        twoFactorAuthenticationSecret = null,
        role = role,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now(),
    )

    val dbConnection = DatabaseUtil.connectDB() ?: return "Failed to connect to the database."

    val jsonString = Json.encodeToString(userInsert)
    val document = Document.parse(jsonString)

    return try {
        dbConnection.getDatabase("ZP").getCollection("users").insertOne(document)
        onReset()
        "User successfully written to the database."
    } catch (e: Exception) {
        "Error writing data to the database: ${e.message}"
    }
}