package gui.scraper.process

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class SourceWebsite {
    Official,
    Vlaksi
}

fun getCurrentTime(): String {
    return DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now().atZone(ZoneId.of("Europe/Ljubljana")))
}

fun isStringJson(json: String?): Boolean {
    try {
        //Gson().getAdapter(JsonElement::class.java).fromJson(json)
        //TODO: Fix this
        return true
    } catch (ex: Exception) {
        return false
    }
}