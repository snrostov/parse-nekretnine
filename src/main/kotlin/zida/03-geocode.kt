package zida

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.commons.csv.CSVFormat
import java.io.File

fun main() {
    write("cities")
    write("locations")
}

private fun write(s: String) {
    val data = mutableMapOf<String, Map<String, String>>()

    val default = CSVFormat.DEFAULT

    val csv = default.builder().setHeader().build()
        .parse(File("data/zida/$s-geocode.csv").readText().substring(1).reader())

    csv.records.forEach {
        data.put(it.get(0), it.toMap())
    }

    File("data/zida/$s.json").writeText(Json.encodeToString<Map<String, Map<String, String>>>(data))
}