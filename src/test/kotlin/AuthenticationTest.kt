package utils

import gui.login.loginUser
import gui.login.loginUserTwoFA
import gui.login.logoutUserBackend
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import utils.context.appContextGlobal
import utils.context.initializeAppContext
import java.io.File
import java.nio.ByteBuffer
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.pow
import kotlin.test.DefaultAsserter.assertTrue
import kotlin.test.Test

class AuthenticationTest {

    private val projectDirectory = System.getProperty("user.dir")
    private val configFile = File("$projectDirectory/src/test/config/config.env")
    private val configSecrets = dotenv {
        directory = configFile.parent
        filename = configFile.name
    }

    // Decode Base32 encoded string to byte array
    private fun base32Decode(secret: String): ByteArray {
        val base32Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        val bytes = mutableListOf<Byte>()
        var buffer = 0
        var bitsLeft = 0
        for (c in secret.uppercase(Locale.getDefault())) {
            if (c !in base32Chars) throw IllegalArgumentException("Invalid Base32 character: $c")
            buffer = (buffer shl 5) or base32Chars.indexOf(c)
            bitsLeft += 5
            if (bitsLeft >= 8) {
                bitsLeft -= 8
                bytes.add((buffer shr bitsLeft).toByte())
                buffer = buffer and ((1 shl bitsLeft) - 1)
            }
        }
        return bytes.toByteArray()
    }

    // Generate HMAC-SHA1 hash
    private fun hmacSha1(key: ByteArray, data: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA1")
        val keySpec = SecretKeySpec(key, "HmacSHA1")
        mac.init(keySpec)
        return mac.doFinal(data)
    }

    // Generate TOTP
    private fun generateTotp(secret: String, timeStep: Long = 30, digits: Int = 6): String {
        val key = base32Decode(secret)
        val time = System.currentTimeMillis() / 1000 / timeStep
        val data = ByteBuffer.allocate(8).putLong(time).array()
        val hash = hmacSha1(key, data)

        // Dynamic truncation
        val offset = hash[hash.size - 1].toInt() and 0x0F
        val truncatedHash = hash.copyOfRange(offset, offset + 4)
        val code = ByteBuffer.wrap(truncatedHash).int and 0x7FFFFFFF
        val otp = code % 10.0.pow(digits.toDouble()).toInt()

        // Zero-padding
        return otp.toString().padStart(digits, '0')
    }

    private fun onLoginSuccess(token: String, username: String) {
        println("onLoginSuccess() token: $token, username: $username")
        val currentContext = appContextGlobal.get()
        val updatedContext = currentContext.copy(token = token, username = username)
        appContextGlobal.set(updatedContext)
    }

    @Test
    fun testLoginUser() = runBlocking {
        initializeAppContext()

        val username = configSecrets["USERNAME_BASIC_LOGIN"] ?: error("Username not found in .env file")
        val password = configSecrets["PASSWORD_BASIC_LOGIN"] ?: error("Password not found in .env file")

        var loginSuccess = false
        var loginFailure = false

        loginUser(
            username,
            password,
            onSuccess = {
                token -> onLoginSuccess(token, username)
                loginSuccess = true
            },
            onTwoFA = {
                // Not testing TwoFA here
            },
            onFailure = {
                loginFailure = true
            }
        )

        assertTrue("Login should be successful", loginSuccess)
        assertTrue("Login should not fail", !loginFailure)
        logoutUser()
    }

    @Test
    fun testLoginUserTwoFA() = runTest {
        val username = configSecrets["USERNAME_TWOFA_LOGIN"] ?: error("Username not found in .env file")
        val password = configSecrets["PASSWORD_TWOFA_LOGIN"] ?: error("Password not found in .env file")
        val totpSecret = configSecrets["TWOFA_SECRET"] ?: error("TOTP secret not found in config.env file")
        val totp = generateTotp(totpSecret)
        // println(totp)

        var loginSuccess = false
        var loginFailure = false

        runBlocking {
            val job = launch {
                loginUser(
                    username,
                    password,
                    onSuccess = {
                        loginSuccess = true
                    },
                    onTwoFA = { loginToken ->
                        launch {
                            loginUserTwoFA(
                                loginToken,
                                totp,
                                onSuccess = { token ->
                                    onLoginSuccess(token, username)
                                    loginSuccess = true
                                },
                                onFailure = {
                                    loginFailure = true
                                }
                            )
                        }
                    },
                    onFailure = {
                        loginFailure = true
                    }
                )
            }
            job.join() // Wait
        }

        assertTrue("Login with 2FA should be successful", loginSuccess)
        assertTrue("Login with 2FA should not fail", !loginFailure)
        logoutUser()
    }

    private suspend fun logoutUser() {
        logoutUserBackend()
        val appContext = appContextGlobal.get()
        assertTrue("Token should be cleared after logout", appContext.token.isEmpty())
        assertTrue("Username should be cleared after logout", appContext.username.isEmpty())
    }
}