import React, { useState, useEffect } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import api from "../utils/api";
import "./Login.css";

export default function Login({ onLogin }) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    if (location.state?.message) {
      console.log(location.state.message);
    }
  }, [location]);


  const handleSubmit = async (e) => {
  console.log("Form submitted!");
  e.preventDefault();
  setLoading(true);
  setError("");

  console.log("Email:", email);
  console.log("Password:", password ? "***" : "empty");
  console.log("API base URL:", api.defaults.baseURL);

  try {
    console.log("About to make API call to /auth/login");
    const res = await api.post("/auth/login", { email, password });
    console.log("API Response received:", res.data);
    
    const token = res.data?.token || res.data?.jwt || res.data?.accessToken;
    
    if (token) {
      console.log("Login successful, token received:", token.substring(0, 20) + "...");
      localStorage.setItem("token", token);
      localStorage.setItem("isAuthenticated", "true");
      
      if (onLogin) {
        onLogin(token);
      }
      
      navigate("/", { replace: true });
    } else {
      console.log("No token in response:", res.data);
      setError("Login successful but no token received");
    }
  } catch (err) {
    console.error("Login error:", err);
    console.error("Error response:", err.response);
    const msg = err.response?.data?.message || 
                err.response?.data || 
                err.message || 
                "Login failed";
    setError(typeof msg === "string" ? msg : "Invalid credentials");
  } finally {
    setLoading(false);
  }
};

  return (
    <div className="auth-container">
      <div className="auth-wrapper">
        <form className="auth-form" onSubmit={handleSubmit}>
          <div className="auth-header">
            <h2>Welcome back</h2>
            <p>Sign in to your account</p>
          </div>

          {error && <div className="error-message">{error}</div>}

          <div className="form-group">
            <label className="form-label" htmlFor="email">
              Email Address
            </label>
            <input
              id="email"
              type="email"
              className="form-input"
              placeholder="you@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="password">
              Password
            </label>
            <input
              id="password"
              type="password"
              className="form-input"
              placeholder="Enter your password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>

          <button className="auth-btn" disabled={loading} type="submit">
            {loading ? (
              <span className="btn-loading">Signing in...</span>
            ) : (
              "Sign In"
            )}
          </button>

          <div className="auth-footer">
            <p>
              Don't have an account?{" "}
              <Link to="/register" className="auth-link">
                Create one here
              </Link>
            </p>
          </div>
        </form>
      </div>
    </div>
  );
}