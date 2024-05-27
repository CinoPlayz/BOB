import React, { useState, useEffect, useContext } from 'react';
import TrainContext from '../TrainContext';
import { VictoryBar } from 'victory';

function Stats() {
    const { fetchStats } = useContext(TrainContext);
    const [statsData, setStatsData] = useState([]);
    const [mostDelays, setMostDelays] = useState([]);
    const [dataByFilter, setDataByFilter] = useState([{keys: "T", averageDelayByFilter: 1}]);

    function groupBy(list, keyGetter) {
        const map = new Map();
        list.forEach((item) => {
             const key = keyGetter(item);
             const collection = map.get(key);
             if (!collection) {
                 map.set(key, [item]);
             } else {
                 collection.push(item);
             }
        });
        return map;
    }
    


    const getStats = async () => {
        const data = await fetchStats();
        setStatsData(data);
        
        const grouped = groupBy(statsData, stat => stat.route.trainNumber);

        let delayByFilter = []
        let delayLabelByFilter = []

        //console.log(Array.from(grouped));
        grouped.forEach((values, keys) => {    
            let sum = 0;
            let count = 0;
            values.forEach((data) => {
                sum += data.delay;
                count += 1;
            });

            let averageDelayByFilter = sum / count;

            delayByFilter.push({keys, averageDelayByFilter})
            delayLabelByFilter.push(keys)
            //console.log(values, keys);
        });

        if(delayByFilter != []){
            setDataByFilter(delayByFilter);
        }

        //console.log(delayByFilter);
        

        console.log("Delay: ")
        console.log(delayByFilter);

    };

    useEffect(() => {
        getStats()
    }, []);

    return (
        <div>
            <ul>
                {
                    dataByFilter.map(data => <li key={data.keys}>{data.averageDelayByFilter}</li>)
                }
            </ul>
            <VictoryBar data={dataByFilter} x="keys"  y="averageDelayByFilter" />
        </div>
    );
}

export default Stats;
