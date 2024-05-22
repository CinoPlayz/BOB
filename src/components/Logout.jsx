import { useEffect, useContext } from 'react';
import { UserContext } from '../UserContext';
import { Navigate } from 'react-router-dom';

function Logout() {
    const userContext = useContext(UserContext);
    useEffect(() => {
        const logout = async () => {
            userContext.setUserContext(null);
            localStorage.removeItem('token');
            // const res = await fetch("http://localhost:3001/users/logout");
        };
        logout();
    }, [userContext]);

    return (
        <Navigate replace to="/" />
    );
}

export default Logout;