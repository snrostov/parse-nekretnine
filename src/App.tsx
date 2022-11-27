import React, {useEffect, useState} from "react";

import L from 'leaflet';
import {Circle, CircleMarker, MapContainer, Marker, Popup, TileLayer} from 'react-leaflet'
import {City} from "./Geo";

// const localUrlPrefix = "http://localhost:8082/"
const publicUrlPrefix = "../"

function url(url) {
    // if (process.env.NODE_ENV !== 'production') {
    //     return localUrlPrefix + url
    // } else {
        return publicUrlPrefix + url
    // }
}

export function App() {
    const [data, setData] = useState<any[]>([])

    useEffect(() => {
        const controller = new AbortController()
        fetch(url("data/raw/offers-02-with-location.jsonl"), {signal: controller.signal})
            .then(r => r.text())
            .then(t => t.split("\n"))
            .then(t => {
                setData(t.map(i => {
                    try {
                        return JSON.parse(i)
                    } catch (e) {
                        return {"error": e}
                    }
                }))
            })
        return () => {
            controller.abort()
        }
    }, [""])

    const [geo, setGeo] = useState<any>([])

    useEffect(() => {
        const controller = new AbortController()
        fetch(url("data/geo.json"), {signal: controller.signal})
            .then(r => r.json())
            .then(r => setGeo(r))
        return () => {
            controller.abort()
        }
    }, [""])

    const cities = new Set<string>()
    for (let item of data) {
        cities.add(item.locationData?.city)
    }

    const [selectedCities, setSelectedCities] = useState<Set<string>>(new Set())

    function upd(e) {
        const c = new Set<string>()
        for (let selectedOption of e.target.selectedOptions) {
            c.add(selectedOption.innerText)
        }
        setSelectedCities(c)
    }


    let filteredItems: any[] = data
    if (selectedCities.size > 0) {
        filteredItems = []
        for (let item of data) {
            if (selectedCities.has(item.locationData.city)) {
                filteredItems.push(item)
            }
        }
    }

    console.log(geo)

    return <div>
        <select style={{position:"absolute"}} multiple size={45} onChange={upd}>
            {Array.from(cities).map(city => <option key={city || "no"}>{city}</option>)}
        </select>
        <div id={"map"}>
        <MapContainer center={[44.8178131, 20.4568974]}
                      zoom={8}
                      scrollWheelZoom={false}>
            <TileLayer
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            />
            {Object.values(geo).map((city: City) => <CircleMarker
                center={[city.lat, city.lon]}
                radius={5}
                pathOptions={{stroke: false, fillOpacity: 0.7, color: 'red'}}
                eventHandlers={{
                    click: () => {
                        setSelectedCities(new Set([city.original_address]))
                    }
                }}
            >
                <Popup>
                    {city.original_address}
                </Popup>
            </CircleMarker>)}

        </MapContainer>
        </div>

        <table>
            <thead>
            <tr>
                <th>#</th>
                <th>Image</th>
                <th>Price, EUR</th>
                <th>Square, m²</th>
                <th>
                    <div>City</div>

                </th>
                <th>Title</th>
            </tr>
            </thead>
            <tbody>
            {filteredItems.map((data, index) => {
                // console.log(data)
                return <tr key={index}>
                    <td>{index}</td>
                    <td><img width={100} height={100} src={url(data.pictureFile)}/></td>
                    <td style={{whiteSpace: "nowrap"}}>{data.price}</td>
                    <td>{data.square}</td>
                    <td>{data.locationData.city}</td>
                    <td>
                        <a href={"https://www.nekretnine.rs" + data.url}>
                            {data.title}
                        </a>
                    </td>
                </tr>
            })}
            </tbody>
        </table>
    </div>
}