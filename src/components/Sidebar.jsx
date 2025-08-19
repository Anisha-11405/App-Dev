import React from "react";
import { Link, useLocation } from "react-router-dom";
import { useUser } from "../App";
import "./Sidebar.css"

const Sidebar = () => {
  const location = useLocation();
  const { user } = useUser();

  const isActive = (path) => {
    return location.pathname === path ? "sidebar-link active" : "sidebar-link";
  };

  // Role-based menu items
  const getMenuItems = () => {
    const items = [];

    // Common items for all users
    items.push(
      { path: "/", label: "🏠 Dashboard", roles: ['PATIENT', 'DOCTOR', 'ADMIN'] },
      { path: "/appointments", label: "📅 Appointments", roles: ['PATIENT', 'DOCTOR', 'ADMIN'] },
      { path: "/profile", label: "👤 Profile", roles: ['PATIENT', 'DOCTOR', 'ADMIN'] }
    );

    // Patient-specific items
    if (user?.role === 'PATIENT') {
      items.splice(2, 0, 
        { path: "/book", label: "➕ Book Appointment", roles: ['PATIENT'] },
        { path: "/doctors", label: "👨‍⚕️ Find Doctors", roles: ['PATIENT'] }
      );
    }

    // Doctor-specific items
    if (user?.role === 'DOCTOR') {
      items.splice(2, 0,
        { path: "/patients", label: "👥 My Patients", roles: ['DOCTOR'] },
        { path: "/set-availability", label: "⏰ Set Availability", roles: ['DOCTOR'] }
      );
    }

    // Admin-specific items
    if (user?.role === 'ADMIN') {
      items.splice(2, 0,
        { path: "/book", label: "➕ Book Appointment", roles: ['ADMIN'] },
        { path: "/doctors", label: "👨‍⚕️ Manage Doctors", roles: ['ADMIN'] },
        { path: "/patients", label: "👥 Manage Patients", roles: ['ADMIN'] }
      );
    }

    return items.filter(item => item.roles.includes(user?.role));
  };

  return (
    <aside className="sidebar">
      <div className="sidebar-header">
        <h3>Navigation</h3>
        {user && (
          <div className="user-info">
            <div className="user-name">{user.name}</div>
            <div className="user-role badge badge-primary">{user.role}</div>
          </div>
        )}
      </div>
      
      <nav className="sidebar-nav">
        {getMenuItems().map((item) => (
          <Link key={item.path} to={item.path} className={isActive(item.path)}>
            {item.label}
          </Link>
        ))}
      </nav>
    </aside>
  );
};

export default Sidebar;