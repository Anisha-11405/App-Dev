import React from "react";
import { NavLink } from "react-router-dom";

const Sidebar = () => {
  return (
    <aside className="sidebar">
      <div className="sidebar-header">
        <h3>Navigation</h3>
      </div>
      
      <nav className="sidebar-nav">
        <NavLink 
          to="/" 
          className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
        >
          <span className="sidebar-icon">🏠</span>
          Dashboard
        </NavLink>
        
        <NavLink 
          to="/book" 
          className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
        >
          <span className="sidebar-icon">📅</span>
          Book Appointment
        </NavLink>
        
        <NavLink 
          to="/appointments" 
          className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
        >
          <span className="sidebar-icon">📋</span>
          View Appointments
        </NavLink>
        
        <NavLink 
          to="/patients" 
          className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
        >
          <span className="sidebar-icon">👥</span>
          Patients
        </NavLink>
        
        <NavLink 
          to="/doctors" 
          className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
        >
          <span className="sidebar-icon">👨‍⚕️</span>
          Doctors
        </NavLink>
        
        <NavLink 
          to="/profile" 
          className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
        >
          <span className="sidebar-icon">👤</span>
          Profile
        </NavLink>
      </nav>
    </aside>
  );
};

export default Sidebar;