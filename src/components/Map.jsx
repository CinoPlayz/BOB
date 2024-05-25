import { useEffect, useContext, useRef } from 'react';
import TrainContext from '../TrainContext';
import trainIcon from '../assets/train_icon.png';
function Map() {
    const { trainData } = useContext(TrainContext);
    const mapRef = useRef(null);

    useEffect(() => {
        if (!mapRef.current) {
            mapRef.current = L.map('map').setView([46.1512, 14.9955], 9);
            L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
                maxZoom: 19,
                attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
            }).addTo(mapRef.current);
        }

        
        if (mapRef.current) {// Clear existing markers
            mapRef.current.eachLayer((layer) => {
                if (layer instanceof L.Marker) {
                    mapRef.current.removeLayer(layer);
                }
            });
        }

        if (trainData.length > 0) {
            trainData.forEach(train => {
                const { St_vlaka, Koordinate, Postaja, Vrsta, Zamuda_cas } = train;
                if (Koordinate) {
                    const [longitude, latitude] = Koordinate.split(',').map(Number);
                    const marker = L.marker([latitude, longitude], { icon: L.icon({ iconUrl: trainIcon, iconSize: [25, 38], iconAnchor: [12, 41] }) }).addTo(mapRef.current);
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