import React, { useState, useEffect } from "react";
import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import Navbar from "./components/Navbar";
import Sidebar from "./components/Sidebar";
import Home from "./pages/Home";
import BookAppointment from "./pages/BookAppointment";
import Appointments from "./pages/Appointments";
import Doctors from "./pages/Doctors";
import Patients from "./pages/Patients";
import Profile from "./pages/Profile";
import Login from "./pages/Login";
import Register from "./pages/Register";
import api from "./utils/api";
import "./App.css";

export default function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const checkAuthStatus = async () => {
      try {
        const token = localStorage.getItem("token");
        const isLoggedIn = localStorage.getItem("isAuthenticated");
        
        console.log("🔍 Checking auth status:", { 
          token: !!token, 
          isLoggedIn,
          tokenLength: token?.length 
        });
        
        if (token && token.trim() !== "" && isLoggedIn === "true") {
          try {
            api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
            
            setIsAuthenticated(true);
            console.log("User authenticated with valid token");
          } catch (error) {
            console.log("Token validation failed:", error.response?.status);
            localStorage.removeItem("token");
            localStorage.removeItem("isAuthenticated");
            setIsAuthenticated(false);
          }
        } else {
          localStorage.removeItem("token");
          localStorage.removeItem("isAuthenticated");
          setIsAuthenticated(false);
          console.log("No valid authentication found");
        }
      } catch (error) {
        console.error("Auth check error:", error);
        localStorage.removeItem("token");
        localStorage.removeItem("isAuthenticated");
        setIsAuthenticated(false);
      } finally {
        setIsLoading(false);
      }
    };

    checkAuthStatus();
  }, []);

  const handleLogin = (token) => {
    console.log("Login handler called with token:", !!token);
    
    if (token) {
      localStorage.setItem("token", token);
      localStorage.setItem("isAuthenticated", "true");
      
      api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
      
      setIsAuthenticated(true);
      console.log("Authentication state updated");
    } else {
      console.error("No token provided to login handler");
    }
  };

  const handleLogout = () => {
    console.log("Logging out...");
    
    localStorage.removeItem("isAuthenticated");
    localStorage.removeItem("token");
    
    delete api.defaults.headers.common['Authorization'];
    
    setIsAuthenticated(false);
    
    console.log("Logout complete");
  };

  if (isLoading) {
    return (
      <div className="loading-screen">
        <div className="loading-spinner"></div>
        <p>Loading...</p>
      </div>
    );
  }

  return (
    <Router>
      {isAuthenticated ? (
        <div className="app-layout">
          <Navbar onLogout={handleLogout} />
          <div className="app-body">
            <Sidebar />
            <main className="main-content">
              <Routes>
                <Route path="/" element={<Home />} />
                <Route path="/dashboard" element={<Home />} />
                
                <Route path="/book" element={<BookAppointment />} />
                <Route path="/appointments" element={<Appointments />} />
                <Route path="/doctors" element={<Doctors />} />
                <Route path="/patients" element={<Patients />} />
                <Route path="/profile" element={<Profile />} />
                
                <Route path="/login" element={<Navigate to="/" replace />} />
                <Route path="/register" element={<Navigate to="/" replace />} />
                
                <Route path="*" element={<Navigate to="/" replace />} />
              </Routes>
            </main>
          </div>
        </div>
      ) : (
        <Routes>
          <Route path="/login" element={<Login onLogin={handleLogin} />} />
          <Route path="/register" element={<Register onLogin={handleLogin} />} />
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      )}
    </Router>
  );
}