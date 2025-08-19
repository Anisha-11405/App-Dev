import React, { useState, useEffect } from "react";
import { useUser } from "../App";
import api from "../utils/api";

export default function Appointments() {
  const { user } = useUser();
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [filter, setFilter] = useState("all");

  useEffect(() => {
    loadAppointments();
  }, []);

  // const loadAppointments = async () => {
  //   try {
  //     setLoading(true);
  //     // Use role-based endpoint
  //     const endpoint = user?.role === 'ADMIN' ? '/api/appointments' : '/api/appointments/my';
  //     const response = await api.get(endpoint);
  //     setAppointments(response.data || []);
  //     setError("");
  //   } catch (err) {
  //     console.error('Failed to load appointments:', err);
  //     setError(err.response?.data?.error || "Failed to load appointments");
  //   } finally {
  //     setLoading(false);
  //   }
  // };

  const loadAppointments = async () => {
  try {
    setLoading(true);
    let endpoint = "/api/appointments";

    if (user?.role === "PATIENT") {
      endpoint = "/api/appointments/my/patient";
    } else if (user?.role === "DOCTOR") {
      endpoint = "/api/appointments/my/doctor";
    } else if (user?.role === "ADMIN") {
      endpoint = "/api/appointments";
    }

    const response = await api.get(endpoint);
    setAppointments(response.data || []);
    setError("");
  } catch (err) {
    console.error("Failed to load appointments:", err);
    setError(err.response?.data?.error || "Failed to load appointments");
  } finally {
    setLoading(false);
  }
};


  const approveAppointment = async (appointmentId) => {
    if (window.confirm("Are you sure you want to approve this appointment?")) {
      try {
        await api.patch(`/api/appointments/${appointmentId}/approve`);
        loadAppointments();
      } catch (err) {
        alert("Failed to approve appointment: " + (err.response?.data || err.message));
      }
    }
  };

  const rejectAppointment = async (appointmentId) => {
    const reason = prompt("Please provide a reason for rejection (optional):");
    if (reason !== null) { // User didn't cancel the prompt
      try {
        await api.patch(`/api/appointments/${appointmentId}/reject`, {
          reason: reason || "No reason provided"
        });
        loadAppointments();
      } catch (err) {
        alert("Failed to reject appointment: " + (err.response?.data || err.message));
      }
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
      SCHEDULED: "badge badge-warning",
      PENDING: "badge badge-warning", 
      CONFIRMED: "badge badge-success",
      CANCELLED: "badge badge-danger",
      COMPLETED: "badge badge-info"
    };
    return statusClasses[status] || "badge badge-secondary";
  };

  const getPageTitle = () => {
    switch (user?.role) {
      case 'ADMIN':
        return 'All Appointments';
      case 'DOCTOR':
        return 'My Appointments';
      case 'PATIENT':
        return 'My Appointments';
      default:
        return 'Appointments';
    }
  };

  const getPageSubtitle = () => {
    switch (user?.role) {
      case 'ADMIN':
        return 'Manage all appointments in the system';
      case 'DOCTOR':
        return 'View and manage your patient appointments';
      case 'PATIENT':
        return 'View your scheduled appointments';
      default:
        return 'View and manage appointments';
    }
  };

  const canApproveReject = (appointment) => {
    return user?.role === 'DOCTOR' && 
           (appointment.status === 'SCHEDULED' || appointment.status === 'PENDING');
  };

  const canComplete = (appointment) => {
    return (user?.role === 'DOCTOR' || user?.role === 'ADMIN') && 
           appointment.status === 'CONFIRMED';
  };

  const canCancel = (appointment) => {
    return appointment.status !== 'COMPLETED' && appointment.status !== 'CANCELLED';
  };

  if (loading) {
    return (
      <div className="container">
        <div className="header">
          <h1>{getPageTitle()}</h1>
        </div>
        <div className="card">Loading appointments...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container">
        <div className="header">
          <h1>{getPageTitle()}</h1>
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
        <h1>{getPageTitle()}</h1>
        <div className="subtle">{getPageSubtitle()}</div>
        {user?.role === 'DOCTOR' && (
          <div className="subtle" style={{ marginTop: '5px' }}>
            <strong>Note:</strong> New appointments will appear here for your approval
          </div>
        )}
      </div>

      <div className="card" style={{ marginBottom: '20px' }}>
        <div style={{ display: 'flex', gap: '10px', alignItems: 'center', flexWrap: 'wrap' }}>
          <span>Filter by status:</span>
          <select 
            value={filter} 
            onChange={(e) => setFilter(e.target.value)}
            style={{ padding: '5px' }}
          >
            <option value="all">All Appointments</option>
            <option value="scheduled">Scheduled (Pending Approval)</option>
            <option value="confirmed">Confirmed</option>
            <option value="completed">Completed</option>
            <option value="cancelled">Cancelled</option>
          </select>
          <button onClick={loadAppointments} style={{ marginLeft: '10px' }}>
            🔄 Refresh
          </button>
          <div style={{ marginLeft: 'auto', fontSize: '0.9em', color: '#666' }}>
            Showing {filteredAppointments.length} of {appointments.length} appointments
          </div>
        </div>
      </div>

      <div className="card">
        <table className="table">
          <thead>
            <tr>
              <th>ID</th>
              {user?.role === 'ADMIN' && <th>Patient</th>}
              {user?.role !== 'DOCTOR' && <th>Doctor</th>}
              {user?.role === 'DOCTOR' && <th>Patient</th>}
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
                
                {/* Show patient info for admin and doctor */}
                {(user?.role === 'ADMIN' || user?.role === 'DOCTOR') && (
                  <td>
                    {appointment.patient?.name || 'Unknown Patient'}
                    <div className="subtle small">
                      ID: {appointment.patient?.id}
                    </div>
                  </td>
                )}
                
                {/* Show doctor info for admin and patient */}
                {user?.role !== 'DOCTOR' && (
                  <td>
                    Dr. {appointment.doctor?.name || 'Unknown Doctor'}
                    <div className="subtle small">
                      {appointment.doctor?.specialization || 'General'}
                    </div>
                  </td>
                )}
                
                <td>{appointment.appointmentDate}</td>
                <td>{appointment.appointmentTime}</td>
                <td>
                  <span title={appointment.reason}>
                    {appointment.reason?.length > 30 
                      ? appointment.reason.substring(0, 30) + '...' 
                      : appointment.reason}
                  </span>
                </td>
                <td>
                  <span className={getStatusBadge(appointment.status)}>
                    {appointment.status === 'SCHEDULED' ? 'PENDING APPROVAL' : appointment.status || 'PENDING'}
                  </span>
                </td>
                <td>
                  <div style={{ display: 'flex', gap: '5px', flexWrap: 'wrap' }}>
                    
                    {/* Doctor approval/rejection buttons */}
                    {canApproveReject(appointment) && (
                      <>
                        <button 
                          onClick={() => approveAppointment(appointment.id)}
                          className="btn btn-sm btn-success"
                          title="Approve this appointment"
                        >
                          ✓ Approve
                        </button>
                        <button 
                          onClick={() => rejectAppointment(appointment.id)}
                          className="btn btn-sm btn-danger"
                          title="Reject this appointment"
                        >
                          ✗ Reject
                        </button>
                      </>
                    )}
                    
                    {/* Complete appointment button */}
                    {canComplete(appointment) && (
                      <button 
                        onClick={() => updateAppointmentStatus(appointment.id, 'COMPLETED')}
                        className="btn btn-sm btn-info"
                        title="Mark as completed"
                      >
                        Complete
                      </button>
                    )}
                    
                    {/* Cancel appointment button */}
                    {canCancel(appointment) && (
                      <button 
                        onClick={() => cancelAppointment(appointment.id)}
                        className="btn btn-sm btn-warning"
                        title="Cancel this appointment"
                      >
                        Cancel
                      </button>
                    )}
                    
                    {/* No actions available */}
                    {!canApproveReject(appointment) && !canComplete(appointment) && !canCancel(appointment) && (
                      <span className="subtle small">No actions</span>
                    )}
                  </div>
                </td>
              </tr>
            ))}
            {filteredAppointments.length === 0 && (
              <tr>
                <td colSpan="8" className="subtle" style={{ textAlign: 'center', padding: '20px' }}>
                  {filter === "all" ? (
                    user?.role === 'DOCTOR' 
                      ? "No appointments found. New patient bookings will appear here for your approval."
                      : "No appointments found."
                  ) : (
                    `No ${filter} appointments found.`
                  )}
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
      
      {/* Legend for doctors */}
      {user?.role === 'DOCTOR' && (
        <div className="card" style={{ marginTop: '20px', backgroundColor: '#f8f9fa' }}>
          <h3 style={{ marginBottom: '10px', fontSize: '1.1em' }}>Appointment Workflow:</h3>
          <div style={{ display: 'flex', gap: '20px', flexWrap: 'wrap', fontSize: '0.9em' }}>
            <div><span className="badge badge-warning">PENDING APPROVAL</span> → Newly booked appointments</div>
            <div><span className="badge badge-success">CONFIRMED</span> → Approved by you</div>
            <div><span className="badge badge-info">COMPLETED</span> → Consultation finished</div>
            <div><span className="badge badge-danger">CANCELLED</span> → Rejected or cancelled</div>
          </div>
        </div>
      )}
    </div>
  );
}