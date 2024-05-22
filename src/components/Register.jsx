import { useContext, useState } from 'react';
import { UserContext } from '../UserContext';
import { Navigate } from 'react-router-dom';
import trainIcon from '../assets/train_icon.png';

function Register() {
    const [username, setUsername] = useState([]);
    const [password, setPassword] = useState([]);
    const [email, setEmail] = useState([]);
    const [error, setError] = useState([]);
    const userContext = useContext(UserContext);

    const handleRegister = async (e) => {
        e.preventDefault();
        const res = await fetch("http://localhost:3001/users", {
            method: "POST",
            credentials: "include",
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                email: email,
                username: username,
                password: password
            })
        });
        const data = await res.json();
        if (data.email && data.username && data.password) {
            const loginSuccess = userContext.login(username, password);
            if (!loginSuccess) {
                setError("Registration successful, but automatic login failed.");
            }
        } else {
            setError("Registration failed");
        }
    };

    return (
        <div className="flex min-h-full flex-1 flex-col justify-center px-6 py-12 lg:px-8">
            <div className="sm:mx-auto sm:w-full sm:max-w-sm">
                <img
                    className="mx-auto h-12 w-auto"
                    src={trainIcon}
                    alt="Train Icon"
                />
                <h2 className="mt-10 text-center text-2xl font-bold leading-9 tracking-tight text-gray-900">
                    Sign up for an account
                </h2>
            </div>

            <div className="mt-10 sm:mx-auto sm:w-full sm:max-w-sm">
                <form className="space-y-6" onSubmit={handleRegister}>
                    {userContext.token ? <Navigate replace to="/" /> : ""}
                    <div>
                        <label htmlFor="email" className="font-bold block text-sm font-medium leading-6 text-gray-900">
                            Email address
                        </label>
                        <div className="mt-2">
                            <input
                                type="email"
                                name="email"
                                id="email"
                                autoComplete="email"
                                required
                                className="pl-2 block  w-full rounded-md border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
                                placeholder=""
                                onChange={(e) => setEmail(e.target.value)}
                            />
                        </div>
                    </div>

                    <div>
                        <label htmlFor="username" className="font-bold block text-sm font-medium leading-6 text-gray-900">
                            Username
                        </label>
                        <div className="mt-2">
                            <input
                                type="text"
                                name="username"
                                id="username"
                                autoComplete="username"
                                required
                                className="pl-2 block w-full rounded-md border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                            />
                        </div>
                    </div>

                    <div>
                        <label htmlFor="password" className="font-bold block text-sm font-medium leading-6 text-gray-900">
                            Password
                        </label>
                        <div className="mt-2">
                            <input
                                type="password"
                                name="password"
                                id="password"
                                autoComplete="current-password"
                                required
                                className="pl-2 block w-full rounded-md border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                            />
                        </div>
                    </div>

                    <div>
                        <button
                            type="submit"
                            className="flex w-full justify-center rounded-md bg-indigo-600 px-3 py-1.5 text-sm font-semibold leading-6 text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
                        >
                            Sign up
                        </button>
                    </div>

                    {error && <p className="mt-2 text-sm text-red-600">{error}</p>}
                </form>

                <p className="mt-10 text-center text-sm text-gray-500">
                    Already a member?{' '}
                    <a href="/login" className="font-semibold leading-6 text-indigo-600 hover:text-indigo-500">
                        Sign in to your account
                    </a>
                </p>
            </div>
        </div>
    );
}

export default Register;
