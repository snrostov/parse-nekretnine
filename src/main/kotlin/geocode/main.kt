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

            location.city?.let { cities.add(it) }
        }

        val pictureUrl = offer.pictureUrl
        if (pictureUrl != null) {
            offer.pictureFile = imageFile(pictureUrl).path
        }

        var price = offer.price
        if (price != null) {
            price = price.replace(" ", "")
            offer.price = price.removeSuffix("EUR")
        }

        var square = offer.square
        if (square != null) {
            offer.square = square.removeSuffix(" m²")
        }
    }

    cities.sorted().forEach {
        println(it)
    }

    var first = true
    File("data/raw/offers-02-with-location.jsonl").writer().use {
        offers
            .sortedBy { it.price?.toIntOrNull() }
            .sortedBy { it.locationData?.city }
            .forEach { offer ->
                if (first) first = false
                else it.appendLine()
                it.append(Json.encodeToString(offer))
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