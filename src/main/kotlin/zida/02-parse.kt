package zida

import Offer
import geocode.Location
import it.skrape.core.htmlDocument
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

val cities = mutableSetOf<String>()
val locations = mutableSetOf<String>()
suspend fun main() {
    val offers = mutableListOf<Offer>()

    File("data/zida/pages").listFiles()?.sortedBy { it.name }?.forEach {
        if (it.isFile && it.name.endsWith("html")) {
            extractOffers(it, offers)
        }
    }

    File("data/zida/offers.jsonl").writer().use { w ->
        offers.forEach {
            w.appendLine(Json.encodeToString(it))
        }
    }

    saveSet("cities", cities)
    saveSet("locations", locations)
}

private fun saveSet(s: String, strings: MutableSet<String>) {
    File("data/zida/$s.txt").writer().use {
        strings.sorted().forEach { c ->
            it.appendLine(c)
        }
    }
}

private fun extractOffers(it: File, offers: MutableList<Offer>) {
    htmlDocument(it) {
        this.findAll("div.ed-card").forEach {
            val offer = Offer()

            try {
                it.findFirst("img") {
                    offer.pictureUrl = attribute("src")
                }

                it.findFirst("div.ed-card-details") {
                    findFirst("a") {
                        offer.url = this.attribute("href")
                        offer.id = offer.url!!.substringAfterLast("/")
                        offer.price = this.findFirst("div").text
                        offer.price = offer.price!!
                            .replace(".", "")
                            .replace("€", "")
                            .trim()


                        try {
                            val tags = this.findAll("strong")
                            offer.square = tags.getOrNull(0)?.text?.replace("m²", "")?.trim()
                            offer.rooms = tags.getOrNull(1)?.text?.replace("sobe", "")?.trim()
                            offer.heating = tags.getOrNull(2)?.text
                        } catch (t: Throwable) {
                        }

                        this.findFirst("h2") {
                            offer.location = findAll("span")
                                .map { it.text }
                                .joinToString(", ")

                            val c = offer.location!!.split(",")
                                .map { it.trim() }
                                .filter { it !in ignorLocs }
                                .reversed()
                            offer.location = c.joinToString(", ")

                            val location = Location()
                            location.city = c.firstOrNull()
                            location.rest = c.drop(1)
                            offer.locationData = location

                            location.city?.let { cities.add(it) }
                            offer.location?.let { locations.add(it) }

                            Unit
                        }

                    }

                    offer.description = it.findFirst("h3").text
                }

                offers.add(offer)
            } catch (t: Throwable) {

            }
        }
    }
}

val ignorLocs = mutableSetOf("Gradske lokacije", "Okolne lokacije")