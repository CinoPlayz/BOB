import React, { useState, useEffect, useContext, useRef } from 'react';
import TrainContext from '../TrainContext';
import trainIcon from '../assets/train_icon.png';

function HistoryMap() {
    const { fetchTrainDataByDateRange } = useContext(TrainContext);
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');
    const [trainData, setTrainData] = useState([]);
    const [isPaused, setIsPaused] = useState(false);
    const [speed, setSpeed] = useState(1);
    const [currentDateTime, setCurrentDateTime] = useState(null);
    const mapRef = useRef(null);
    const timerRef = useRef(null);
    const currentGroupIndexRef = useRef(0);

    useEffect(() => {
        if (!mapRef.current) {
            mapRef.current = L.map('map').setView([46.1512, 14.9955], 9);
            L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
                maxZoom: 19,
                attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
            }).addTo(mapRef.current);
        }
    }, []);

    const handleSearch = async () => {
        const data = await fetchTrainDataByDateRange(startDate, endDate);
        setTrainData(data);
    };

    const displayNextGroup = (timeOfRequestGroups, sortedGroupTimes) => {
        if (currentGroupIndexRef.current >= sortedGroupTimes.length) return;
        const time = parseInt(sortedGroupTimes[currentGroupIndexRef.current]);
        const group = timeOfRequestGroups[time];
        setCurrentDateTime(new Date(time));

        if (mapRef.current) {
            mapRef.current.eachLayer((layer) => {
                if (layer instanceof L.Marker) {
                    mapRef.current.removeLayer(layer);
                }
            });
        }

        group.forEach(train => {
            const { trainNumber, coordinates, nextStation, delay } = train;
            if (coordinates) {
                const { lat, lng } = coordinates;
                const marker = L.marker([lat, lng], { icon: L.icon({ iconUrl: trainIcon, iconSize: [25, 41], iconAnchor: [12, 41] }) }).addTo(mapRef.current);
                marker.bindPopup(`
                    <div>
                        <b>Train Number:</b> ${trainNumber}<br>
                        <b>Next Station:</b> ${nextStation}<br>
                        <b>Delay:</b> ${delay} min
                    </div>
                `);
            }
        });

        currentGroupIndexRef.current++;
        if (!isPaused) {
            timerRef.current = setTimeout(() => displayNextGroup(timeOfRequestGroups, sortedGroupTimes), 5000 / speed);
        }
    };

    useEffect(() => {
        if (trainData.length > 0) {
            currentGroupIndexRef.current = 0;
            const timeOfRequestGroups = trainData.reduce((groups, train) => {
                const time = new Date(train.timeOfRequest).getTime();
                if (!groups[time]) groups[time] = [];
                groups[time].push(train);
                return groups;
            }, {});
            const sortedGroupTimes = Object.keys(timeOfRequestGroups).sort((a, b) => a - b);

            displayNextGroup(timeOfRequestGroups, sortedGroupTimes);

            return () => clearTimeout(timerRef.current);
        }
    }, [trainData, speed]);

    const handlePauseResume = () => {
        setIsPaused(prev => !prev);
    };

    useEffect(() => {
        if (isPaused) {
            clearTimeout(timerRef.current);
        } else {
            if (trainData.length > 0) {
                const timeOfRequestGroups = trainData.reduce((groups, train) => {
                    const time = new Date(train.timeOfRequest).getTime();
                    if (!groups[time]) groups[time] = [];
                    groups[time].push(train);
                    return groups;
                }, {});
                const sortedGroupTimes = Object.keys(timeOfRequestGroups).sort((a, b) => a - b);
                displayNextGroup(timeOfRequestGroups, sortedGroupTimes);
            }
        }
    }, [isPaused]);

    return (
        <div>
            <div style={{
                position: 'absolute',
                top: '10px',
                left: '50%',
                transform: 'translateX(-50%)',
                zIndex: 1000,
                backgroundColor: 'white',
                padding: '10px',
                borderRadius: '8px',
                boxShadow: '0 4px 8px rgba(0, 0, 0, 0.1)',
                marginTop: '4%',
            }}>
                <label>
                    Start Date:
                    <input type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} />
                </label>
                <label>
                    End Date:
                    <input type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} />
                </label>
                <button onClick={handleSearch}>Search</button>
                <div>
                    <label>
                        Animation Speed:
                        <select value={speed} onChange={(e) => setSpeed(parseInt(e.target.value))}>
                            <option value={1}>1x</option>
                            <option value={2}>2x</option>
                            <option value={3}>3x</option>
                            <option value={4}>4x</option>
                            <option value={5}>5x</option>
                        </select>
                    </label>
               
                <button onClick={handlePauseResume} style={{paddingLeft: '10px'}}>
                {isPaused ? '▶️' : '⏸️'}
                </button>
                </div>
                {currentDateTime && <div>Current Time: {currentDateTime.toLocaleString()}</div>}
            </div>
            <div id="map" style={{ height: '100vh', width: '100vw' }}></div>
        </div>
    );
}

export default HistoryMap;