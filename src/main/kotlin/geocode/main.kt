package geocode

import Offer
import images.imageFile
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

suspend fun main() {
    val offers = File("data/raw/offers.jsonl").readLines().map {
        Json.decodeFromString<Offer>(it)
    }

    val countries = mutableSetOf<String>()
    val cities = mutableSetOf<String>()

    offers.forEach { offer ->
        offer.location?.let {
            val components = it.split(",")

            val location = Location()
            offer.locationData = location

            location.country = components.last().trim()
            location.city = components.getOrNull(components.lastIndex - 1)?.trim()
            location.cityPart = components.getOrNull(components.lastIndex - 2)?.trim()
            location.rest = components.dropLast(3)
        }

        val pictureUrl = offer.pictureUrl
        if (pictureUrl != null) {
            offer.pictureFile = imageFile(pictureUrl).path
        }
    }

    countries.sorted().forEach { println(it) }
    cities.sorted().forEach { println(it) }

    File("data/raw/offers-02-with-location.jsonl").writer().use {
        offers.forEach { offer ->
            it.appendLine(Json.encodeToString(offer))
        }
    }
}

@Serializable
class Location {
    var country: String? = null
    var city: String? = null
    var cityPart: String? = null
    var rest: List<String> = listOf()
}