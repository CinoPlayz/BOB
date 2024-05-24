import { useEffect, useState } from 'react'
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { UserContext } from "./UserContext.jsx";
import Register from "./components/Register.jsx";
import Header from "./components/Header.jsx";
import Login from './components/Login.jsx';
import Logout from "./components/Logout.jsx";
import Map from "./components/Map.jsx";
import Tmp from "./components/Tmp.jsx";

import './App.css'

function App() {
  const [token, setToken] = useState(localStorage.getItem('token') || null);

  const updateUserData = (token) => {//če token obstaja ga shrani v localstorage
    if (token) {
      localStorage.setItem("token", token);
    } else {
      localStorage.removeItem("token");
    }
    setToken(token);
  };

  useEffect(() => {//preverja ali je token prisoten v localstorage ob inicializaciji
    const savedToken = localStorage.getItem('token');
    if (savedToken) {
      setToken(savedToken);
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
    })
    const data = await res.json();
    if (data.token) {
      updateUserData(data.token);
      return true;
    } else {
      console.log("falsee");
      return false;
    }
  };

  return (
    <BrowserRouter>
      <UserContext.Provider value={{
        token: token,
        setUserContext: updateUserData,
        login: login
      }}>

        <Header title="Zelezniski promet" />
        <div className="App pt-16">
          <Routes>
            <Route path="/login" exact element={<Login />}></Route>
            <Route path="/register" element={<Register />}></Route>
            <Route path="/logout" element={<Logout />}></Route>
            <Route path="/map" element={<Map />}></Route>
            <Route path="/tmp" element={<Tmp />}></Route>
          </Routes>
        </div>
      </UserContext.Provider>
    </BrowserRouter>
  )
}

export default App;
