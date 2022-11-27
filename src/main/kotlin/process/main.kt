package process

import Offer
import it.skrape.core.htmlDocument
import it.skrape.selects.html5.a
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

suspend fun main() {
    val offers = mutableListOf<Offer>()

    File("data/raw/pages").listFiles()?.sortedBy { it.name }?.forEach {
        if (it.isFile && it.name.endsWith("html")) {
            extractOffers(it, offers)
        }
    }

    File("data/raw/offers.jsonl").writer().use { w ->
        offers.forEach {
            w.appendLine(Json.encodeToString(it))
        }
    }
}

private fun extractOffers(it: File, offers: MutableList<Offer>) {
    htmlDocument(it) {
        "div.row.offer" {
            findAll {
                forEach {
                    val offer = Offer()
                    offers.add(offer)

                    // <span class="flag-bottom">AGENCIJA</span>

                    try {
                        it.findFirst("picture") {
                            findAll("source").firstOrNull()?.let {
                                offer.pictureUrl = it.attribute("data-srcset")
                            }
                        }
                    } catch (t: Throwable) {
                    }

                    // <picture class="advert-picture d-block placeholder-preview-box ratio-1-1">
                    //     <source media="(max-width: 1343px)" type="image/webp"
                    //             data-srcset="https://img.nekretnine.rs/foto/MjU2eDI1Ni9jZW50ZXIvbWlkZGxlL2ZpbHRlcnM6Zm9ybWF0KHdlYnAp/nek/C9IXOuBNM_fss?st=2tNhQoRtcAoWpKwJPSw2vgMovHfVOLckcJ2epNgOHy0&amp;ts=1669209513&amp;e=0">
                    //     <source media="(min-width: 1344px)" type="image/webp"
                    //             data-srcset="https://img.nekretnine.rs/foto/MTgxeDE4MS9jZW50ZXIvbWlkZGxlL2ZpbHRlcnM6Zm9ybWF0KHdlYnAp/nek/C9IXOuBNM_fss?st=x0vfJI0p2devsQzSmap6pXwPSSgzPugWlQpJaNqWNl4&amp;ts=1669209513&amp;e=0">
                    //     <img width="1" height="1" class="img-fluid mx-auto d-block lazyload"
                    //          data-src="https://img.nekretnine.rs/foto/MTgxeDE4MS9jZW50ZXIvbWlkZGxlL2ZpbHRlcnM6cXVhbGl0eSg4MCk=/nek/C9IXOuBNM_fss?st=nKwlQHKwNmxip-5VzBp7owm4h48PE81U9gW-XT78BBQ&amp;ts=1669209513&amp;e=0"
                    //          alt="Idvor kuća na prodaju" title="Idvor kuća na prodaju | nekretnine.rs">
                    // </picture>

                    it.findFirst("h2.offer-title") {
                        // <h2 class="offer-title text-truncate w-100">
                        //     <a href="/stambeni-objekti/kuce/idvor-kuca-na-prodaju/Nkkt313lqQX/">
                        //         Idvor kuća na prodaju
                        //     </a>
                        // </h2>

                        findFirst {
                            a {
                                findFirst {
                                    val href = attribute("href")
                                    offer.url = href
                                    offer.id = href.removeSuffix("/").substringAfterLast("/")
                                    offer.title = text
                                }
                            }
                        }
                    }


                    // <div class="mt-1 mt-lg-2 mb-lg-0 d-md-block offer-meta-info offer-adress">
                    //            23.11.2022  |  Prodaja  |  Porodična kuća
                    //        </div>

                    it.findFirst("p.offer-location") {
                        // <p class="offer-location text-truncate">
                        //            Kovačica, Srbija
                        //        </p>

                        offer.location = this.text
                    }

                    it.findFirst("p.offer-price") {
                        findFirst("span") {
                            offer.price = text
                        }
                    }

                    it.findSecond("p.offer-price") {
                        findFirst("span") {
                            offer.square = text
                        }
                    }

                    // <p class="d-none d-md-block offer-price-label">Cena</p>
                    //                                    <p class="offer-price">
                    //                        <span>22 000 EUR</span>
                    //                        <small class="custom-offer-style">229 €/m²</small>
                    //                    </p>

                    // <div class="text-right">
                    //                    <p class="d-none d-md-block offer-price-label">Kvadratura</p>
                    //                    <p class="offer-price offer-price--invert">
                    //                        <span>96 m²</span>
                    //                    </p>
                    //                </div>
                }
            }
        }
    }
}