import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.commons.csv.CSVFormat
import java.io.File

fun main() {
    val data = mutableMapOf<String, Map<String, String>>()

    val default = CSVFormat.DEFAULT

    val csv = default.builder().setHeader().build()
        .parse(File("data/geocoded_by_geoapify-27_11_2022, 14_07_38.csv").readText().substring(1).reader())

    csv.records.forEach {
        data.put(it.get(0), it.toMap())
    }

//    val lines = File("data/geocoded_by_geoapify-27_11_2022, 14_07_38.csv").readLines()
//    val fields = lines.first().substring(1).split(",").map { it.trim() }
//
//    lines.drop(1).forEach { item ->
//        data.add(buildMap {
//
//
//            item.split(",", limit = fields.size).forEachIndexed { index, s ->
//                put(fields[index], s.trim())
//            }
//        })
//    }

    File("data/geo.json").writeText(Json.encodeToString<Map<String, Map<String, String>>>(data))
}