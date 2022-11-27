import dataUrl from "url:../data/raw/offers-02-with-location.jsonl"
import {useEffect, useState} from "react";

const rawDataUrl = "../data/raw/offers-02-with-location.jsonl"

export function App() {
    const [data, setData] = useState([])

    useEffect(() => {
        const d = fetch(rawDataUrl)
            .then(r => r.text())
            .then(t => t.split("\n"))
            .then(d => setData(d))
    })

    return <table>
        {data.map((item, index) => {
            try {
                const data = JSON.parse(item)
                // console.log(data)
                return <tr key={index}>
                    <td>{index}</td>
                    {/*<td><img width={100} height={100} src={data.pictureUrl}/></td>*/}
                    <td><img width={100} height={100} src={"../" + data.pictureFile}/></td>
                    <td style={{whiteSpace: "nowrap"}}>{data.price}</td>
                    <td>{data.square}</td>
                    <td>{data.locationData.city}</td>
                    <td>
                        <a href={"https://www.nekretnine.rs" + data.url}>
                            {data.title}
                        </a>
                    </td>
                </tr>
            } catch (e) {
                return <tr key={index}>
                    <td>ERROR: {item}</td>
                </tr>
            }
        })}
    </table>;
}