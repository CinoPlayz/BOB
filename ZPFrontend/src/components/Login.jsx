import { useContext, useState } from 'react';
import { UserContext } from '../UserContext';
import { Navigate } from 'react-router-dom';
import trainIcon from '../assets/train_icon.png';

function Login() {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [totp, setTotp] = useState("");
    const [error, setError] = useState("");
    const [loginToken, setLoginToken] = useState(null);
    const userContext = useContext(UserContext);

    const handleInitialLogin = async (e) => {
        setError("");
        e.preventDefault();
        const result = await userContext.login(username, password);
        if (result.success) {
            setError("");
        } else if (result.loginToken) {
            setLoginToken(result.loginToken);
        } else {
            setUsername("");
            setPassword("");
            setError("Invalid username or password");
        }
    };

    const handleTOTPLogin = async (e) => {
        setError("");
        e.preventDefault();
        const result = await userContext.verifyTOTP(loginToken, totp, username);
        if (result.success) {
            setError("");
        } else {
            setTotp("");
            setError("Invalid TOTP");
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
                    Sign in to your account
                </h2>
            </div>

            <div className="mt-10 sm:mx-auto sm:w-full sm:max-w-sm">
                <form className="space-y-6" onSubmit={loginToken ? handleTOTPLogin : handleInitialLogin}>
                    {userContext.token ? <Navigate replace to="/map" /> : ""}
                    {!loginToken && (
                        <>
                            <div>
                                <label htmlFor="username" className="block text-sm font-medium leading-6 text-gray-900 font-bold">
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
                                <label htmlFor="password" className="block text-sm font-medium leading-6 text-gray-900 font-bold">
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
                        </>
                    )}

                    {loginToken && (
                        <div>
                            <label htmlFor="totp" className="block text-sm font-medium leading-6 text-gray-900 font-bold">
                                TOTP Token
                            </label>
                            <div className="mt-2">
                                <input
                                    type="text"
                                    name="totp"
                                    id="totp"
                                    required
                                    className="pl-2 block w-full rounded-md border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-indigo-600 sm:text-sm sm:leading-6"
                                    value={totp}
                                    onChange={(e) => setTotp(e.target.value)}
                                />
                            </div>
                        </div>
                    )}

                    <div>
                        <button
                            type="submit"
                            className="flex w-full justify-center rounded-md bg-indigo-600 px-3 py-1.5 text-sm font-semibold leading-6 text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
                        >
                            {loginToken ? "Verify TOTP" : "Sign in"}
                        </button>
                    </div>

                    {error && <p className="mt-2 text-sm text-red-600">{error}</p>}
                </form>
            </div>
        </div>
    );
}

export default Login;