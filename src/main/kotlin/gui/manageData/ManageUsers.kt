package gui.manageData

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gui.TitleText
import gui.addData.PasswordVisibilityToggleButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import models.Token
import models.TokenInsert
import models.User
import models.UserUpdate
import utils.api.dao.deleteUser
import utils.api.dao.getAllUsers
import utils.api.dao.updateUser
import java.time.DateTimeException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ManageUsers(
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isLoading = remember { mutableStateOf(true) }

    // Update user in user-list after successful update in database
    fun updateUser(newUser: User) {
        // Update the list of users by replacing the user with the same ID
        users = users.map { if (it.id == newUser.id) newUser else it }
    }

    var userToDelete by remember { mutableStateOf<User?>(null) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    // Function to handle user deletion
    fun deleteUser(user: User) {
        // Set the user to delete
        userToDelete = user
    }

    // Confirmation dialog for deleting a user
    userToDelete?.let { user ->
        AlertDialog(
            onDismissRequest = {
                // Reset userToDelete when dialog is dismissed
                userToDelete = null
            },
            title = {
                Text(text = "Delete User")
            },
            text = {
                Text(text = "Are you sure you want to delete ${user.username}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Call deleteUserFromDB when confirmed
                        coroutineScope.launch {
                            val feedback = deleteUserFromDB(user.id) {
                                // If deletion is successful, remove the user from the list
                                users = users.filterNot { it.id == user.id }
                            }
                            // Reset userToDelete after deletion
                            userToDelete = null
                            // Show feedback message if needed
                            feedbackMessage = feedback
                        }
                    }
                ) {
                    Text(text = "Yes")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        // Dismiss the dialog
                        userToDelete = null
                    }
                ) {
                    Text(text = "No")
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            isLoading.value = true
            try {
                users = withContext(Dispatchers.IO) { getAllUsers() }
                // users = emptyList() // testing
                // users = getAllUsers()
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    if (isLoading.value) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .padding(16.dp)
        ) {
            CircularProgressIndicator()
        }
    } else {
        if (errorMessage != null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Text("Failed to load users: $errorMessage")
            }
        } else {
            val state = rememberLazyListState()

            if (users.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        TitleText(
                            text = "*** NO USERS ***",
                            fontSize = 20
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .padding(16.dp),
                state = state
            ) {
                items(users) { user ->
                    UserItem(
                        user = user,
                        onDeleteUser = { userToDelete -> deleteUser(userToDelete) },
                        // onUpdateUser = { users = users.map { if (it.id == user.id) user else it } }
                        onUpdateUser = { userToUpdate -> updateUser(userToUpdate) }
                    )
                }
                /*items(users) { user ->
                    UserItem(user = user)
                }*/
            }
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
fun UserItem(
    user: User,
    modifier: Modifier = Modifier,
    onDeleteUser: (User) -> Unit,
    onUpdateUser: (User) -> Unit // Function to update a single user
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    var editMode by remember { mutableStateOf(false) } // Track edit mode

    var username by remember { mutableStateOf(user.username) }
    var email by remember { mutableStateOf(user.email) }

    var password by remember { mutableStateOf("") }
    var passwordRepeat by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isPasswordRepeatVisible by remember { mutableStateOf(false) }

    var role by remember { mutableStateOf(user.role) }

    var tokens by remember { mutableStateOf(user.tokens) }
    var newTokens by remember { mutableStateOf(listOf<TokenInsert>()) }

    var twoFA by remember { mutableStateOf(user.`2faEnabled`) }
    var twoFASecret by remember { mutableStateOf(user.`2faSecret` ?: "") }

    var createdAt by remember { mutableStateOf(user.createdAt) }
    var updatedAt by remember { mutableStateOf(user.updatedAt) }

    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    // Update internal state when the user parameter changes
    LaunchedEffect(user) {
        username = user.username
        email = user.email
        role = user.role
        tokens = user.tokens
        twoFA = user.`2faEnabled`
        twoFASecret = user.`2faSecret` ?: ""
        createdAt = user.createdAt
        updatedAt = user.updatedAt
    }

    val onUpdateUserSuccess: (User) -> Unit = { updatedUser ->
        // Update the user within the ManageUsers composable
        onUpdateUser(updatedUser)

        newTokens = emptyList()
        editMode = false
    }

    val coroutineScope = rememberCoroutineScope()

    Surface(
        modifier = modifier.padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        elevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.BottomEnd
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                if (editMode) {
                    // Input fields for editing
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        modifier = Modifier
                            .fillMaxWidth(),
                        singleLine = true
                    )
                    //Text("Username: ${user.username}", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier
                            .fillMaxWidth(),
                        singleLine = true
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

                    //Text("Email: ${user.email}")
                    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Role:", style = MaterialTheme.typography.body1)
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
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("2FA Enabled:")
                            RadioButton(
                                selected = twoFA,
                                onClick = { twoFA = true }
                            )
                            Text("Yes", modifier = Modifier.padding(start = 8.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            RadioButton(
                                selected = !twoFA,
                                onClick = { twoFA = false }
                            )
                            Text("No", modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                    if (twoFA) {
                        OutlinedTextField(
                            value = twoFASecret,
                            onValueChange = { twoFASecret = it },
                            label = { Text("2FA Secret") },
                            modifier = Modifier
                                .fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    //Text("2FA Enabled: ${if (user.`2faEnabled`) "Yes" else "No"}")
                    if (tokens != null) {
                        Column {
                            tokens!!.forEachIndexed { index, token ->

                                var year by remember { mutableStateOf("${token.expiresOn?.year}") }
                                var month by remember { mutableStateOf("${token.expiresOn?.monthValue}") }
                                var day by remember { mutableStateOf("${token.expiresOn?.dayOfMonth}") }

                                var hour by remember { mutableStateOf("${token.expiresOn?.hour}") }
                                var minute by remember { mutableStateOf("${token.expiresOn?.minute}") }
                                var second by remember { mutableStateOf("${token.expiresOn?.second}") }

                                var dateTimeError by remember { mutableStateOf(false) }

                                Text("Token ${index + 1}:", fontWeight = FontWeight.Bold)
                                OutlinedTextField(
                                    value = token.token,
                                    // onValueChange = { tokens!![index].token = it },
                                    onValueChange = { newTokenValue ->
                                        tokens = tokens!!.toMutableList().apply { this[index] = token.copy(token = newTokenValue) }
                                    },
                                    label = { Text("Token Value") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                )
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                ) {
                                    Text("Token Type:", style = MaterialTheme.typography.body1)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = token.type == "all",
                                            onClick = {
                                                tokens = tokens!!.toMutableList().apply {
                                                    this[index] = token.copy(type = "all")
                                                }
                                            }
                                        )
                                        Text("All", modifier = Modifier.padding(start = 8.dp))
                                        Spacer(modifier = Modifier.width(16.dp))
                                        RadioButton(
                                            selected = token.type == "login",
                                            onClick = {
                                                tokens = tokens!!.toMutableList().apply {
                                                    this[index] = token.copy(type = "login")
                                                }
                                            }
                                        )
                                        Text("Login", modifier = Modifier.padding(start = 8.dp))
                                    }
                                }
                                /*OutlinedTextField(
                                    value = token.type,
                                    // onValueChange = { tokens!![index].type = it },
                                    onValueChange = { newTypeValue ->
                                        tokens = tokens!!.toMutableList().apply { this[index] = token.copy(type = newTypeValue) }
                                    },
                                    label = { Text("Token Type [ all / login ]") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                )*/
                                Text("Token Expires On")
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = year,
                                        // onValueChange = { year = it.take(4) }, // Limit input to 4 characters
                                        onValueChange = { newYearString ->
                                            val newYear = newYearString.take(4).toIntOrNull() ?: token.expiresOn?.year
                                            val newDateTime: LocalDateTime
                                            // newDateTime = token.expiresOn.withMonth(newMonth)

                                            try {
                                                if (newYear == null || month.toIntOrNull() == null || day.toIntOrNull() == null || hour.toIntOrNull() == null || minute.toIntOrNull() == null || second.toIntOrNull() == null) {
                                                    throw DateTimeException("Empty Values")
                                                }
                                                newDateTime = LocalDateTime.of(newYear, month.toInt(), day.toInt(), hour.toInt(), minute.toInt(), second.toInt())
                                                year = newYearString.take(4) // Limit input to 4 characters
                                                tokens = tokens!!.toMutableList().apply {
                                                    this[index] = token.copy(expiresOn = newDateTime)
                                                }
                                                dateTimeError = false
                                                // println(tokens!![index].expiresOn.toString())
                                            } catch (e: DateTimeException) {
                                                year = newYearString.take(4)
                                                tokens = tokens!!.toMutableList().apply {
                                                    this[index] = token.copy(expiresOn = null)
                                                }
                                                dateTimeError = true
                                            }
                                        },
                                        isError = dateTimeError,
                                        label = { Text("YYYY") },
                                        modifier = Modifier.weight(1f),
                                    )
                                    Text("-", modifier = Modifier.align(Alignment.CenterVertically))
                                    OutlinedTextField(
                                        value = month,
                                        onValueChange = { newMonthString ->
                                            val newMonth = newMonthString.take(2).toIntOrNull() ?: token.expiresOn?.monthValue
                                            val newDateTime: LocalDateTime
                                            try {
                                                if (year.toIntOrNull() == null || newMonth == null || day.toIntOrNull() == null || hour.toIntOrNull() == null || minute.toIntOrNull() == null || second.toIntOrNull() == null) {
                                                    throw DateTimeException("Empty Values")
                                                }
                                                newDateTime = LocalDateTime.of(year.toInt(), newMonth, day.toInt(), hour.toInt(), minute.toInt(), second.toInt())
                                                month = newMonthString.take(2) // Limit input to 2 characters
                                                tokens = tokens!!.toMutableList().apply {
                                                    this[index] = token.copy(expiresOn = newDateTime)
                                                }
                                                dateTimeError = false
                                            } catch (e: DateTimeException) {
                                                //month = newMonthString.take(2)
                                                month = newMonthString.take(2)
                                                dateTimeError = true
                                            }
                                        },
                                        isError = dateTimeError,
                                        label = { Text("MM") },
                                        modifier = Modifier.weight(1f),
                                    )
                                    Text("-", modifier = Modifier.align(Alignment.CenterVertically))
                                    OutlinedTextField(
                                        value = day,
                                        onValueChange = { newDayString ->
                                            val newDay = newDayString.take(2).toIntOrNull() ?: token.expiresOn?.dayOfMonth
                                            val newDateTime: LocalDateTime
                                            try {
                                                if (year.toIntOrNull() == null || month.toIntOrNull() == null || newDay == null || hour.toIntOrNull() == null || minute.toIntOrNull() == null || second.toIntOrNull() == null) {
                                                    throw DateTimeException("Empty Values")
                                                }
                                                newDateTime = LocalDateTime.of(year.toInt(), month.toInt(), newDay, hour.toInt(), minute.toInt(), second.toInt())
                                                day = newDayString.take(2) // Limit input to 2 characters
                                                tokens = tokens!!.toMutableList().apply {
                                                    this[index] = token.copy(expiresOn = newDateTime)
                                                }
                                                dateTimeError = false
                                            } catch (e: DateTimeException) {
                                                day = newDayString.take(2)
                                                dateTimeError = true
                                            }
                                        },
                                        isError = dateTimeError,
                                        label = { Text("DD") },
                                        modifier = Modifier.weight(1f),
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    OutlinedTextField(
                                        value = hour,
                                        onValueChange = { newHourString ->
                                            val newHour = newHourString.take(2).toIntOrNull() ?: token.expiresOn?.hour
                                            val newDateTime: LocalDateTime
                                            try {
                                                if (year.toIntOrNull() == null || month.toIntOrNull() == null || day.toIntOrNull() == null || newHour == null || minute.toIntOrNull() == null || second.toIntOrNull() == null) {
                                                    throw DateTimeException("Empty Values")
                                                }
                                                newDateTime = LocalDateTime.of(year.toInt(), month.toInt(), day.toInt(), newHour, minute.toInt(), second.toInt())
                                                hour = newHourString.take(2) // Limit input to 2 characters
                                                tokens = tokens!!.toMutableList().apply {
                                                    this[index] = token.copy(expiresOn = newDateTime)
                                                }
                                                dateTimeError = false
                                            } catch (e: DateTimeException) {
                                                hour = newHourString.take(2)
                                                dateTimeError = true
                                            }
                                        },
                                        isError = dateTimeError,
                                        label = { Text("HH") },
                                        modifier = Modifier.weight(1f),
                                    )
                                    Text(":", modifier = Modifier.align(Alignment.CenterVertically))
                                    OutlinedTextField(
                                        value = minute,
                                        onValueChange = { newMinuteString ->
                                            val newMinute = newMinuteString.take(2).toIntOrNull() ?: token.expiresOn?.minute
                                            val newDateTime: LocalDateTime
                                            try {
                                                if (year.toIntOrNull() == null || month.toIntOrNull() == null || day.toIntOrNull() == null || hour.toIntOrNull() == null || newMinute == null || second.toIntOrNull() == null) {
                                                    throw DateTimeException("Empty Values")
                                                }
                                                newDateTime = LocalDateTime.of(year.toInt(), month.toInt(), day.toInt(), hour.toInt(), newMinute, second.toInt())
                                                minute = newMinuteString.take(2) // Limit input to 2 characters
                                                tokens = tokens!!.toMutableList().apply {
                                                    this[index] = token.copy(expiresOn = newDateTime)
                                                }
                                                dateTimeError = false
                                            } catch (e: DateTimeException) {
                                                minute = newMinuteString.take(2)
                                                dateTimeError = true
                                            }
                                        },
                                        isError = dateTimeError,
                                        label = { Text("MM") },
                                        modifier = Modifier.weight(1f),
                                    )
                                    Text(":", modifier = Modifier.align(Alignment.CenterVertically))
                                    OutlinedTextField(
                                        value = second,
                                        onValueChange = { newSecondString ->
                                            val newSecond = newSecondString.take(2).toIntOrNull() ?: token.expiresOn?.minute
                                            val newDateTime: LocalDateTime
                                            try {
                                                if (year.toIntOrNull() == null || month.toIntOrNull() == null || day.toIntOrNull() == null || hour.toIntOrNull() == null || minute.toIntOrNull() == null || newSecond == null) {
                                                    throw DateTimeException("Empty Values")
                                                }
                                                newDateTime = LocalDateTime.of(year.toInt(), month.toInt(), day.toInt(), hour.toInt(), minute.toInt(), newSecond)
                                                second = newSecondString.take(2) // Limit input to 2 characters
                                                tokens = tokens!!.toMutableList().apply {
                                                    this[index] = token.copy(expiresOn = newDateTime)
                                                }
                                                dateTimeError = false
                                            } catch (e: DateTimeException) {
                                                second = newSecondString.take(2)
                                                dateTimeError = true
                                            }
                                        },
                                        isError = dateTimeError,
                                        label = { Text("SS") },
                                        modifier = Modifier.weight(1f),
                                    )
                                }

                                /*OutlinedTextField(
                                    value = formatter.format(token.expiresOn),
                                    //value = token.expiresOn, // Assuming token is a String
                                    //onValueChange = { token.expiresOn = LocalDateTime.parse(it) },
                                    onValueChange = { newExpiryDate ->
                                        tokens = tokens!!.toMutableList().apply { this[index] = token.copy(expiresOn = LocalDateTime.parse(newExpiryDate)) }
                                    },
                                    //onValueChange = { tokens!![index].expiresOn = it }, // Update token in the list
                                    label = { Text("Token Expires On") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                )*/
                            }
                        }
                    }

                    TokensInput(
                        newTokens = newTokens,
                        onAddToken = { newTokens = newTokens + TokenInsert() },
                        onUpdateToken = { index, updatedToken ->
                            newTokens = newTokens.toMutableList().apply {
                                this[index] = updatedToken
                            }
                        }
                    )

                    // Text("Role: ${user.role}")
                    // Text("Active Login Tokens: ${user.tokens?.size}")

                    Text("Created On: ${user.createdAt.format(formatter)}")
                    Text("Last Updated On: ${user.updatedAt.format(formatter)}")
                } else {
                    // Text fields in read-only mode
                    // Text("ID: ${user.id}", fontWeight = FontWeight.Bold)
                    Text("Username: $username", fontWeight = FontWeight.Bold)
                    Text("Email: $email")
                    Text("Role: $role")
                    Text("Active Login Tokens: ${tokens?.size}")
                    Text("2FA Enabled: ${if (twoFA) "Yes" else "No"}")
                    Text("User Created On: ${user.createdAt.plusHours(2).format(formatter)}", fontSize = 12.sp)
                    Text("User Last Updated On: ${user.updatedAt.plusHours(2).format(formatter)}", fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = {
                        if (editMode) {
                            coroutineScope.launch {
                                val feedback = updateUserInDB(
                                    user = user,
                                    username = username,
                                    email = email,
                                    password = password,
                                    passwordRepeat = passwordRepeat,
                                    role = role,
                                    tokens = tokens,
                                    newTokens = newTokens,
                                    twoFA = twoFA,
                                    twoFASecret = twoFASecret,
                                    onSuccess = onUpdateUserSuccess
                                )

                                feedbackMessage = feedback
                            }
                        } else {
                            editMode = true
                        }
                    },
                ) {
                    if (editMode) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    } else {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                }

                if (editMode) {
                    IconButton(
                        onClick = {
                            // Reset input fields to initial values
                            username = user.username
                            email = user.email
                            role = user.role
                            tokens = user.tokens
                            twoFA = user.`2faEnabled`
                            twoFASecret = user.`2faSecret` ?: ""
                            createdAt = user.createdAt
                            updatedAt = user.updatedAt
                            newTokens = emptyList()
                            editMode = false
                        }
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = "Cancel")
                    }
                }

                if (!editMode) {
                    IconButton(
                        onClick = {
                            // Call onDeleteUser to initiate deletion
                            onDeleteUser(user)
                        }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }

                /*if (!editMode) {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                val feedback = deleteUserFromDB(
                                    id = user.id,
                                    onSuccess = onDeleteUserSuccess
                                )
                                feedbackMessage = feedback
                            }

                            // editMode = false
                        }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }*/
            }
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

suspend fun deleteUserFromDB(
    id: String,
    onSuccess: () -> Unit
): String {

    return try {
        coroutineScope {
            deleteUser(id)
        }
        onSuccess()
        "User successfully deleted from the database."
    } catch (e: Exception) {
        "Error removing user from the database. ${e.message}"
    }
}

suspend fun updateUserInDB(
    user: User,
    username: String,
    email: String,
    password: String,
    passwordRepeat: String,
    role: String,
    tokens: List<Token>?,
    newTokens: List<TokenInsert>?,
    twoFA: Boolean,
    twoFASecret: String,
    onSuccess: (User) -> Unit
): String {

    if (username.isEmpty() ||
        email.isEmpty() ||
        role.isEmpty()
    ) {
        return ("Please fill in all fields.")
    }

    if (twoFA && twoFASecret.isEmpty()) {
        return ("Please provide 2FA Secret.")
    }

    if (password != passwordRepeat) {
        return ("Passwords do not match.")
    }

    // Exclude tokens with empty token value or invalid expiresOn DateTime
    val filteredTokens = tokens?.filter { it.token.isNotEmpty() && it.expiresOn != null }
    // val filteredTokens = tokens?.filter { it.token.isNotEmpty() }
    // tokens = tokens.filter { it.token.isNotEmpty() }

    // Exclude new tokens with empty token value or invalid expiresOn DateTime
    val filteredNewTokens = newTokens?.filter { it.token.isNotEmpty() && it.expiresOn != null }

    // Check if type chosen for all tokens
    if (newTokens != null && newTokens.any { it.type.isEmpty() }) {
        return "Choose type for all new tokens."
    }

    val userUpdate = UserUpdate(
        id = user.id,
        username = username,
        email = email,
        password = password,
        tokens = filteredTokens,
        newTokens = filteredNewTokens,
        `2faEnabled` = twoFA,
        `2faSecret` = twoFASecret,
        role = role
    )

    return try {
        var updatedUser: User
        coroutineScope {
            updatedUser = updateUser(userUpdate)
        }
        onSuccess(updatedUser)
        "User successfully updated in the database."
    } catch (e: Exception) {
        "Error updating user in the database. ${e.message}"
    }
}

@Composable
fun TokensInput(
    newTokens: List<TokenInsert>,
    onAddToken: () -> Unit,
    onUpdateToken: (Int, TokenInsert) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        // Text("Tokens", style = MaterialTheme.typography.h6)

        newTokens.forEachIndexed { index, token ->
            TokenInput(
                token = token,
                onTokenChange = { updatedToken -> onUpdateToken(index, updatedToken) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = { onAddToken() },
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp)
        ) {
            Text("Add New Token")
        }
    }
}

@Composable
fun TokenInput(
    token: TokenInsert,
    onTokenChange: (TokenInsert) -> Unit
) {
    var type by remember { mutableStateOf(token.type) }
    var value by remember { mutableStateOf(token.token) }
    var year by remember { mutableStateOf("${token.expiresOn?.year}") }
    var month by remember { mutableStateOf("${token.expiresOn?.monthValue}") }
    var day by remember { mutableStateOf("${token.expiresOn?.dayOfMonth}") }
    var hour by remember { mutableStateOf("${token.expiresOn?.hour}") }
    var minute by remember { mutableStateOf("${token.expiresOn?.minute}") }
    var second by remember { mutableStateOf("${token.expiresOn?.second}") }

    var dateTimeError by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                value = it
                onTokenChange(token.copy(token = it))
            },
            label = { Text("Token Value") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = type == "all",
                onClick = {
                    type = "all"
                    //onTokenChange(token.copy(type = type))
                    token.type = type
                    //token = token.copy(type = type)
                }
            )
            Text("All", modifier = Modifier.padding(start = 8.dp))
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(
                selected = type == "login",
                onClick = {
                    type = "login"
                    token.type = type
                    //onTokenChange(token.copy(type = type))
                }
            )
            Text("Login", modifier = Modifier.padding(start = 8.dp))
        }

        Text("Expiration date")
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = year,
                onValueChange = { newYearString ->
                    val newYear = newYearString.take(4).toIntOrNull() ?: token.expiresOn?.year
                    val newDateTime: LocalDateTime

                    try {
                        if (newYear == null || newYearString.toIntOrNull() == null || month.toIntOrNull() == null || day.toIntOrNull() == null || hour.toIntOrNull() == null || minute.toIntOrNull() == null || second.toIntOrNull() == null) {
                            throw DateTimeException("Empty Values")
                        }
                        newDateTime = LocalDateTime.of(newYear, month.toInt(), day.toInt(), hour.toInt(), minute.toInt(), second.toInt())
                        year = newYearString.take(4) // Limit input to 4 characters
                        onTokenChange(token.copy(expiresOn = newDateTime))
                        dateTimeError = false
                    } catch (e: DateTimeException) {
                        year = newYearString.take(4)
                        onTokenChange(token.copy(expiresOn = null))
                        dateTimeError = true
                    }
                },
                /*onValueChange = {
                    year = it.take(4)
                    try {
                        onTokenChange(token.copy(expiresOn = token.expiresOn.withYear(it.toIntOrNull() ?: token.expiresOn.year)))
                        dateTimeError = false
                    } catch (e: DateTimeException) {
                        dateTimeError = true
                    }
                },*/
                /*onValueChange = {
                    // year = it.take(4)
                    // onTokenChange(token.copy(expiresOn = token.expiresOn.withYear(it.toIntOrNull() ?: token.expiresOn.year)))
                    val newYearString = it.take(4)
                    val newYear = newYearString.toIntOrNull() ?: token.expiresOn.year

                    if (newYear in 1900..2100) {
                        val newDateTime = token.expiresOn.withYear(newYear)
                        year = newYearString
                        onTokenChange(token.copy(expiresOn = newDateTime))
                        dateTimeError = false
                    } else {
                        year = newYearString
                        dateTimeError = true
                    }
                },*/
                isError = dateTimeError,
                label = { Text("YYYY") },
                modifier = Modifier.weight(1f),
            )
            Text("-", modifier = Modifier.align(Alignment.CenterVertically))
            OutlinedTextField(
                value = month,
                onValueChange = { newMonthString ->
                    val newMonth = newMonthString.take(2).toIntOrNull() ?: token.expiresOn?.monthValue
                    val newDateTime: LocalDateTime

                    try {
                        if (year.toIntOrNull() == null || newMonth == null || newMonthString.toIntOrNull() == null || day.toIntOrNull() == null || hour.toIntOrNull() == null || minute.toIntOrNull() == null || second.toIntOrNull() == null) {
                            throw DateTimeException("Empty Values")
                        }
                        newDateTime = LocalDateTime.of(year.toInt(), newMonth, day.toInt(), hour.toInt(), minute.toInt(), second.toInt())
                        month = newMonthString.take(2)
                        onTokenChange(token.copy(expiresOn = newDateTime))
                        dateTimeError = false
                    } catch (e: DateTimeException) {
                        month = newMonthString.take(2)
                        onTokenChange(token.copy(expiresOn = null))
                        dateTimeError = true
                    }
                },
                isError = dateTimeError,
                label = { Text("MM") },
                modifier = Modifier.weight(1f),
            )
            Text("-", modifier = Modifier.align(Alignment.CenterVertically))
            OutlinedTextField(
                value = day,
                onValueChange = { newDayString ->
                    val newDay = newDayString.take(2).toIntOrNull() ?: token.expiresOn?.monthValue
                    val newDateTime: LocalDateTime

                    try {
                        if (year.toIntOrNull() == null || month.toIntOrNull() == null || newDay == null || newDayString.toIntOrNull() == null || hour.toIntOrNull() == null || minute.toIntOrNull() == null || second.toIntOrNull() == null) {
                            throw DateTimeException("Empty Values")
                        }
                        newDateTime = LocalDateTime.of(year.toInt(), month.toInt(), newDay, hour.toInt(), minute.toInt(), second.toInt())
                        day = newDayString.take(2)
                        onTokenChange(token.copy(expiresOn = newDateTime))
                        dateTimeError = false
                    } catch (e: DateTimeException) {
                        day = newDayString.take(2)
                        onTokenChange(token.copy(expiresOn = null))
                        dateTimeError = true
                    }
                },
                isError = dateTimeError,
                label = { Text("DD") },
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = hour,
                onValueChange = { newHourString ->
                    val newHour = newHourString.take(2).toIntOrNull() ?: token.expiresOn?.monthValue
                    val newDateTime: LocalDateTime

                    try {
                        if (year.toIntOrNull() == null || month.toIntOrNull() == null || day.toIntOrNull() == null || newHour == null || newHourString.toIntOrNull() == null || minute.toIntOrNull() == null || second.toIntOrNull() == null) {
                            throw DateTimeException("Empty Values")
                        }
                        newDateTime = LocalDateTime.of(year.toInt(), month.toInt(), day.toInt(), newHour, minute.toInt(), second.toInt())
                        hour = newHourString.take(2)
                        onTokenChange(token.copy(expiresOn = newDateTime))
                        dateTimeError = false
                    } catch (e: DateTimeException) {
                        hour = newHourString.take(2)
                        onTokenChange(token.copy(expiresOn = null))
                        dateTimeError = true
                    }
                },
                isError = dateTimeError,
                label = { Text("HH") },
                modifier = Modifier.weight(1f),
            )
            Text(":", modifier = Modifier.align(Alignment.CenterVertically))
            OutlinedTextField(
                value = minute,
                onValueChange = { newMinuteString ->
                    val newMinute = newMinuteString.take(2).toIntOrNull() ?: token.expiresOn?.monthValue
                    val newDateTime: LocalDateTime

                    try {
                        if (year.toIntOrNull() == null || month.toIntOrNull() == null || day.toIntOrNull() == null || hour.toIntOrNull() == null || newMinute == null || newMinuteString.toIntOrNull() == null || second.toIntOrNull() == null) {
                            throw DateTimeException("Empty Values")
                        }
                        newDateTime = LocalDateTime.of(year.toInt(), month.toInt(), day.toInt(), hour.toInt(), newMinute, second.toInt())
                        minute = newMinuteString.take(2)
                        onTokenChange(token.copy(expiresOn = newDateTime))
                        dateTimeError = false
                    } catch (e: DateTimeException) {
                        minute = newMinuteString.take(2)
                        onTokenChange(token.copy(expiresOn = null))
                        dateTimeError = true
                    }
                },
                isError = dateTimeError,
                label = { Text("MM") },
                modifier = Modifier.weight(1f),
            )
            Text(":", modifier = Modifier.align(Alignment.CenterVertically))
            OutlinedTextField(
                value = second,
                onValueChange = { newSecondString ->
                    val newSecond = newSecondString.take(2).toIntOrNull() ?: token.expiresOn?.monthValue
                    val newDateTime: LocalDateTime

                    try {
                        if (year.toIntOrNull() == null || month.toIntOrNull() == null || day.toIntOrNull() == null || hour.toIntOrNull() == null || minute.toIntOrNull() == null || newSecond == null || newSecondString.toIntOrNull() == null) {
                            throw DateTimeException("Empty Values")
                        }
                        newDateTime = LocalDateTime.of(year.toInt(), month.toInt(), day.toInt(), hour.toInt(), minute.toInt(), newSecond)
                        second = newSecondString.take(2)
                        onTokenChange(token.copy(expiresOn = newDateTime))
                        dateTimeError = false
                    } catch (e: DateTimeException) {
                        second = newSecondString.take(2)
                        onTokenChange(token.copy(expiresOn = null))
                        dateTimeError = true
                    }
                },
                isError = dateTimeError,
                label = { Text("SS") },
                modifier = Modifier.weight(1f),
            )
        }
    }
}