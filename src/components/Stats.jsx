import React, { useState, useEffect, useContext } from 'react';
import TrainContext from '../TrainContext';
import { VictoryBar, VictoryChart, VictoryAxis, VictoryContainer, VictoryZoomContainer, VictoryBrushContainer, Background } from 'victory';

function Stats() {
    const { fetchStats } = useContext(TrainContext);
    const [statsData, setStatsData] = useState([]);
    const [mostDelays, setMostDelays] = useState([]);
    const [dataByFilter, setDataByFilter] = useState([{ index: 0, keys: "T", averageDelayByFilter: 1 }]);
    const [zoomDomain, setZoomDomain] = useState({});
    const [selectedDomain, setSelectedDomain] = useState({});


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
        let countOfByFilter = 1;
        let countOfMoreDelay = 0;
        grouped.forEach((values, keys) => {
            countOfByFilter += 1;
            let sum = 0;
            let count = 0;
            values.forEach((data) => {
                sum += data.delay;
                count += 1;
            });

            let averageDelayByFilter = sum / count;

            if(averageDelayByFilter > 0){
                countOfMoreDelay += 1;
            }
            delayByFilter.push({index: countOfByFilter, keys, averageDelayByFilter })
            delayLabelByFilter.push(keys)
            //console.log(values, keys);
        });

        if (delayByFilter.length != 0) {
            setDataByFilter(delayByFilter);
        }



        console.log("Delay: ")
        console.log(delayByFilter);

        console.log(countOfMoreDelay);

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
            <VictoryChart containerComponent={
                <VictoryZoomContainer zoomDomain={
                    { x: [5, 35], y: [0, 25] }
                }
                    zoomDimension="x" />
            }>
                <VictoryAxis tickValues={dataByFilter.map(data => data.keys)} tickFormat={dataByFilter.map(data => data.keys)}>

                </VictoryAxis>
                <VictoryAxis dependentAxis>
                </VictoryAxis>
                <VictoryBar data={dataByFilter} x="keys" y="averageDelayByFilter" />
            </VictoryChart>

            <div>
                <VictoryChart
                    width={550}
                    height={300}

                    containerComponent={
                        <VictoryZoomContainer responsive={false}
                            zoomDimension="x"
                            allowZoom={false}
                            allowPan
                            zoomDomain={zoomDomain}
                            onZoomDomainChange={(domain, props) => setSelectedDomain(domain)}
                        />
                    }
                >

                    <VictoryAxis tickValues={dataByFilter.map(data => data.index)} tickFormat={dataByFilter.map(data => data.keys)}></VictoryAxis>

                    <VictoryAxis dependentAxis>
                    </VictoryAxis>

                    <VictoryBar
                        style={{
                            data: { stroke: "tomato" }
                        }}
                        data={dataByFilter}
                        x="index" y="averageDelayByFilter"
                    />

                </VictoryChart>

                <VictoryChart
                    width={550}
                    height={40}
                    scale={{ x: "index" }}
                    domainPadding={{ x: [10, 10] }}
                    padding={{ top: 0, left: 50, right: 50, bottom: 30 }}

                    containerComponent={
                        <VictoryBrushContainer responsive={false}
                            brushDimension="x"
                            brushDomain={selectedDomain}
                            allowDrag

                            allowDraw
                            brushStyle={{ stroke: "transparent", fill: "black", fillOpacity: 1 }}
                            brushComponent={<rect rx="5" ry="5" width="5" />}
                            onBrushDomainChange={(domain, props) => setZoomDomain(domain)}
                        />
                    }
                    style={{
                        background: { fill: "#c0c0c0" }
                    }}
                    backgroundComponent={<Background rx="5" ry="5" />}
                >
                    <VictoryAxis
                        tickValues={dataByFilter.map(data => data.index)}
                        style={{
                            axis: { display: "none" },
                            labels: { display: "none" },
                            ticks: { display: "none" }
                        }}
                        tickFormat={(x) => null}
                    />
                    
                    <VictoryBar
                        style={{
                            data: { stroke: "transparent", fill: 'transparent' }
                        }}
                        data={dataByFilter}
                        x="index" y="averageDelayByFilter"
                    />
                </VictoryChart>
            </div>

        </div>
    );
}

export default Stats;
