import { useEffect, useContext, useState,useRef   } from 'react';
import { UserContext } from '../UserContext';
import { Navigate } from 'react-router-dom';
import trainIcon from '../assets/train_icon.png';
import trainData from '../trainData.json';


function Map() {
    const mapRef = useRef(null);
    const [trainData, setTrainData] = useState([]);

    useEffect(() => {
        if (!mapRef.current) {
            mapRef.current = L.map('map').setView([46.1512, 14.9955], 9);
            L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
                maxZoom: 19,
                attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
            }).addTo(mapRef.current);
        }

        const fetchTrainData = async () => {
            try {
                const response = await fetch('http://localhost:3001/trainLocHistories/activeTrains');
                const data = await response.json();
                setTrainData(data);
                console.log('Podatki o vlakih:', data);
            } catch (error) {
                console.error('Napaka pri pridobivanju podatkov o vlakih:', error);
            }
        };

        fetchTrainData();
        const interval = setInterval(fetchTrainData, 5 * 60 * 1000); // Osveževanje na 5 minut

        return () => clearInterval(interval);
    }, []);

    useEffect(() => {
        if (mapRef.current && trainData.length > 0) {
            trainData.forEach(train => {
                const { St_vlaka, Koordinate, Postaja, Vrsta, Zamuda_cas } = train;
                if (Koordinate) {
                    const [longitude, latitude] = Koordinate.split(',').map(Number);
                    const marker = L.marker([latitude, longitude]).addTo(mapRef.current);
                    marker.bindPopup(`
                    <div>
                    <b>St_vlaka:</b> ${St_vlaka}<br>
                    <b>Postaja:</b> ${Postaja}<br>
                    <b>Zamuda:</b> ${Zamuda_cas} min
                </div>
                    `);
                }
            });
        }
    }, [trainData]);

    return <div id="map" style={{ height: '100vh', width: '100vw' }}></div>;
}

export default Map;