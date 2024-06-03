import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App.jsx'
import './index.css'

ReactDOM.createRoot(document.getElementById('root')).render(
  // remove <React.StrictMode> to not call API twice
  <React.StrictMode>
    <App />
  </React.StrictMode>,
)
