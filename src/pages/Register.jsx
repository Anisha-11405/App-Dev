import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import api from "../utils/api";
import "./Login.css";

export default function Register({ onLogin }) {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [role, setRole] = useState("PATIENT");
  const [phoneNumber, setPhoneNumber] = useState("");
  const [dateOfBirth, setDateOfBirth] = useState("");
  const [specialization, setSpecialization] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");

    if (password !== confirmPassword) {
      setError("Passwords don't match");
      setLoading(false);
      return;
    }

    if (password.length < 6) {
      setError("Password must be at least 6 characters long");
      setLoading(false);
      return;
    }

    if (!phoneNumber.trim()) {
      setError("Phone number is required");
      setLoading(false);
      return;
    }

    if (role === "PATIENT" && !dateOfBirth) {
      setError("Date of birth is required for patients");
      setLoading(false);
      return;
    }

    if (role === "DOCTOR" && !specialization.trim()) {
      setError("Specialization is required for doctors");
      setLoading(false);
      return;
    }

    try {
      const requestData = { 
        name, 
        email, 
        password, 
        role,
        phoneNumber
      };
      
      if (role === "PATIENT") {
        requestData.dateOfBirth = dateOfBirth;
      } else if (role === "DOCTOR") {
        requestData.specialization = specialization;
      }
      // Admin doesn't need additional fields beyond phoneNumber

      const res = await api.post("/auth/register", requestData);
      
      // Check if response contains token (auto-login) or just success message
      const token = res.data?.token || res.data?.jwt || res.data?.accessToken;
      if (token) {
        localStorage.setItem("token", token);
        if (onLogin) onLogin(token);
        navigate("/", { replace: true });
      } else {
        navigate("/login", { 
          replace: true, 
          state: { message: "Registration successful! Please login." }
        });
      }
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data || err.message || "Registration failed";
      setError(typeof msg === "string" ? msg : "Registration failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-wrapper">
        <form className="auth-form" onSubmit={handleSubmit}>
          <div className="auth-header">
            <h2>Create your account</h2>
            <p>Join our Healthcare platform</p>
          </div>

          {error && <div className="error-message">{error}</div>}

          <div className="form-group">
            <label className="form-label" htmlFor="name">
              Full Name
            </label>
            <input
              id="name"
              type="text"
              className="form-input"
              placeholder="Enter your full name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
            />
          </div>

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
            <label className="form-label" htmlFor="role">
              I am a
            </label>
            <select
              id="role"
              className="form-input form-select"
              value={role}
              onChange={(e) => {
                setRole(e.target.value);
                // Reset role-specific fields when changing role
                setDateOfBirth("");
                setSpecialization("");
              }}
            >
              <option value="PATIENT">Patient</option>
              <option value="DOCTOR">Doctor</option>
              <option value="ADMIN">Administrator</option>
            </select>
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="phoneNumber">
              Phone Number
            </label>
            <input
              id="phoneNumber"
              type="tel"
              className="form-input"
              placeholder="Enter your phone number"
              value={phoneNumber}
              onChange={(e) => setPhoneNumber(e.target.value)}
              required
            />
          </div>

          {role === "PATIENT" && (
            <div className="form-group">
              <label className="form-label" htmlFor="dateOfBirth">
                Date of Birth
              </label>
              <input
                id="dateOfBirth"
                type="date"
                className="form-input"
                value={dateOfBirth}
                onChange={(e) => setDateOfBirth(e.target.value)}
                required
              />
            </div>
          )}

          {role === "DOCTOR" && (
            <div className="form-group">
              <label className="form-label" htmlFor="specialization">
                Specialization
              </label>
              <input
                id="specialization"
                type="text"
                className="form-input"
                placeholder="e.g., Cardiology, Neurology, etc."
                value={specialization}
                onChange={(e) => setSpecialization(e.target.value)}
                required
              />
            </div>
          )}

          {role === "ADMIN" && (
            <div className="form-group">
              <div style={{
                padding: '12px',
                backgroundColor: '#e3f2fd',
                border: '1px solid #1976d2',
                borderRadius: '6px',
                fontSize: '14px',
                color: '#1565c0'
              }}>
                <strong>Administrator Account</strong><br />
                You will have full access to manage patients, doctors, and appointments.
              </div>
            </div>
          )}

          <div className="form-group">
            <label className="form-label" htmlFor="password">
              Password
            </label>
            <input
              id="password"
              type="password"
              className="form-input"
              placeholder="At least 6 characters"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              minLength="6"
            />
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="confirmPassword">
              Confirm Password
            </label>
            <input
              id="confirmPassword"
              type="password"
              className="form-input"
              placeholder="Confirm your password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
            />
          </div>

          <button className="auth-btn" disabled={loading} type="submit">
            {loading ? (
              <span className="btn-loading">Creating Account...</span>
            ) : (
              "Create Account"
            )}
          </button>

          <div className="auth-footer">
            <p>
              Already have an account?{" "}
              <Link to="/login" className="auth-link">
                Sign in here
              </Link>
            </p>
          </div>
        </form>
      </div>
    </div>
  );
}