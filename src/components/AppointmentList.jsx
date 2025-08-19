import React, { useState, useEffect } from 'react';
import api from '../utils/api';

export default function AppointmentsList() {
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchAppointments();
  }, []);

  const fetchAppointments = async () => {
    try {
      setLoading(true);
      setError('');
      const response = await api.get('/api/appointments');
      setAppointments(response.data);
    } catch (err) {
      let errorMessage = 'Failed to load appointments';
      if (err.response?.status === 401) {
        errorMessage = 'Please login again';
        localStorage.removeItem('token');
        window.location.href = '/login';
      } else if (err.response?.data?.error) {
        errorMessage = err.response.data.error;
      }
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString) => {
    try {
      return new Date(dateString).toLocaleDateString();
    } catch {
      return dateString;
    }
  };

  if (loading) {
    return <div className="loading">Loading appointments...</div>;
  }

  if (error) {
    return (
      <div className="error-container">
        <div className="error-message">{error}</div>
        <button onClick={fetchAppointments} className="btn-refresh">Retry</button>
      </div>
    );
  }

  return (
    <div className="appointments-container">
      <div className="header">
        <h1 className="title">Appointments</h1>
        <p className="subtitle">Search, sort, and browse all appointments</p>
        <button onClick={fetchAppointments} className="btn-refresh">Refresh</button>
      </div>

      {appointments.length === 0 ? (
        <div className="card no-appointments">No appointments found.</div>
      ) : (
        <div className="appointments-list">
          {appointments.map((appointment) => (
            <div key={appointment.id} className="card appointment-card">
              <div className="appointment-header">
                <h3>Appointment #{appointment.id}</h3>
                <span className={`status ${appointment.status?.toLowerCase()}`}>
                  {appointment.status}
                </span>
              </div>
              <div className="appointment-details">
                <p><strong>Patient:</strong> {appointment.patient?.name} (ID: {appointment.patient?.id})</p>
                <p><strong>Doctor:</strong> {appointment.doctor?.name} (ID: {appointment.doctor?.id})</p>
                <p><strong>Specialization:</strong> {appointment.doctor?.specialization}</p>
                <p><strong>Date:</strong> {formatDate(appointment.appointmentDate)}</p>
                <p><strong>Time:</strong> {appointment.appointmentTime}</p>
                <p><strong>Reason:</strong> {appointment.reason}</p>
                <p><strong>Created:</strong> {new Date(appointment.createdAt).toLocaleString()}</p>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
