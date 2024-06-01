import React, { useState, useEffect, } from 'react';
import { VictoryTheme, VictoryTooltip, VictoryPie } from 'victory';
import { Label, Listbox, ListboxButton, ListboxOption, ListboxOptions, Transition } from '@headlessui/react'
import { CheckIcon, ChevronUpDownIcon } from '@heroicons/react/20/solid'


function classNames(...classes) {
    return classes.filter(Boolean).join(' ')
}


function StatsDelayRoute({ statsDelay, statsRoute, statsStations }) {
    const [dataByFilterBikes, setDataByFilterBikes] = useState([{ index: 0, trainNames: [], averageDelayByTrainNumber: 1 }]);
    const [dataByFilterType, setDataByFilterType] = useState([{ index: 0, trainType: "Loading", countOfTrains: 0, label: "Loading" }]);
    const [schedule, setSchedule] = useState([{ _id: "0", trainName: "", timeStart: "", timeEnd: "" }]);
    const [startStation, setStartStation] = useState(statsStations[0]);
    const [endStation, setEndStation] = useState(statsStations[1]);
    const [dateOfRide, setDateOfRide] = useState("2024-05-23");
    const [checkedTrains, setCheckedTrains] = useState([]);


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

    const getStats = async (statsRoute, statsDelay) => {
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

        routeByBikes.push({ index: 0, count: groupedByBikes.get(true).length, supports: true, label: groupedByBikes.get(true).length + " trains\nsupport" })
        routeByBikes.push({ index: 1, count: groupedByBikes.get(false).length, supports: false, label: groupedByBikes.get(false).length + " trains\nnot support" })
        if (routeByBikes.length != 0) {
            setDataByFilterBikes(routeByBikes);
        }
    };

    useEffect(() => {
        getStats(statsRoute, statsDelay);
    }, [statsRoute]);

    const handleSearchDelays = async (e) => {
        e.preventDefault();

        let scheduleByRoute = []
        statsRoute.forEach((route) => {
            if (route.drivesOn.includes(new Date(dateOfRide).getDay())) {
                let start = {}
                let end = {}
                if (route.start.station == startStation._id) {
                    start = route.start
                }

                route.middle.forEach((routeMiddle) => {
                    //Checks start station in middle
                    if (Object.keys(start).length === 0 && routeMiddle.station == startStation._id) {
                        start = routeMiddle
                    }

                    if (Object.keys(end).length === 0 && Object.keys(start).length === 0 && routeMiddle.station == endStation._id) {
                        end = routeMiddle
                    }
                })

                if (Object.keys(end).length === 0 && route.end.station == endStation._id) {
                    end = route.end
                }

                if (Object.keys(start).length !== 0 && Object.keys(end).length !== 0) {
                    scheduleByRoute.push({ _id: route._id, trainName: route.trainType + " " + route.trainNumber, timeStart: start.time, timeEnd: end.time })
                }
            }

        })

        if (scheduleByRoute.length != 0) {
            scheduleByRoute.sort((a, b) => {
                let aSplit = a.timeStart.split(":");
                let bSplit = b.timeStart.split(":");

                let aValue = parseInt(aSplit[0]) * 60 + parseInt(aSplit[1]);
                let bValue = parseInt(bSplit[0]) * 60 + parseInt(bSplit[1]);

                return aValue - bValue;
            })
            setSchedule(scheduleByRoute);
        }
    };


    const handleCheck = (e) => {
        let checkedValue = e.target.value;
        let checkedTrainsNew = checkedTrains.slice();
        if(!checkedTrains.includes(checkedValue)){
            checkedTrainsNew.push(checkedValue);
        }
        else {
            checkedTrainsNew = checkedTrainsNew.filter(item => item !== checkedValue);
        }

        setCheckedTrains(checkedTrainsNew);
        console.log(checkedTrainsNew);
    };

    return (
        <div>
            <div className="divide-y divide-gray-200 overflow-hidden rounded-lg bg-white shadow my-8">
                <div className="px-4 py-5 sm:px-6">
                    <h4 className="text-xl">Schedule</h4>
                </div>
                <div className="px-4 py-5 sm:p-6">
                    <form onSubmit={handleSearchDelays}>
                        <div className="space-y-12">
                            <div className="pb-24 flex flex-row grow flex-wrap justify-center items-center gap-x-6 gap-y-8 ">
                                <div>
                                    <label htmlFor="date" className="block text-sm font-medium leading-6 text-gray-900">
                                        Date Of Ride
                                    </label>
                                    <div>
                                        <input
                                            type="date"
                                            name="dateOfRide"
                                            id="date"
                                            value={dateOfRide}
                                            onChange={(e) => setDateOfRide(e.target.value)}
                                            className="block rounded-md border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
                                        />
                                    </div>
                                </div>

                                {/*Start station*/}
                                <div>
                                    <Listbox value={startStation} onChange={setStartStation}>
                                        {({ open }) => (
                                            <>
                                                <Label className="block text-sm font-medium leading-6 text-gray-900">Start station</Label>
                                                <div className="relative mt-2">
                                                    <ListboxButton className="relative cursor-default rounded-md bg-white py-1.5 pl-3 pr-10 text-left text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 focus:outline-none focus:ring-2 focus:ring-indigo-600 sm:text-sm sm:leading-6">
                                                        <span className="block truncate">{startStation.name}</span>
                                                        <span className="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-2">
                                                            <ChevronUpDownIcon className="h-5 w-5 text-gray-400" aria-hidden="true" />
                                                        </span>
                                                    </ListboxButton>

                                                    <Transition show={open} leave="transition ease-in duration-100" leaveFrom="opacity-100" leaveTo="opacity-0">
                                                        <ListboxOptions className="absolute z-10 mt-1 max-h-60 overflow-auto rounded-md bg-white py-1 text-base shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none sm:text-sm">
                                                            {statsStations.map((station) => (
                                                                <ListboxOption
                                                                    key={station._id + "1"}
                                                                    className={({ focus }) =>
                                                                        classNames(
                                                                            focus ? 'bg-indigo-600 text-white' : '',
                                                                            !focus ? 'text-gray-900' : '',
                                                                            'relative cursor-default select-none py-2 pl-3 pr-9'
                                                                        )
                                                                    }
                                                                    value={station}
                                                                >
                                                                    {({ startStation, focus }) => (
                                                                        <>
                                                                            <span className={classNames(startStation ? 'font-semibold' : 'font-normal', 'block truncate')}>
                                                                                {station.name}
                                                                            </span>

                                                                            {startStation ? (
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
                                </div>


                                {/*End station*/}
                                <div>
                                    <Listbox value={endStation} onChange={setEndStation}>
                                        {({ open }) => (
                                            <>
                                                <Label className="block text-sm font-medium leading-6 text-gray-900">Start station</Label>
                                                <div className="relative mt-2">
                                                    <ListboxButton className="relative  cursor-default rounded-md bg-white py-1.5 pl-3 pr-10 text-left text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 focus:outline-none focus:ring-2 focus:ring-indigo-600 sm:text-sm sm:leading-6">
                                                        <span className="block truncate">{endStation.name}</span>
                                                        <span className="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-2">
                                                            <ChevronUpDownIcon className="h-5 w-5 text-gray-400" aria-hidden="true" />
                                                        </span>
                                                    </ListboxButton>

                                                    <Transition show={open} leave="transition ease-in duration-100" leaveFrom="opacity-100" leaveTo="opacity-0">
                                                        <ListboxOptions className="absolute z-10 mt-1 max-h-60 overflow-auto rounded-md bg-white py-1 text-base shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none sm:text-sm">
                                                            {statsStations.map((station) => (
                                                                <ListboxOption
                                                                    key={station._id + "2"}
                                                                    className={({ focus }) =>
                                                                        classNames(
                                                                            focus ? 'bg-indigo-600 text-white' : '',
                                                                            !focus ? 'text-gray-900' : '',
                                                                            'relative cursor-default select-none py-2 pl-3 pr-9'
                                                                        )
                                                                    }
                                                                    value={station}
                                                                >
                                                                    {({ endStation, focus }) => (
                                                                        <>
                                                                            <span className={classNames(endStation ? 'font-semibold' : 'font-normal', 'block truncate')}>
                                                                                {station.name}
                                                                            </span>

                                                                            {endStation ? (
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
                                </div>

                                <div className="grid content-end">
                                    <button
                                        type="submit"
                                        className="rounded-md bg-indigo-600 px-3 mt-8 py-2 text-sm font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
                                    >
                                        Search
                                    </button>
                                </div>

                            </div>



                        </div>


                    </form>
                </div>

                <div className="px-4 py-5 sm:p-6">
                    <ul role="list" className="divide-y divide-gray-100">
                        <fieldset>
                            {schedule.map((entry) => (
                                <li key={entry._id} className="flex justify-between gap-x-6 py-5">
                                    <div className="flex h-6 items-center">
                                        <input
                                            aria-describedby="comments-description"
                                            onChange={handleCheck}
                                            value={entry._id}
                                            type="checkbox"
                                            className="h-4 w-4 rounded border-gray-300 text-indigo-600 focus:ring-indigo-600"
                                        />
                                    </div>
                                    <div className="flex min-w-0 gap-x-4">
                                        <p className="text-sm font-semibold leading-6 text-gray-900">{entry.trainName}</p>
                                    </div>
                                    <div className="hidden shrink-0 sm:flex sm:flex-col sm:items-end">
                                        <p className="text-sm leading-6 text-gray-900">{entry.timeStart} - {entry.timeEnd}</p>
                                    </div>
                                </li>
                            ))}
                        </fieldset>
                    </ul>

                </div>


            </div>

            <div className="divide-y divide-gray-200 overflow-hidden rounded-lg bg-white shadow my-8">
                <div className="px-4 py-5 sm:px-6">
                    <h4 className="text-xl">Number of trains by Type</h4>
                </div>
                <div className="px-4 py-5 sm:p-6">
                    <p>Total: {dataByFilterType.reduce((sum, currentValue) => {
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

export default StatsDelayRoute;