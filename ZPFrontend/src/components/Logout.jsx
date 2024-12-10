import { useEffect, useContext } from 'react';
import { UserContext } from '../UserContext';
import { Navigate } from 'react-router-dom';

function Logout() {
    const userContext = useContext(UserContext);
    useEffect(() => {
        const logout = async () => {
            const token = localStorage.getItem('token'); // get token before reseting userContext
            userContext.setUserContext(null);
            localStorage.removeItem('token');
          
            //console.log("token: " + token)

            await fetch("http://localhost:3001/users/token", {
                method: 'DELETE',
                credentials: "include",
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`                }
            });
        };
        logout();
    }, [userContext]);

    return (
        <Navigate replace to="/" />
    );
}

export default Logout;