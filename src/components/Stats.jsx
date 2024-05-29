import React, { useState, useEffect, useContext } from 'react';
import TrainContext from '../TrainContext';
import { Label, Listbox, ListboxButton, ListboxOption, ListboxOptions, Transition } from '@headlessui/react'
import { CheckIcon, ChevronUpDownIcon } from '@heroicons/react/20/solid';
import StatsDelay from './StatsDelay';
import StatsRoute from './StatsRoute';

const showSelectedOptions = [{ id: 1, name: "Delays" }, { id: 2, name: "Train info" }, { id: 3, name: "Delays By Route" }]

function classNames(...classes) {
    return classes.filter(Boolean).join(' ')
}

function Stats() {
    const { fetchDelayStats, fetchRouteStats } = useContext(TrainContext);
    const [showSelected, setShowSelected] = useState(showSelectedOptions[0]);
    const [statsDelay, setstatsDelay] = useState([{ route: { trainType: "Loading", trainNumber: "Loading" } }]);
    const [statsRoute, setStatsRoute] = useState([{ route: { trainType: "Loading", trainNumber: "Loading" } }]);

    const getStats = async () => {
        const statsDelayFetch = await fetchDelayStats();
        setstatsDelay(statsDelayFetch);

        const statsRouteFetch = await fetchRouteStats();
        setStatsRoute(statsRouteFetch);
    };

    useEffect(() => {
        getStats()
    }, []);


    return (
        <div className="mx-auto max-w-7xl sm:px-6 lg:px-8">
            <Listbox value={showSelected} onChange={setShowSelected}>
                {({ open }) => (
                    <>
                        <Label className="block text-sm font-medium leading-6 text-gray-900">Show stats</Label>
                        <div className="relative mt-2">
                            <ListboxButton className="relative w-full cursor-default rounded-md bg-white py-1.5 pl-3 pr-10 text-left text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 focus:outline-none focus:ring-2 focus:ring-indigo-600 sm:text-sm sm:leading-6">
                                <span className="block truncate">{showSelected.name}</span>
                                <span className="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-2">
                                    <ChevronUpDownIcon className="h-5 w-5 text-gray-400" aria-hidden="true" />
                                </span>
                            </ListboxButton>

                            <Transition show={open} leave="transition ease-in duration-100" leaveFrom="opacity-100" leaveTo="opacity-0">
                                <ListboxOptions className="absolute z-10 mt-1 max-h-60 w-full overflow-auto rounded-md bg-white py-1 text-base shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none sm:text-sm">
                                    {showSelectedOptions.map((select) => (
                                        <ListboxOption
                                            key={select.id}
                                            className={({ focus }) =>
                                                classNames(
                                                    focus ? 'bg-indigo-600 text-white' : '',
                                                    !focus ? 'text-gray-900' : '',
                                                    'relative cursor-default select-none py-2 pl-3 pr-9'
                                                )
                                            }
                                            value={select}
                                        >
                                            {({ showSelected, focus }) => (
                                                <>
                                                    <span className={classNames(showSelected ? 'font-semibold' : 'font-normal', 'block truncate')}>
                                                        {select.name}
                                                    </span>

                                                    {showSelected ? (
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

            {showSelected.id == 1 && <StatsDelay statsDelay={statsDelay} />}
            {showSelected.id == 2 && <StatsRoute statsRoute={statsRoute} />}


        </div>



    );
}

export default Stats;
