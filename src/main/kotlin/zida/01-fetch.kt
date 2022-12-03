package zida

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpStatusCode.Companion.OK
import kotlinx.coroutines.delay
import kotlin.io.path.Path
import kotlin.io.path.writeText
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

val urlBase =
    "https://www.4zida.rs/prodaja-kuca?jeftinije_od=60000eur" +
            "&stanje=uobicajeno&stanje=novo&stanje=renovirano" +
            "&tip_kuce=samostalna_kuca&tip_kuce=seoska_kuca&tip_kuce=imanje&tip_kuce=domacinstvo&tip_kuce=kuca_u_nizu"

// https://www.nekretnine.rs/stambeni-objekti/kuce/izdavanje-prodaja/prodaja/cena/1_60000
// /lista/po-stranici/10/stranica/2/

suspend fun main() {
    val client = HttpClient(CIO)

    // repeat 1..46

    val random = Random(4)

    for (i in 1..19) {
        val url = when (i) {
            1 -> urlBase
            else -> "${urlBase}&strana=$i"
        }

        println(url)
        val response = client.get(url)
        check(response.status == OK)
        Path("data/zida/pages/page-${i.toString().padStart(2, '0')}.html")
            .writeText(response.bodyAsText())

        val duration = (1000 + random.nextInt(1000)).milliseconds
        println("waiting $duration ms...")
        delay(duration)
    }
}