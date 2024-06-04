import React from 'react';
import FrontPhoto from '../assets/front.png';
import '../style/Home.css';
import { Link, useLocation } from 'react-router-dom';
import { Fragment, useState, useContext } from 'react';
import { Disclosure, DisclosureButton, DisclosurePanel, Menu, MenuButton, MenuItem, MenuItems, Transition } from '@headlessui/react';
import { Bars3Icon, BellIcon, XMarkIcon } from '@heroicons/react/24/outline';
import { UserContext } from "../UserContext.jsx";


function Home() {
    const isActive = (path) => {
        return location.pathname === path ? 'border-indigo-500 text-gray-900' : 'border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700';
    };

    const userContext = useContext(UserContext);

    return (
        <div className="home-container">
            <div className="left-side">
                <img src={FrontPhoto} alt="Front" className="overlapping-image" />
            </div>
            <div className="right-side">
                <h1>Welcome to advanced railroad system tracking service.</h1>
                <p>
                    Explore our{' '}
                    <Link to="/map" className={`font-bold ${isActive('/map')}`}>
                        Map
                    </Link>
                </p>
                <UserContext.Consumer>
                    {context => (
                        context.token ?
                            <>
                                <p>
                                    Save personal{' '}
                                    <Link to="/historyMap" className={`font-bold ${isActive('/historyMap')}`}>
                                        Travel History
                                    </Link>
                                    .
                                </p>
                                <p>
                                    Explore{' '}
                                    <Link to="/stats" className={`font-bold ${isActive('/stats')}`}>
                                        Advanced Stats
                                    </Link>
                                    .
                                </p>
                            </>
                            :
                            <>
                                <p>
                                    <Link to="/register" className={`font-bold ${isActive('/register')}`}>
                                        Become a member
                                    </Link>
                                    {' '}for full functionality.
                                </p>
                            </>
                    )}
                </UserContext.Consumer>
            </div>
        </div>
    );
}

export default Home;
