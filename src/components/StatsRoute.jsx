import React, { useState, useEffect, } from 'react';
import { VictoryTheme, VictoryTooltip, VictoryPie } from 'victory';


function classNames(...classes) {
    return classes.filter(Boolean).join(' ')
}


function StatsRoute({ statsRoute }) {    
    const [dataByFilterBikes, setDataByFilterBikes] = useState([{ index: 0, trainNames: [], averageDelayByTrainNumber: 1 }]);
    const [dataByFilterType, setDataByFilterType] = useState([{ index: 0, trainType: "Loading", countOfTrains: 0, label: "Loading"}]);


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

    const getStats = async (statsRoute) => {
        //Group by Type
        let groupedByType = groupBy(statsRoute, stat => stat.trainType);
        //console.log(Array.from(groupedByType));
        let routeByType = []
        let countOfByType = 0;
        let countOfTrainsByType = 0;

        groupedByType.forEach((values, keys) => {
            countOfByType += 1;
            countOfTrainsByType = 0;

            values.forEach((data) => {
                countOfTrainsByType += 1;
            });

            let trainType = values[0].trainType
            routeByType.push({ index: countOfByType, trainType: trainType, countOfTrains: countOfTrainsByType, label: trainType + "\n" + countOfTrainsByType + " trains" })
        })

        if (routeByType.length != 0) {
            setDataByFilterType(routeByType);
        }


        //Group by Bikes
        let groupedByBikes = groupBy(statsRoute, stat => stat.canSupportBikes);
        let routeByBikes = []

        routeByBikes.push({ index: 0, count: groupedByBikes.get(true).length, supports: true, label: groupedByBikes.get(true).length + " trains\nsupport"})
        routeByBikes.push({ index: 1, count: groupedByBikes.get(false).length, supports: false, label: groupedByBikes.get(false).length + " trains\nnot support"})
        if (routeByBikes.length != 0) {
            setDataByFilterBikes(routeByBikes);
        }
    };

    useEffect(() => {
        getStats(statsRoute);
        
    }, [statsRoute]);

    return (
        <div>
            <div className="divide-y divide-gray-200 overflow-hidden rounded-lg bg-white shadow my-8">
                <div className="px-4 py-5 sm:px-6">
                    <h4 className="text-xl">Number of trains by Type</h4>
                </div>
                <div className="px-4 py-5 sm:p-6">
                    <p>Total: { dataByFilterType.reduce((sum, currentValue) => {
                        return sum + currentValue.countOfTrains
                    }, 0)}</p>
                    <div className="size-3/6">
                        <VictoryPie
                            theme={VictoryTheme.material}
                            data={dataByFilterType}
                            innerRadius={100}
                            labelComponent={<VictoryTooltip />}
                            x="trainType" y="countOfTrains">
                        </VictoryPie>
                    </div>
                </div>
            </div>


            <div className="divide-y divide-gray-200 overflow-hidden rounded-lg bg-white shadow my-8">
                <div className="px-4 py-5 sm:px-6">
                    <h4 className="text-xl">Trains by supproting bikes</h4>
                </div>
                <div className="px-4 py-5 sm:p-6">
                    <div className="size-3/6">
                        <VictoryPie
                            theme={VictoryTheme.material}
                            data={dataByFilterBikes}
                            innerRadius={100}
                            labelComponent={<VictoryTooltip />}
                            x="label" y="count">
                        </VictoryPie>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default StatsRoute;