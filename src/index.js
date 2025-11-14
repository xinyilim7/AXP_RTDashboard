import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import './index.css';           // Tailwind + your custom CSS
import './component/dashboardLayout.css'; // optional extra styles



const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
