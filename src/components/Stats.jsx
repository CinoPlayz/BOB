import React, { useState, useEffect, useContext, useRef } from 'react';
import TrainContext from '../TrainContext';
import { VictoryBar, VictoryChart, VictoryAxis, VictoryZoomContainer, VictoryBrushContainer, Background, VictoryTheme, VictoryTooltip, VictoryPie } from 'victory';
import { Label, Listbox, ListboxButton, ListboxOption, ListboxOptions, Transition } from '@headlessui/react'
import { CheckIcon, ChevronUpDownIcon } from '@heroicons/react/20/solid';
import { useParentSize } from '@cutting/use-get-parent-size';

const orderByOptions = [{ id: 1, name: "Unordered" }, { id: 2, name: "Asc" }, { id: 3, name: "Desc" }]

function classNames(...classes) {
    return classes.filter(Boolean).join(' ')
}

function Stats() {
    const { fetchStats } = useContext(TrainContext);
    const [dataByFilterTrainNumber, setDataByFilterTrainNumber] = useState([{ index: 0, trainName: "T", averageDelayByTrainNumber: 1 }]);
    const [dataByFilterDelayRange, setDataByFilterDelayRange] = useState([{ index: 0, trainNames: [], averageDelayByTrainNumber: 1 }]);
    const [dataByFilterType, setDataByFilterType] = useState([{ index: 0, trainType: [], averageDelayByTrainNumber: 1 }]);
    const [zoomDomain, setZoomDomain] = useState({ x: [1.645427709190672, 9.049852400548696], y: [0, 25.1] });
    const [selectedDomain, setSelectedDomain] = useState({});
    const [orderBy, setOrderBy] = useState(orderByOptions[0]);
    const [isSorted, setIsSorted] = useState(false);
    const [previousOrderBy, setPreviousOrderBy] = useState(1);
    const ref = useRef(null);
    const { width, height } = useParentSize(ref);




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

    function getRangeByDelay(delay) {
        if (delay <= 5) {
            return "0-5";
        }
        else if (delay <= 15) {
            return "6-15";
        }
        else if (delay <= 30) {
            return "16-30";
        }
        else {
            return "31+";
        }
    }



    const getStats = async () => {
        const statsData = await fetchStats();

        //Group By Train Number
        const groupedByTrainNumber = groupBy(statsData, stat => stat.route.trainNumber);
        //console.log(Array.from(groupedByTrainNumber));
        let delayByTrainNumber = [];
        let countOfByTrainNumber = 1;
        groupedByTrainNumber.forEach((values, keys) => {
            countOfByTrainNumber += 1;
            let sum = 0;
            let count = 0;
            values.forEach((data) => {
                sum += data.delay;
                count += 1;
            });

            let averageDelayByTrainNumber = sum / count;

            let trainName = values[0].route.trainType + " " + values[0].route.trainNumber

            delayByTrainNumber.push({ index: countOfByTrainNumber, trainName: trainName, averageDelayByTrainNumber })
        });

        if (delayByTrainNumber.length != 0) {
            setDataByFilterTrainNumber(delayByTrainNumber);
        }


        //Group by Delay Range
        const groupedByDelayRange = groupBy(delayByTrainNumber, stat => getRangeByDelay(Math.round(stat.averageDelayByTrainNumber)));
        let delayByRange = []
        let countOfByDelayRange = 0;

        //console.log(Array.from(groupedByDelayRange));

        groupedByDelayRange.forEach((values, keys) => {
            countOfByDelayRange += 1

            let trainNames = []
            let countOfTrainsByDelayRange = 0;

            values.forEach((data) => {
                trainNames.push(data.trainName);
                countOfTrainsByDelayRange += 1;
            });

            let averageDelay = Math.round(values[0].averageDelayByTrainNumber)

            delayByRange.push({ index: countOfByDelayRange, trainNames: trainNames, delayRange: averageDelay, countOfTrainsByDelayRange: countOfTrainsByDelayRange, label: "Zamuda: " + keys + "min\nSt: " + countOfTrainsByDelayRange })
        });

        if (delayByRange.length != 0) {
            setDataByFilterDelayRange(delayByRange);
        }


        //Group by Type
        let groupedByType = groupBy(statsData, stat => stat.route.trainType);
        let delayByType = []
        let countOfByType = 0;

        groupedByType.forEach((values, keys) => {
            countOfByType += 1;
            let sum = 0;
            let count = 0;
            values.forEach((data) => {
                sum += data.delay;
                count += 1;
            });

            let averageDelayByType = sum / count;
            let trainType = values[0].route.trainType
            delayByType.push({ index: countOfByType, trainType: trainType, averageDelayByType, label: trainType + "\n" + Math.round(averageDelayByType) + "min" })
        })

        if (delayByType.length != 0) {
            setDataByFilterType(delayByType);
        }


    };

    useEffect(() => {
        getStats()
    }, []);

    function sortForDelay(orderDelay) {
        if (!isSorted) {
            orderDelay.sort(function (a, b) {
                return a.averageDelayByTrainNumber - b.averageDelayByTrainNumber;
            });
            setIsSorted(true);
        }
    }

    function changeOrder(orderDelay, previousOrderedBy) {
        let countOfByTrainNumber = 0;
        orderDelay.map((element) => { element.index = countOfByTrainNumber; countOfByTrainNumber += 1 });
        setPreviousOrderBy(previousOrderedBy);
    }

    useEffect(() => {
        let orderDelay = dataByFilterTrainNumber.slice(0);

        /*if(!isSorted){
            console.log("here");
            orderDelay.sort(function(a,b) {
                return a.averageDelayByTrainNumber - b.averageDelayByTrainNumber;
            });
            setIsSorted(true);
        }*/

        switch (orderBy.id) {
            case 2:

                sortForDelay(orderDelay);
                console.log(previousOrderBy);
                if (previousOrderBy == 3) {
                    orderDelay.reverse();
                    changeOrder(orderDelay, 2);
                }

                if (previousOrderBy == 1) {
                    changeOrder(orderDelay, 2);

                }


                break;
            case 3:
                sortForDelay(orderDelay);
                console.log(previousOrderBy);
                if (previousOrderBy == 2 || previousOrderBy == 1) {
                    orderDelay.reverse();
                    changeOrder(orderDelay, 3);
                }
                break;
            default:
                break;
        }

        setDataByFilterTrainNumber(orderDelay);

    }, [orderBy])


    return (
        <div className="mx-auto max-w-7xl sm:px-6 lg:px-8">

            <div className="divide-y divide-gray-200 overflow-hidden rounded-lg bg-white shadow my-8">
                <div className="px-4 py-5 sm:px-6">
                    <h4 className="text-xl">Delays by Train</h4>
                </div>
                <div className="px-4 py-5 sm:p-6">
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

                    <div ref={ref} className="grid place-content-center">
                        <VictoryChart
                            width={width - 30}
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

                            <VictoryAxis tickValues={dataByFilterTrainNumber.map(data => data.index)} tickFormat={dataByFilterTrainNumber.map(data => data.trainName)}></VictoryAxis>

                            <VictoryAxis dependentAxis>
                            </VictoryAxis>

                            <VictoryBar
                                style={{
                                    data: { stroke: "tomato" }
                                }}
                                barWidth={8}
                                data={dataByFilterTrainNumber}
                                x="index" y="averageDelayByTrainNumber"
                                labels={dataByFilterTrainNumber.map(data => Math.round(data.averageDelayByTrainNumber))}
                                labelComponent={<VictoryTooltip />}
                            />

                        </VictoryChart>

                        <VictoryChart
                            width={width - 30}
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
                                tickValues={dataByFilterTrainNumber.map(data => data.index)}
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
                                data={dataByFilterTrainNumber}
                                x="index" y="averageDelayByTrainNumber"
                            />
                        </VictoryChart>

                    </div>
                </div>
            </div>


            <div className="divide-y divide-gray-200 overflow-hidden rounded-lg bg-white shadow my-8">
                <div className="px-4 py-5 sm:px-6">
                    <h4 className="text-xl">Delays by Delay Range</h4>
                </div>
                <div className="px-4 py-5 sm:p-6">
                    <VictoryPie
                        theme={VictoryTheme.material}
                        data={dataByFilterDelayRange}
                        innerRadius={100}
                        labelComponent={<VictoryTooltip />}
                        x="delayRange" y="countOfTrainsByDelayRange">
                    </VictoryPie>
                </div>
            </div>

            <div className="divide-y divide-gray-200 overflow-hidden rounded-lg bg-white shadow my-8">
                <div className="px-4 py-5 sm:px-6">
                    <h4 className="text-xl">Delays by Train Type</h4>
                </div>
                <div className="px-4 py-5 sm:p-6">
                <VictoryPie
                    theme={VictoryTheme.material}
                    data={dataByFilterType}
                    innerRadius={100}
                    labelComponent={<VictoryTooltip />}
                    x="trainType" y="averageDelayByType">
                </VictoryPie>
                </div>
            </div>


            <ul>
                {
                    dataByFilterTrainNumber.map(data => <li key={data.trainName}>{data.averageDelayByTrainNumber}</li>)
                }
            </ul>
        </div>



    );
}

export default Stats;
