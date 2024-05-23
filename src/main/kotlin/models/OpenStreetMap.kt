package models

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class OpenStreetMap(val version: Double, val generator: String, val elements: List<Section>)

@Serializable
data class OpenStreetMapTagsNode(
    val name: String? = null,
    @SerialName("name:sl")
    val nameSl: String? = null,
    val public_transport: String? = null,
    val railway: String,
    val wikidata: String? = null,
    val wikipedia: String? = null
)

@Serializable
data class OpenStreetMapTagsWay(
    val building: String? = null,
    val name: String? = null,
    @SerialName("name:de")
    val nameDe: String? = null,
    @SerialName("name:ru")
    val nameRu: String? = null,
    @SerialName("name:sl")
    val nameSl: String? = null,
    @SerialName("old_name:de")
    val oldNameDe: String? = null,
    val public_transport: String? = null,
    val railway: String,
    val wheelchair: String? = null,
    val wikidata: String? = null,
    val wikipedia: String? = null
)


@Serializable
data class OpenStreetMapBounds(val minlat: Float, val minlon: Float, val maxlat: Float, val maxlon: Float)

@Serializable
data class OpenStreetMapCoordinates(val lat: Float, val lon: Float)

@Serializable
enum class OpenStreetMapType {
    @SerialName("node")
    NODE,

    @SerialName("way")
    WAY
}

@Serializable(with = SectionSerializer::class)
sealed class Section {
    // Enforcing that every subclass should have a variable called type.
    @SerialName("type")
    abstract val type: OpenStreetMapType
}

// Models to support different kinds of sections.
@Serializable
data class NodeSection(
    override val type: OpenStreetMapType = OpenStreetMapType.NODE,
    val id: Long,
    val lat: Float,
    val lon: Float,
    val tags: OpenStreetMapTagsNode
) : Section()

@Serializable
data class WaySection(
    override val type: OpenStreetMapType = OpenStreetMapType.WAY,
    val id: Long,
    val bounds: OpenStreetMapBounds,
    val nodes: List<Long>,
    val geometry: List<OpenStreetMapCoordinates>,
    val tags: OpenStreetMapTagsWay
) : Section()

object SectionSerializer :
    JsonContentPolymorphicSerializer<Section>(
        Section::class
    ) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<Section> {
        return when (element.jsonObject["type"]?.jsonPrimitive?.content) {
            "node" -> NodeSection.serializer()
            "way" -> WaySection.serializer()
            else -> throw Exception("ERROR: No Serializer found. Serialization failed.")
        }
    }
}




