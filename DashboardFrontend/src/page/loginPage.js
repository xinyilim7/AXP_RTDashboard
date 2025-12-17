import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Eye, EyeOff } from "lucide-react";
import "../index.css";

export const Login = () => {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [showPassword, setShowPassword] = useState(false);
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

    const togglePasswordVisibility = () => {
        setShowPassword(!showPassword);
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
                    <div className="input-group password-group">
                        <label>Password</label>
                        <div className="password-input-wrapper">
                            <input
                                type={showPassword ? "text" : "password"}
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                required
                            />
                            <span
                                className="password-toggle-icon"
                                onClick={togglePasswordVisibility}
                            >
                {showPassword ? <EyeOff /> : <Eye />}
              </span>
                        </div>
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
