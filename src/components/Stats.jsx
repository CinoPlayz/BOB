import React, { useState, useEffect, useContext } from 'react';
import TrainContext from '../TrainContext';
import { VictoryBar, VictoryChart, VictoryAxis, VictoryZoomContainer, VictoryBrushContainer, Background, VictoryTheme, VictoryTooltip, VictoryPie } from 'victory';
import { Label, Listbox, ListboxButton, ListboxOption, ListboxOptions, Transition } from '@headlessui/react'
import { CheckIcon, ChevronUpDownIcon } from '@heroicons/react/20/solid'

const orderByOptions = [{ id: 1, name: "Unordered" }, { id: 2, name: "Asc" }, { id: 3, name: "Desc" }]

function classNames(...classes) {
    return classes.filter(Boolean).join(' ')
  }

function Stats() {
    const { fetchStats } = useContext(TrainContext);
    const [statsData, setStatsData] = useState([]);
    const [dataByFilter, setDataByFilter] = useState([{ index: 0, trainName: "T", averageDelayByFilter: 1 }]);
    const [dataByFilterPie, setDataByFilterPie] = useState([{ index: 0, trainNames: [], averageDelayByFilter: 1 }]);
    const [zoomDomain, setZoomDomain] = useState({ x: [1.645427709190672, 9.049852400548696], y: [0, 25.1] });
    const [selectedDomain, setSelectedDomain] = useState({});
    const [orderBy, setOrderBy] = useState(orderByOptions[0]);
    const [isSorted, setIsSorted] = useState(false);
    const [previousOrderBy, setPreviousOrderBy] = useState(1);




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

    function getRangeByDelay(delay){
        if(delay <= 5){
            return "0-5";
        }
        else if(delay <= 15){
            return "6-15";
        }
        else if (delay <= 30){
            return "16-30";
        }
        else {
            return "31+";
        }
    }



    const getStats = async () => {
        const statsData = await fetchStats();
        setStatsData(statsData);

        const groupedByTrainNumber = groupBy(statsData, stat => stat.route.trainNumber);

        let delayByFilter = []

        //console.log(Array.from(groupedByTrainNumber));
        let countOfByFilter = 1;
        groupedByTrainNumber.forEach((values, keys) => {
            countOfByFilter += 1;
            let sum = 0;
            let count = 0;
            values.forEach((data) => {
                sum += data.delay;
                count += 1;
            });

            let averageDelayByFilter = sum / count;

            let trainName = values[0].route.trainType + " " + values[0].route.trainNumber

            delayByFilter.push({ index: countOfByFilter, trainName: trainName, averageDelayByFilter })
        });

        if (delayByFilter.length != 0) {
            setDataByFilter(delayByFilter);
        }

        


        const groupedByDelay = groupBy(delayByFilter, stat => getRangeByDelay(Math.round(stat.averageDelayByFilter)));
        let delayByFilterPie = []
        let countOfByFilterPie = 0;

        //console.log(Array.from(groupedByDelay));

        groupedByDelay.forEach((values, keys) => {
            countOfByFilterPie += 1

            let trainNames = []
            let countOfTrainsByDelay = 0;

            values.forEach((data) => {
                trainNames.push(data.trainName);
                countOfTrainsByDelay += 1;
            });

            let averageDelay = Math.round(values[0].averageDelayByFilter)

            delayByFilterPie.push({ index: countOfByFilterPie, trainNames: trainNames, averageDelayByFilter: averageDelay, countOfTrainsByDelay: countOfTrainsByDelay, label: "Zamuda: " + keys + "min\nSt: " + countOfTrainsByDelay })
        });

        if (delayByFilterPie.length != 0) {
            setDataByFilterPie(delayByFilterPie);
        }

        console.log(delayByFilterPie)


    };

    useEffect(() => {
        getStats()
    }, []);

    function sortForDelay(orderDelay){
        if(!isSorted){
            orderDelay.sort(function(a,b) {
                return a.averageDelayByFilter - b.averageDelayByFilter;
            });
            setIsSorted(true);
        }
    }

    function changeOrder(orderDelay, previousOrderedBy){
        let countOfByFilter = 0;  
        orderDelay.map((element) => {element.index = countOfByFilter; countOfByFilter += 1});
        setPreviousOrderBy(previousOrderedBy);
    }

    useEffect(() => {
        let orderDelay = dataByFilter.slice(0);
        let orderDelayPie = dataByFilterPie.slice(0);

        /*if(!isSorted){
            console.log("here");
            orderDelay.sort(function(a,b) {
                return a.averageDelayByFilter - b.averageDelayByFilter;
            });
            setIsSorted(true);
        }*/

        switch (orderBy.id){
            case 2 :     
                
                sortForDelay(orderDelay);
                console.log(previousOrderBy);
                if(previousOrderBy == 3){                    
                    orderDelay.reverse();
                    changeOrder(orderDelay , 2);
                }     
                
                if(previousOrderBy == 1){
                    changeOrder(orderDelay, 2);
                    
                }

                
            break;
            case 3: 
                sortForDelay(orderDelay);
                console.log(previousOrderBy);
                if(previousOrderBy == 2 || previousOrderBy == 1){
                    orderDelay.reverse();
                    changeOrder(orderDelay, 3);
                } 
                break;
            default: 
            break;
        }

        setDataByFilter(orderDelay); 
        setDataByFilterPie(orderDelayPie);
        

    }, [orderBy])
    

    return (
        <div>

            <Listbox value={orderBy} onChange={setOrderBy}>
                {({ open }) => (
                    <>
                        <Label className="block text-sm font-medium leading-6 text-gray-900">Order By</Label>

                        <div className="relative mt-2">
                            <ListboxButton className="relative w-full cursor-default rounded-md bg-white py-1.5 pl-3 pr-10 text-left text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 focus:outline-none focus:ring-2 focus:ring-indigo-600 sm:text-sm sm:leading-6">
                                <span className="block truncate">{orderBy.name}</span>
                                <span className="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-2">
                                    <ChevronUpDownIcon className="h-5 w-5 text-gray-400" aria-hidden="true" />
                                </span>
                            </ListboxButton>

                            <Transition show={open} leave="transition ease-in duration-100" leaveFrom="opacity-100" leaveTo="opacity-0">
                                <ListboxOptions className="absolute z-10 mt-1 max-h-60 w-full overflow-auto rounded-md bg-white py-1 text-base shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none sm:text-sm">
                                    {orderByOptions.map((order) => (
                                        <ListboxOption
                                            key={order.id}
                                            className={({ focus }) =>
                                                classNames(
                                                    focus ? 'bg-indigo-600 text-white' : '',
                                                    !focus ? 'text-gray-900' : '',
                                                    'relative cursor-default select-none py-2 pl-3 pr-9'
                                                )
                                            }
                                            value={order}
                                        >
                                            {({ selected, focus }) => (
                                                <>
                                                    <span className={classNames(selected ? 'font-semibold' : 'font-normal', 'block truncate')}>
                                                        {order.name}
                                                    </span>

                                                    {selected ? (
                                                        <span
                                                            className={classNames(
                                                                focus ? 'text-white' : 'text-indigo-600',
                                                                'absolute inset-y-0 right-0 flex items-center pr-4'
                                                            )}
                                                        >
                                                            <CheckIcon className="h-5 w-5" aria-hidden="true" />
                                                        </span>
                                                    ) : null}
                                                </>
                                            )}
                                        </ListboxOption>
                                    ))}
                                </ListboxOptions>
                            </Transition>
                        </div>
                    </>
                )}
            </Listbox>




            <div>
                <VictoryChart
                    width={550}
                    height={300}
                    theme={VictoryTheme.material}

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

                    <VictoryAxis tickValues={dataByFilter.map(data => data.index)} tickFormat={dataByFilter.map(data => data.trainName)}></VictoryAxis>

                    <VictoryAxis dependentAxis>
                    </VictoryAxis>

                    <VictoryBar
                        style={{
                            data: { stroke: "tomato" }
                        }}
                        barWidth={8}
                        data={dataByFilter}
                        x="index" y="averageDelayByFilter"
                        labels={dataByFilter.map(data => Math.round(data.averageDelayByFilter))}
                        labelComponent={<VictoryTooltip />}
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

                <VictoryPie
                    theme={VictoryTheme.material}
                    data={dataByFilterPie}
                    innerRadius={100}
                    labelComponent={<VictoryTooltip />}
                    x="averageDelayByFilter" y="countOfTrainsByDelay">


                </VictoryPie>

            </div>

            <ul>
                {
                    dataByFilter.map(data => <li key={data.trainName}>{data.averageDelayByFilter}</li>)
                }
            </ul>

        </div>
    );
}

export default Stats;
