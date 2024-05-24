
import { Fragment } from 'react';
import { Disclosure, DisclosureButton, DisclosurePanel, Menu, MenuButton, MenuItem, MenuItems, Transition } from '@headlessui/react';
import { Bars3Icon, BellIcon, XMarkIcon } from '@heroicons/react/24/outline';
import { Link, useLocation } from 'react-router-dom';
import { UserContext } from "../UserContext.jsx";
import trainIcon from '../assets/train_icon.png';
import ProfileIcon from '../assets/profile_icon.png';

function classNames(...classes) {
  return classes.filter(Boolean).join(' ');
}

export default function Header(props) {
  const location = useLocation();

  const isActive = (path) => {
    return location.pathname === path ? 'border-indigo-500 text-gray-900' : 'border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700';
  };

  return (
    <Disclosure as="nav" className="bg-white shadow fixed w-full top-0 z-10">
      {({ open }) => (
        <>
          <div className="mx-auto max-w-7xl px-2 sm:px-6 lg:px-8">
            <div className="relative flex h-16 justify-between">
              <div className="absolute inset-y-0 left-0 flex items-center sm:hidden">
                <DisclosureButton className="relative inline-flex items-center justify-center rounded-md p-2 text-gray-400 hover:bg-gray-100 hover:text-gray-500 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-indigo-500">
                  <span className="absolute -inset-0.5" />
                  <span className="sr-only">Open main menu</span>
                  {open ? (
                    <XMarkIcon className="block h-6 w-6" aria-hidden="true" />
                  ) : (
                    <Bars3Icon className="block h-6 w-6" aria-hidden="true" />
                  )}
                </DisclosureButton>
              </div>
              <div className="flex flex-1 items-center justify-center sm:items-stretch sm:justify-start">
                <div className="flex flex-shrink-0 items-center">
                  <img
                    className="h-8 w-auto"
                    src={trainIcon}
                    alt="Train Icon"
                  />
                </div>
                <div className="hidden sm:ml-6 sm:flex sm:space-x-8 h-full items-center">
                  <Link
                    to="/"
                    className={`inline-flex items-center h-full border-b-2 px-1 pt-1 text-sm font-medium ${isActive('/')}`}
                  >
                    Home
                  </Link>
                  <UserContext.Consumer>
                    {context => (
                      context.token ?
                        <>
                          <Link
                            to="/map"
                            className={`inline-flex items-center h-full border-b-2 px-1 pt-1 text-sm font-medium ${isActive('/map')}`}
                          >
                            Map
                          </Link>
                          <Link
                            to="/tmp"
                            className={`inline-flex items-center h-full border-b-2 px-1 pt-1 text-sm font-medium ${isActive('/map')}`}
                          >
                            Tmp
                          </Link>
                        </>
                        :
                        <>
                          <Link
                            to="/login"
                            className={`inline-flex items-center h-full border-b-2 px-1 pt-1 text-sm font-medium ${isActive('/login')}`}
                          >
                            Login
                          </Link>
                          <Link
                            to="/register"
                            className={`inline-flex items-center h-full border-b-2 px-1 pt-1 text-sm font-medium ${isActive('/register')}`}
                          >
                            Register
                          </Link>

                        </>

                    )}
                  </UserContext.Consumer>
                </div>
              </div>
              <UserContext.Consumer>
                {context => (
                  context.token ?
                    <>
                      <div className="absolute inset-y-0 right-0 flex items-center pr-2 sm:static sm:inset-auto sm:ml-6 sm:pr-0">
                        <button
                          type="button"
                          className="relative rounded-full bg-white p-1 text-gray-400 hover:text-gray-500 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2"
                        >
                          <span className="absolute -inset-1.5" />
                          <span className="sr-only">View notifications</span>
                          <BellIcon className="h-6 w-6" aria-hidden="true" />
                        </button>

                        {/* Profile dropdown */}
                        <Menu as="div" className="relative ml-3">
                          <div>
                            <MenuButton className="relative flex rounded-full bg-white text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2">
                              <span className="absolute -inset-1.5" />
                              <span className="sr-only">Open user menu</span>
                              <img
                                className="h-8 w-11 rounded-full"
                                src={ProfileIcon}
                                alt=""
                              />
                            </MenuButton>
                          </div>

                          <Transition
                            as={Fragment}
                            enter="transition ease-out duration-200"
                            enterFrom="transform opacity-0 scale-95"
                            enterTo="transform opacity-100 scale-100"
                            leave="transition ease-in duration-75"
                            leaveFrom="transform opacity-100 scale-100"
                            leaveTo="transform opacity-0 scale-95"
                          >

                            <MenuItems className="absolute right-0 z-10 mt-2 w-48 origin-top-right rounded-md bg-white py-1 shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none">
                              <MenuItem>
                                {({ active }) => (
                                  <a
                                    href="#"
                                    className={classNames(active ? 'bg-gray-100' : '', 'block px-4 py-2 text-sm text-gray-700')}
                                  >
                                    Your Profile
                                  </a>
                                )}
                              </MenuItem>
                              <MenuItem>
                                {({ active }) => (
                                  <a
                                    href="#"
                                    className={classNames(active ? 'bg-gray-100' : '', 'block px-4 py-2 text-sm text-gray-700')}
                                  >
                                    Settings
                                  </a>
                                )}
                              </MenuItem>
                              <MenuItem>
                                {({ active }) => (
                                  <Link to="/logout" className={classNames(active ? 'bg-gray-100' : '', 'block px-4 py-2 text-sm text-gray-700')}>
                                    Sign out
                                  </Link>
                                )}
                              </MenuItem>
                            </MenuItems>
                          </Transition>
                        </Menu>
                      </div>
                    </>
                    :
                    <>
                    </>
                )}
              </UserContext.Consumer>
            </div>
          </div>

          <DisclosurePanel className="sm:hidden">
            <div className="space-y-1 pb-4 pt-2">
              <DisclosureButton
                as={Link}
                to="/"
                className={`block border-l-4 py-2 pl-3 pr-4 text-base font-medium ${isActive('/')}`}
              >
                Home
              </DisclosureButton>
              <DisclosureButton
                as={Link}
                to="/login"
                className={`block border-l-4 py-2 pl-3 pr-4 text-base font-medium ${isActive('/login')}`}
              >
                Login
              </DisclosureButton>
              <DisclosureButton
                as={Link}
                to="/register"
                className={`block border-l-4 py-2 pl-3 pr-4 text-base font-medium ${isActive('/register')}`}
              >
                Register
              </DisclosureButton>
            </div>
          </DisclosurePanel>
        </>
      )}
    </Disclosure>
  );
}
