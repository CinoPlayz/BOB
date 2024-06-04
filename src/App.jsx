import { useEffect, useState } from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { UserContext } from "./UserContext.jsx";
import { TrainProvider } from "./TrainContext.jsx";
import Register from "./components/Register.jsx";
import Header from "./components/Header.jsx";
import Login from './components/Login.jsx';
import Logout from "./components/Logout.jsx";
import Map from "./components/Map.jsx";
import HistoryMap from "./components/HistoryMap.jsx";
import Stats from "./components/Stats.jsx";
import Home from "./components/Home.jsx"

import './App.css'

function App() {
  const [token, setToken] = useState(localStorage.getItem('token') || null);
  const [username, setUsername] = useState(localStorage.getItem('username') || null);

  const updateUserData = (token, username = null) => { //če token obstaja ga shrani v localstorage
    if (token) {
      localStorage.setItem("token", token);
      if (username) {
        localStorage.setItem("username", username);
      }
    } else {
      localStorage.removeItem("token");
      localStorage.removeItem("username");
    }
    setToken(token);
    if (username !== null) {
      setUsername(username);
    }
  };

  useEffect(() => { //preverja ali je token prisoten v localstorage ob inicializaciji
    const savedToken = localStorage.getItem('token');
    const savedUsername = localStorage.getItem('username');
    if (savedToken) {
      setToken(savedToken);
    }
    if (savedUsername) {
      setUsername(savedUsername);
    }
  }, []);

  const login = async (username, password) => {
    const res = await fetch("http://localhost:3001/users/login", {
      method: "POST",
      credentials: "include",
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        username: username,
        password: password
      })
    });

    const data = await res.json();
    if (data.token) {
      updateUserData(data.token, username);
      return { success: true };
    } else if (data.loginToken) {
      return { success: false, loginToken: data.loginToken };
    } else {
      return { success: false };
    }
  };

  const verifyTOTP = async (loginToken, totp, username) => {
    const res = await fetch("http://localhost:3001/users/twoFaLogin", {
      method: "POST",
      credentials: "include",
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        loginToken: loginToken,
        otpCode: totp
      })
    });

    const data = await res.json();
    if (data.token) {
      updateUserData(data.token, username);
      return { success: true };
    } else {
      return { success: false };
    }
  };

  return (
    <BrowserRouter>
      <UserContext.Provider value={{
        token: token,
        username: username,
        setUserContext: updateUserData,
        login: login,
        verifyTOTP: verifyTOTP
      }}>
        <TrainProvider>
          <Header title="Zelezniski promet" />
          <div className="App pt-16">
            <Routes>
              <Route path="/" exact element={<Home />} />
              <Route path="/login" exact element={<Login />}></Route>
              <Route path="/register" element={<Register />}></Route>
              <Route path="/logout" element={<Logout />}></Route>
              <Route path="/map" element={<Map />}></Route>
              <Route path="/historyMap" element={<HistoryMap />}></Route>
              <Route path="/stats" element={<Stats />}></Route>
              <Route path="*" element={<div>404 - Page Not Found</div>} />
            </Routes>
          </div>
        </TrainProvider>
      </UserContext.Provider>
    </BrowserRouter>
  )
}

export default App;
