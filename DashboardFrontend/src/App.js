import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'; // Added 'Navigate'
import { Provider } from 'react-redux';
import { DashboardPage } from './page/dashboardPage';
import { Login } from './page/loginPage';
import store from './state/store';

const ProtectedRoute = ({ children }) => {
    const token = localStorage.getItem('authToken');

    if (!token) {
        return <Navigate to="/login" replace />;
    }
    return children;
};

function App() {
    return (
        <Provider store={store}>
            <Router>
                <div className="min-h-screen bg-gray-100">
                    <Routes>
                        <Route path="/login" element={<Login />} />
                        <Route
                            path="/"
                            element={
                                <ProtectedRoute>
                                    <DashboardPage />
                                </ProtectedRoute>
                            }
                        />
                        <Route path="/dashboard" element={<Navigate to="/" replace />} />
                        <Route path="*" element={<div className="p-8 text-center text-xl">404 - Not Found</div>} />
                    </Routes>
                </div>
            </Router>
        </Provider>
    );
}

export default App;