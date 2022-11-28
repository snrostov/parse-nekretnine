// noinspection SpellCheckingInspection

import {cache, detailsDirHandle, getCached} from "./cache";

export interface OfferDetails {
    url: string
    images: string[],
    location: string[],
    Opis: string,
    "Transakcija": string
    "Kategorija": string
    "Kvadratura": string
    "Uknjiženo": string
    "Stanje nekretnine": string
    "Ukupan broj soba": number
    "Broj kupatila": number
    "Spratnost": number
}

export const OfferDetailProps = [
    "Transakcija",
    "Kategorija",
    "Kvadratura",
    "Uknjiženo",
    "Stanje nekretnine",
    "Ukupan broj soba",
    "Broj kupatila",
    "Spratnost",
]

export function fetchDetails(url, id): Promise<OfferDetails> {
    return getCached(id, () => compute(url, id))
}

function compute(url, id): Promise<OfferDetails> {
    const data: OfferDetails = {
        "Broj kupatila": 0,
        Opis: "",
        "Stanje nekretnine": "",
        "Ukupan broj soba": 0,
        Kategorija: "",
        Kvadratura: "",
        Spratnost: 0,
        Transakcija: "",
        "Uknjiženo": "",
        url: url,
        images: [],
        location: []
    }
    const a = fetch(url)
        .then(response => response.text())
        .then(html => {
            const doc = new DOMParser().parseFromString(html, "text/html");
            doc.querySelectorAll("div.property__amenities li").forEach(e => {
                let keyElement = e.childNodes[0];
                let valueElement = e.querySelector("strong");

                if (keyElement && valueElement) {
                    let key = keyElement.textContent.trim()
                    key = key.substring(0, key.length - 1).trim() // remove ":"
                    const value = valueElement.innerText.trim()
                    if (key && value) {
                        data[key] = value
                    }
                }
            })
            const d = doc.querySelector("div.property__description")
            if (d) {
                data["Opis"] = d.textContent.replace("Opis", "").trim()
            }

            doc.querySelectorAll("div.property__location li").forEach(e => {
                data.location.push(e.textContent)
            })
        })
        .catch(function (err) {
            console.log('Failed to fetch page: ', err);
        });

    const b = fetch(url + "galerija")
        .then(response => response.text())
        .then(html => {
            const doc = new DOMParser().parseFromString(html, "text/html");
            doc.querySelectorAll("div.gallery-thumbs img").forEach(e => {
                const src = e.getAttribute("data-src")
                if (src) data.images.push(src)
            })
        })
        .catch(function (err) {
            console.log('Failed to fetch page: ', err);
        });

    return Promise.all([a, b]).then(() => data)
}
