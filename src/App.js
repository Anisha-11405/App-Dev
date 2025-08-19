import React, { useState, useEffect, createContext, useContext } from "react";
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
import SetAvailability from "./pages/SetAvailability"; // Import the new component
import api from "./utils/api";
import "./App.css";

// Create User Context for role-based access
const UserContext = createContext();

export const useUser = () => {
  const context = useContext(UserContext);
  if (!context) {
    throw new Error("useUser must be used within a UserProvider");
  }
  return context;
};

// Role-based route protection component
const ProtectedRoute = ({ children, allowedRoles }) => {
  const { user } = useUser();
  
  if (!user) {
    return <Navigate to="/login" replace />;
  }
  
  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return (
      <div className="access-denied">
        <div className="access-denied-content">
          <h2>Access Denied</h2>
          <p>You don't have permission to access this page.</p>
          <p>Your role: <strong>{user.role}</strong></p>
          <p>Required roles: <strong>{allowedRoles.join(", ")}</strong></p>
          <button onClick={() => window.history.back()}>Go Back</button>
        </div>
      </div>
    );
  }
  
  return children;
};

export default function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [user, setUser] = useState(null);

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
            
            // Fetch user information
            const userResponse = await api.get("/auth/me");
            setUser(userResponse.data);
            setIsAuthenticated(true);
            console.log("User authenticated:", userResponse.data);
          } catch (error) {
            console.log("Token validation failed:", error.response?.status);
            localStorage.removeItem("token");
            localStorage.removeItem("isAuthenticated");
            setIsAuthenticated(false);
            setUser(null);
          }
        } else {
          localStorage.removeItem("token");
          localStorage.removeItem("isAuthenticated");
          setIsAuthenticated(false);
          setUser(null);
          console.log("No valid authentication found");
        }
      } catch (error) {
        console.error("Auth check error:", error);
        localStorage.removeItem("token");
        localStorage.removeItem("isAuthenticated");
        setIsAuthenticated(false);
        setUser(null);
      } finally {
        setIsLoading(false);
      }
    };

    checkAuthStatus();
  }, []);

  const handleLogin = async (token) => {
    console.log("Login handler called with token:", !!token);
    
    if (token) {
      localStorage.setItem("token", token);
      localStorage.setItem("isAuthenticated", "true");
      
      api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
      
      try {
        // Fetch user information after login
        const userResponse = await api.get("/auth/me");
        setUser(userResponse.data);
        setIsAuthenticated(true);
        console.log("Authentication state updated with user:", userResponse.data);
      } catch (error) {
        console.error("Failed to fetch user info after login:", error);
        setIsAuthenticated(true); // Still authenticate, but without user data
      }
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
    setUser(null);
    
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
    <UserContext.Provider value={{ user, setUser }}>
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
                  
                  {/* Patient and Admin can book appointments */}
                  <Route 
                    path="/book" 
                    element={
                      <ProtectedRoute allowedRoles={['PATIENT', 'ADMIN']}>
                        <BookAppointment />
                      </ProtectedRoute>
                    } 
                  />
                  
                  {/* All roles can view appointments (filtered by role in component) */}
                  <Route 
                    path="/appointments" 
                    element={
                      <ProtectedRoute allowedRoles={['PATIENT', 'DOCTOR', 'ADMIN']}>
                        <Appointments />
                      </ProtectedRoute>
                    } 
                  />
                  
                  {/* Patients and Admins can search doctors */}
                  <Route 
                    path="/doctors" 
                    element={
                      <ProtectedRoute allowedRoles={['PATIENT', 'ADMIN']}>
                        <Doctors />
                      </ProtectedRoute>
                    } 
                  />
                  
                  {/* Doctors and Admins can view patients */}
                  <Route 
                    path="/patients" 
                    element={
                      <ProtectedRoute allowedRoles={['DOCTOR', 'ADMIN']}>
                        <Patients />
                      </ProtectedRoute>
                    } 
                  />
                  
                  {/* Only Doctors can set availability */}
                  <Route 
                    path="/set-availability" 
                    element={
                      <ProtectedRoute allowedRoles={['DOCTOR']}>
                        <SetAvailability />
                      </ProtectedRoute>
                    } 
                  />
                  
                  {/* All authenticated users can manage their profile */}
                  <Route path="/profile" element={<Profile />} />
                  
                  {/* Redirect authenticated users away from auth pages */}
                  <Route path="/login" element={<Navigate to="/" replace />} />
                  <Route path="/register" element={<Navigate to="/" replace />} />
                  
                  {/* Catch all other routes */}
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
    </UserContext.Provider>
  );
}