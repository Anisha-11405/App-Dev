import React from 'react';

const Home = () => {
  return (
    <div className="dashboard-container">
      <div className="dashboard-header">
        <h1>Dashboard</h1>
        <p>Welcome to your Healthcare Management System</p>
      </div>

      <div className="dashboard-grid">
        <div className="dashboard-card">
          <div className="card-icon">📅</div>
          <div className="card-content">
            <h3>Total Appointments</h3>
            <div className="card-number">24</div>
            <p>This month</p>
          </div>
        </div>

        <div className="dashboard-card">
          <div className="card-icon">👥</div>
          <div className="card-content">
            <h3>Active Patients</h3>
            <div className="card-number">156</div>
            <p>Registered</p>
          </div>
        </div>

        <div className="dashboard-card">
          <div className="card-icon">👨‍⚕️</div>
          <div className="card-content">
            <h3>Available Doctors</h3>
            <div className="card-number">12</div>
            <p>On duty today</p>
          </div>
        </div>

        <div className="dashboard-card">
          <div className="card-icon">⏰</div>
          <div className="card-content">
            <h3>Pending Appointments</h3>
            <div className="card-number">8</div>
            <p>Need confirmation</p>
          </div>
        </div>
      </div>

      <div className="recent-activity">
        <h2>Recent Activity</h2>
        <div className="activity-list">
          <div className="activity-item">
            <div className="activity-icon">📅</div>
            <div className="activity-details">
              <p><strong>New appointment booked</strong></p>
              <span>Patient ID: P001 with Dr. Smith - 2 hours ago</span>
            </div>
          </div>
          <div className="activity-item">
            <div className="activity-icon">👤</div>
            <div className="activity-details">
              <p><strong>New patient registered</strong></p>
              <span>Patient ID: P156 - 4 hours ago</span>
            </div>
          </div>
          <div className="activity-item">
            <div className="activity-icon"></div>
            <div className="activity-details">
              <p><strong>Appointment completed</strong></p>
              <span>Patient ID: P045 with Dr. Jones - 6 hours ago</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Home;