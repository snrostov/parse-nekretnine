package images

import Offer
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

@OptIn(InternalAPI::class)
suspend fun main() {
    val offers = File("data/raw/offers.jsonl").readLines().map {
        Json.decodeFromString<Offer>(it)
    }

    val client = HttpClient(CIO)

    offers.forEachIndexed { index, it ->
        println("$index / ${offers.size}")
        val url = it.pictureUrl
        if (url != null) {
            val resp = client.get(url)
            if (resp.status == HttpStatusCode.OK) {
                // https://img.nekretnine.rs/foto/
                // MjU2eDI1Ni9jZW50ZXIvbWlkZGxlL2ZpbHRlcnM6Zm9ybWF0KHdlYnAp/
                // nek/
                // vT5PP23Bw_fss?st=wdbN8t8lk-DNn0N4IzLE8k4CSDn_QoWWxVmUHGYbLaY&ts=1653391189&e=0
                val file = imageFile(url)
                resp.bodyAsChannel().copyAndClose(file.writeChannel())
            }
        }
    }
}

private fun imageFile(url: String): File {
    val fileName = imgFileName(url)
    val file = File("data/raw/images/$fileName.webp")
    return file
}

private fun imgFileName(url: String) = url.replace(Regex("[^A-Za-z0-9-_.]"), "_")