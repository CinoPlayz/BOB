package utils.parsing

import java.net.URLDecoder
import java.nio.charset.Charset

fun String.unescapeUnicode() = replace("\\\\u([0-9A-Fa-f]{4})".toRegex()) {
    String(Character.toChars(it.groupValues[1].toInt(radix = 16)))
}

//Function to decode URL encoding (database enteries)
fun getDecodedData(string: String){
    val decode = URLDecoder.decode(string, Charset.defaultCharset())
}