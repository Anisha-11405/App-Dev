import React, { useState, useEffect } from "react";
import api from "../utils/api";

export default function Appointments() {
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [filter, setFilter] = useState("all");

  useEffect(() => {
    loadAppointments();
  }, []);

  const loadAppointments = async () => {
    try {
      setLoading(true);
      const response = await api.get('/api/appointments');
      setAppointments(response.data || []);
      setError("");
    } catch (err) {
      console.error('Failed to load appointments:', err);
      setError(err.response?.data?.error || "Failed to load appointments");
    } finally {
      setLoading(false);
    }
  };

  const updateAppointmentStatus = async (appointmentId, newStatus) => {
    try {
      await api.patch(`/api/appointments/${appointmentId}/status`, {
        status: newStatus
      });
      loadAppointments();
    } catch (err) {
      alert("Failed to update appointment status");
    }
  };

  const cancelAppointment = async (appointmentId) => {
    if (window.confirm("Are you sure you want to cancel this appointment?")) {
      try {
        await api.patch(`/api/appointments/${appointmentId}/cancel`);
        loadAppointments();
      } catch (err) {
        alert("Failed to cancel appointment");
      }
    }
  };

  const filteredAppointments = appointments.filter(appointment => {
    if (filter === "all") return true;
    return appointment.status?.toLowerCase() === filter.toLowerCase();
  });

  const getStatusBadge = (status) => {
    const statusClasses = {
      PENDING: "badge badge-warning",
      CONFIRMED: "badge badge-success",
      CANCELLED: "badge badge-danger",
      COMPLETED: "badge badge-info"
    };
    return statusClasses[status] || "badge badge-secondary";
  };

  if (loading) {
    return (
      <div className="container">
        <div className="header">
          <h1>Appointments</h1>
        </div>
        <div className="card">Loading appointments...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container">
        <div className="header">
          <h1>Appointments</h1>
        </div>
        <div className="card feedback error">
          {error}
          <button 
            onClick={loadAppointments}
            style={{ marginLeft: '10px' }}
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="container">
      <div className="header">
        <h1>Appointments</h1>
        <div className="subtle">View and manage all appointments</div>
      </div>

      <div className="card" style={{ marginBottom: '20px' }}>
        <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
          <span>Filter by status:</span>
          <select 
            value={filter} 
            onChange={(e) => setFilter(e.target.value)}
            style={{ padding: '5px' }}
          >
            <option value="all">All Appointments</option>
            <option value="pending">Pending</option>
            <option value="confirmed">Confirmed</option>
            <option value="completed">Completed</option>
            <option value="cancelled">Cancelled</option>
          </select>
          <button onClick={loadAppointments} style={{ marginLeft: '10px' }}>
            🔄 Refresh
          </button>
        </div>
      </div>

      <div className="card">
        <table className="table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Patient</th>
              <th>Doctor</th>
              <th>Date</th>
              <th>Time</th>
              <th>Reason</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {filteredAppointments.map((appointment) => (
              <tr key={appointment.id}>
                <td>{appointment.id}</td>
                <td>
                  {appointment.patient?.name || 'Unknown Patient'}
                  <div className="subtle small">
                    ID: {appointment.patient?.id}
                  </div>
                </td>
                <td>
                  Dr. {appointment.doctor?.name || 'Unknown Doctor'}
                  <div className="subtle small">
                    {appointment.doctor?.specialization || 'General'}
                  </div>
                </td>
                <td>{appointment.appointmentDate}</td>
                <td>{appointment.appointmentTime}</td>
                <td>{appointment.reason}</td>
                <td>
                  <span className={getStatusBadge(appointment.status)}>
                    {appointment.status || 'PENDING'}
                  </span>
                </td>
                <td>
                  <div style={{ display: 'flex', gap: '5px', flexWrap: 'wrap' }}>
                    {appointment.status === 'PENDING' && (
                      <>
                        <button 
                          onClick={() => updateAppointmentStatus(appointment.id, 'CONFIRMED')}
                          className="btn btn-sm btn-success"
                        >
                          Confirm
                        </button>
                        <button 
                          onClick={() => cancelAppointment(appointment.id)}
                          className="btn btn-sm btn-danger"
                        >
                          Cancel
                        </button>
                      </>
                    )}
                    {appointment.status === 'CONFIRMED' && (
                      <button 
                        onClick={() => updateAppointmentStatus(appointment.id, 'COMPLETED')}
                        className="btn btn-sm btn-info"
                      >
                        Complete
                      </button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
            {filteredAppointments.length === 0 && (
              <tr>
                <td colSpan="8" className="subtle" style={{ textAlign: 'center', padding: '20px' }}>
                  {filter === "all" ? "No appointments found." : `No ${filter} appointments found.`}
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}