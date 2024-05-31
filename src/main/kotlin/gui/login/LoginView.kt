package gui.login

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import utils.context.appContextGlobal

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var twoFATOTP by remember { mutableStateOf("") }
    var twoFATokenLogin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var enterTwoFATOTP by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    fun onLoginSuccess(token: String) {
        errorMessage = ""
        val currentContext = appContextGlobal.get()
        val updatedContext = currentContext.copy(token = token)
        appContextGlobal.set(updatedContext)
        onLoginSuccess()
    }

    fun onFailureLogin(message: String) {
        errorMessage = message
    }

    fun onTwoFALogin(tokenLogin: String) {
        errorMessage = ""
        enterTwoFATOTP = true
        twoFATokenLogin = tokenLogin

    }

    if (enterTwoFATOTP) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("BandOfBytes", style = MaterialTheme.typography.h4)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Two Factor Code", style = MaterialTheme.typography.h5)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = twoFATOTP,
                    onValueChange = { twoFATOTP = it },
                    label = { Text("TOTP") }
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    coroutineScope.launch {
                        loginUserTwoFA(
                            twoFATokenLogin,
                            twoFATOTP,
                            onSuccess = { token -> onLoginSuccess(token) },
                            onFailure = { message -> onFailureLogin(message) }
                        )
                    }
                }) {
                    Text("Submit")
                }

                Button(onClick = {
                    username = ""
                    password = ""
                    twoFATOTP = ""
                    twoFATokenLogin = ""
                    errorMessage = ""
                    enterTwoFATOTP = false
                }) {
                    Text("Cancel")
                }

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colors.error)
                }
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("BandOfBytes", style = MaterialTheme.typography.h4)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Login", style = MaterialTheme.typography.h5)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    coroutineScope.launch {
                        loginUser(
                            username,
                            password,
                            onSuccess = { token -> onLoginSuccess(token) },
                            onTwoFA = { tokenLogin -> onTwoFALogin(tokenLogin) },
                            onFailure = { message -> onFailureLogin(message) }
                        )
                    }
                }) {
                    Text("Login")
                }

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colors.error)
                }
            }
        }
    }
}