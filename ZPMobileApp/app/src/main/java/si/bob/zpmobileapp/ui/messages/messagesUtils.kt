package si.bob.zpmobileapp.ui.messages

import org.json.JSONObject

enum class Category(val displayName: String) {
    MISCELLANEOUS("miscellaneous"),
    EXTREME("extreme");

    override fun toString(): String {
        return displayName
    }
}

data class Message(
    val postedByUser: String,
    val timeOfMessage: String,
    val message: String,
    val category: Category
) {
    companion object {
        fun fromJson(messageData: JSONObject): Message {
            val category = try {
                Category.valueOf(messageData.getString("category").uppercase()) // Converts string to enum
            } catch (e: IllegalArgumentException) {
                Category.MISCELLANEOUS // Default if the category is invalid
            }

            return Message(
                postedByUser = messageData.getString("postedByUser"),
                timeOfMessage = messageData.getString("timeOfMessage"),
                message = messageData.getString("message"),
                category = category
            )
        }
    }
}