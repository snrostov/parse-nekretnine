import React, {useEffect, useRef, useState} from "react";
import {CircleMarker, MapContainer, TileLayer, Tooltip} from 'react-leaflet'
import {City} from "./Geo";
import {fetchDetails, OfferDetailProps, OfferDetails} from "./details";

const localUrlPrefix = "http://localhost:8082/"
const publicUrlPrefix = "../"

function url(url) {
    if (process.env.NODE_ENV !== 'production') {
        return localUrlPrefix + url
    } else {
        return publicUrlPrefix + url
    }
}

function LazyImage(attrs: { src: string }) {
    const ref = useRef()

    useEffect(() => {
        if (ref.current) {
            const observer = new IntersectionObserver(
                (entries) => {
                    entries.forEach(entry => {
                        if (entry.intersectionRatio) {
                            (entry.target as HTMLImageElement).src = attrs.src
                        }
                    });
                });
            observer.observe(ref.current);
            return () => {
                observer.disconnect()
            }
        }
    }, [ref.current, attrs.src])

    return <img ref={ref} width={100} height={100}/>
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
        <select style={{position: "absolute"}} multiple size={45} onChange={upd}>
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
                    <Tooltip direction={"top"}>{city.original_address}</Tooltip>
                </CircleMarker>)}

            </MapContainer>
        </div>

        <table>
            <thead>
            <tr>
                <th style={{width: 20}}>#</th>
                <th style={{width: 100}}>Image</th>
                <th style={{width: 100}}>Price, EUR</th>
                <th style={{width: 100}}>Square, m²</th>
                <th style={{width: 100}}>
                    <div>City</div>
                </th>
                <th>Title</th>
            </tr>
            </thead>
            <tbody>
            {filteredItems.map((data, index) => {
                // console.log(data)
                const detailsUrl = "https://www.nekretnine.rs" + data.url
                return [<tr key={index}>
                    <td>{index}</td>
                    <td><LazyImage src={url(data.pictureFile)}/></td>
                    <td style={{whiteSpace: "nowrap"}}>{data.price}</td>
                    <td>{data.square}</td>
                    <td>{data.locationData.city}</td>
                    <td>
                        <a href={detailsUrl}>
                            {data.title}
                        </a>
                    </td>
                </tr>,
                    <tr>
                        <td colSpan={6}  style={{borderBottom: "1px solid black"}}>
                            <OfferDetails offer={data}/>
                        </td>
                    </tr>
                ]
            })}
            </tbody>
        </table>
    </div>
}

function OfferDetails(props: { offer: any }) {
    const [details, setDetails] = useState<OfferDetails>()
    const url = "http://localhost:8010/proxy" + props.offer.url

    function load() {
        if (!details) {
            fetchDetails(url)
                .then(r => setDetails(r))
        }
    }

    if (details && details.url != url) {
        setDetails(null)
    }

    if (details) {
        return <div>
            <div>{details.location.map(place => <span>— {place}</span>)}</div>
            <div style={{display: "flex"}}>
                {OfferDetailProps.map(prop => <li className="prop">{prop}: <b>{details[prop]}</b></li>)}
            </div>
            <div style={{fontSize: "10px"}}>
                {details.Opis}
            </div>
            {details.images.map(src => <img height={100} src={src}/>)}
        </div>;
    } else {
        return <div onClick={load} style={{cursor: "pointer"}}>
            (Click to load details)
        </div>;
    }
}
