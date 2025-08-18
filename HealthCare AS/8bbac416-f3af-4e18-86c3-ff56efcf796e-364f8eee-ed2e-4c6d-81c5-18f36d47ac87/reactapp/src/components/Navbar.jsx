import React from "react";
import { Link } from "react-router-dom";

const Navbar = ({ onLogout }) => {
  const handleLogout = () => {
    if (onLogout) {
      onLogout();
    }
  };

  return (
    <nav className="navbar">
      <div className="navbar-brand">
        <div className="logo">HC</div>
        <span className="brand-text">Healthcare App</span>
      </div>
      
      <div className="navbar-nav">
        <Link to="/" className="nav-link">Home</Link>
        <Link to="/appointments" className="nav-link">Appointments</Link>
        <Link to="/patients" className="nav-link">Patients</Link>
        
        <button className="logout-btn" onClick={handleLogout}>
          Logout
        </button>
      </div>
    </nav>
  );
};

export default Navbar;