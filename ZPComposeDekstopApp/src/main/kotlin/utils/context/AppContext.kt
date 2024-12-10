package utils.context

import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.atomic.AtomicReference

data class AppContext(
    val url: String,
    var username: String,
    var token: String, // update after successful login
    val officialUrl: String,
    val vlakSiUrl: String,
    val requestDetailReport: Boolean,
    val dbUri: String,
    val blacklist: List<String>
)

// Lazy initialization: appContextGlobal will be initialized only once, on its first usage.
/*val appContextGlobal: AppContext by lazy {
    runBlocking {
        readConfiguration() ?: throw IllegalStateException("Failed to initialize AppContext")
    }
}*/

// Initialize appContextGlobal with AtomicReference
val appContextGlobal = AtomicReference<AppContext>()

fun initializeAppContext() {
    runBlocking {
        val config = readConfiguration() ?: throw IllegalStateException("Failed to initialize AppContext")
        appContextGlobal.set(config)
    }
}

private suspend fun readConfiguration(): AppContext? = withContext(Dispatchers.IO) {
    try {
        val projectDirectory = System.getProperty("user.dir")
        val configFile = File("$projectDirectory/config/config.env")

        if (!configFile.exists()) {
            throw IllegalArgumentException("Config file not found.")
        }

        val configSecrets = dotenv {
            directory = configFile.parent
            filename = configFile.name
        }

        val url: String = configSecrets["API_BASE_URL"] ?: throw IllegalStateException("API_BASE_URL not found in configuration")
        // val token: String = configSecrets["API_BASE_TOKEN"] ?: throw IllegalStateException("API_BASE_TOKEN not found in configuration")
        val officialUrl: String = configSecrets["OFFICIAL_URL"] ?: throw IllegalStateException("OFFICIAL_URL not found in configuration")
        val vlakSiUrl: String = configSecrets["VLAKSI_URL"] ?: throw IllegalStateException("VLAKSI_URL not found in configuration")
        val requestDetailReport: String = configSecrets["REQUEST_DETAIL_REPORT"] ?: throw IllegalStateException("REQUEST_DETAIL_REPORT not found in configuration")
        val dbUri: String = configSecrets["DBURI"] ?: throw IllegalStateException("DBURI not found in configuration")
        val blacklist: String = configSecrets["BLACKLIST"] ?: throw IllegalStateException("BLACKLIST not found in configuration")

        val requestDetailReportBoolean = requestDetailReport == "true"
        val regexURL = Regex("https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)")
        if (!regexURL.matches(officialUrl)) {
            throw IllegalStateException("OFFICIAL_URL malformed in configuration")
        }

        if (!regexURL.matches(vlakSiUrl)) {
            throw IllegalStateException("VLAKSI_URL malformed in configuration")
        }

        val blacklistList = blacklist.split(',')

        AppContext(
            url = url,
            username = "",
            token = "",
            vlakSiUrl = vlakSiUrl,
            officialUrl = officialUrl,
            requestDetailReport = requestDetailReportBoolean,
            dbUri = dbUri,
            blacklist = blacklistList
        )
    } catch (e: Exception) {
        println("Error reading database configuration: ${e.message}")
        null
    }
}