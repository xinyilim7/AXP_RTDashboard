import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import "../index.css";

export const Login = () => {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const navigate = useNavigate();

    const ADMIN_USERNAME = process.env.REACT_APP_ADMIN_USERNAME;
    const ADMIN_PASSWORD = process.env.REACT_APP_ADMIN_PASSWORD;

    const handleLogin = async (e) => {
        e.preventDefault();
        setError("");

        try {
            if (username === ADMIN_USERNAME && password === ADMIN_PASSWORD) {
                localStorage.setItem("authToken", "fake-jwt-token-123");
                navigate("/dashboard");
            } else {
                setError("Invalid username or password");
            }
        } catch (err) {
            setError("Something went wrong!");
        }
    };

    return (
        <div className="login-container">
            <div className="login-box">
                <h2>AXP Dashboard Login</h2>
                <form onSubmit={handleLogin}>
                    <div className="input-group">
                        <label>Username</label>
                        <input
                            type="text"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            required
                        />
                    </div>
                    <div className="input-group">
                        <label>Password</label>
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                        />
                    </div>
                    {error && <p className="error-msg">{error}</p>}
                    <button type="submit" className="login-btn">
                        LOGIN
                    </button>
                </form>
            </div>
        </div>
    );
};
