import { createContext, useState, useEffect } from 'react';

const TrainContext = createContext();

export const TrainProvider = ({ children }) => {
    const [trainData, setTrainData] = useState([]);
    const [lastUpdate, setLastUpdate] = useState(null);

    const fetchTrainData = async () => {
        const lastFetch = localStorage.getItem('lastFetch');
        const cachedData = localStorage.getItem('cachedTrainData');
        const now = Date.now();
        const fiveMinutes = 5 * 60 * 1000;

        if (lastFetch && now - lastFetch < fiveMinutes && cachedData) {
            //console.log('Using cached data', cachedData);
            setTrainData(JSON.parse(cachedData));
            setLastUpdate(Date.now());
            return;
        }

        try {
            const response = await fetch('http://localhost:3001/trainLocHistories/activeTrains');
            const data = await response.json();
            setTrainData(data);
            setLastUpdate(Date.now());
            localStorage.setItem('lastFetch', Date.now());
            localStorage.setItem('cachedTrainData', JSON.stringify(data));
            //console.log('Podatki o vlakih:', data);
        } catch (error) {
            //console.error('Napaka pri pridobivanju podatkov o vlakih:', error);
        }
    };


    const fetchTrainDataByDateRange = async (startDate, endDate) => {
        try {
            const response = await fetch(`http://localhost:3001/trainLocHistories/trainLocByDate?startDate=${startDate}&endDate=${endDate}`);
            const data = await response.json();
            return data;
        } catch (error) {
            //console.error('Napaka pri pridobivanju podatkov o vlakih:', error);
            return [];
        }
    };

    const fetchDelayStats = async () => {
        try {
            const response = await fetch(`http://localhost:3001/delays/stats`);
            const data = await response.json();
            return data;
        } catch (error) {
            //console.error('Error while getting stats:', error);
            return [];
        }
    };

    const fetchRouteStats = async () => {
        try {
            const response = await fetch(`http://localhost:3001/routes`);
            const data = await response.json();
            return data;
        } catch (error) {
            //console.error('Error while getting stats:', error);
            return [];
        }
    };

    const fetchStationStats = async () => {
        try {
            const response = await fetch(`http://localhost:3001/stations`);
            const data = await response.json();
            return data;
        } catch (error) {
            //console.error('Error while getting stats:', error);
            return [];
        }
    };

    const fetchUserDelay = async () => {
        try {
            let token = localStorage.getItem('token') || null;
            const response = await fetch(`http://localhost:3001/users/delays`, {
                headers: {
                    'Authorization': 'Bearer ' + token
                },
            });
            const data = await response.json();
            return data;
        } catch (error) {
            //console.error('Error while getting user stats:', error);
            return [];
        }
    };

    const addUserDelay = async (listOfDelays, dateOfRide) => {
        try {
            let token = localStorage.getItem('token') || null;
            let data = [];

            for (let i = 0; i < listOfDelays.length; i++) {
                const response = await fetch(`http://localhost:3001/users/delays`,
                    {
                        method: 'POST',
                        headers: {
                            'Authorization': 'Bearer ' + token,
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify({
                            "delay": listOfDelays[i],
                            "time": dateOfRide
                        })
                    }
                );
                data = await response.json();
            }

            return data;
        } catch (error) {
            //console.error('Error while getting stats:', error);
            return [];
        }
    };

    const deleteUserDelay = async (id) => {
        try {
            let token = localStorage.getItem('token') || null;
            const response = await fetch(`http://localhost:3001/users/delays/` + id,
                {
                    method: 'DELETE',
                    headers: {
                        'Authorization': 'Bearer ' + token
                    }
                }
            );
            await response.text();

            return "Deleted";
        } catch (error) {
            //console.error('Error while deleting stats:', error);
            return [];
        }
    };

    useEffect(() => {
        fetchTrainData();
        const intervalId = setInterval(fetchTrainData, 5 * 60 * 1000); // Osvežitev vsake 5 minuti

        return () => clearInterval(intervalId);
    }, []);

    return (
        <TrainContext.Provider value={{ trainData, lastUpdate, fetchTrainDataByDateRange, fetchDelayStats, fetchRouteStats, fetchStationStats, addUserDelay, deleteUserDelay, fetchUserDelay }}>
            {children}
        </TrainContext.Provider>
    );
};

export default TrainContext;
