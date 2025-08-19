import React, { useState, useEffect } from "react";
import { useUser } from "../App";
import api from "../utils/api";

const SetAvailability = () => {
  const { user } = useUser();
  const [availabilitySlots, setAvailabilitySlots] = useState([]);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [appointments, setAppointments] = useState([]);
  const [loadingAppointments, setLoadingAppointments] = useState(true);

  // Default time slots
  const timeSlots = [
    "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
    "12:00", "12:30", "14:00", "14:30", "15:00", "15:30",
    "16:00", "16:30", "17:00", "17:30", "18:00"
  ];

  const daysOfWeek = [
    { key: 'monday', label: 'Monday' },
    { key: 'tuesday', label: 'Tuesday' },
    { key: 'wednesday', label: 'Wednesday' },
    { key: 'thursday', label: 'Thursday' },
    { key: 'friday', label: 'Friday' },
    { key: 'saturday', label: 'Saturday' },
    { key: 'sunday', label: 'Sunday' }
  ];

  useEffect(() => {
    loadMyAppointments();
    initializeAvailability();
  }, []);

  const initializeAvailability = () => {
    // Initialize with default availability (empty for now)
    const defaultSlots = daysOfWeek.map(day => ({
      day: day.key,
      dayLabel: day.label,
      timeSlots: [],
      isAvailable: false
    }));
    setAvailabilitySlots(defaultSlots);
  };

  const loadMyAppointments = async () => {
    try {
      setLoadingAppointments(true);
      const response = await api.get('/api/appointments/my-appointments');
      setAppointments(response.data || []);
    } catch (error) {
      console.error('Failed to load appointments:', error);
    } finally {
      setLoadingAppointments(false);
    }
  };

  const toggleDayAvailability = (dayKey) => {
    setAvailabilitySlots(prev => 
      prev.map(slot => 
        slot.day === dayKey 
          ? { 
              ...slot, 
              isAvailable: !slot.isAvailable,
              timeSlots: !slot.isAvailable ? [] : slot.timeSlots
            }
          : slot
      )
    );
  };

  const toggleTimeSlot = (dayKey, timeSlot) => {
    setAvailabilitySlots(prev => 
      prev.map(slot => {
        if (slot.day === dayKey) {
          const newTimeSlots = slot.timeSlots.includes(timeSlot)
            ? slot.timeSlots.filter(t => t !== timeSlot)
            : [...slot.timeSlots, timeSlot].sort();
          
          return {
            ...slot,
            timeSlots: newTimeSlots,
            isAvailable: newTimeSlots.length > 0
          };
        }
        return slot;
      })
    );
  };

  const handleQuickSet = (dayKey, type) => {
    let selectedSlots = [];
    
    switch(type) {
      case 'morning':
        selectedSlots = timeSlots.filter(slot => {
          const hour = parseInt(slot.split(':')[0]);
          return hour >= 9 && hour < 12;
        });
        break;
      case 'afternoon':
        selectedSlots = timeSlots.filter(slot => {
          const hour = parseInt(slot.split(':')[0]);
          return hour >= 14 && hour < 17;
        });
        break;
      case 'evening':
        selectedSlots = timeSlots.filter(slot => {
          const hour = parseInt(slot.split(':')[0]);
          return hour >= 17;
        });
        break;
      case 'all':
        selectedSlots = [...timeSlots];
        break;
      default:
        selectedSlots = [];
    }

    setAvailabilitySlots(prev => 
      prev.map(slot => 
        slot.day === dayKey 
          ? { 
              ...slot, 
              timeSlots: selectedSlots,
              isAvailable: selectedSlots.length > 0
            }
          : slot
      )
    );
  };

  const saveAvailability = async () => {
    try {
      setSaving(true);
      
      const availabilityData = {
        doctorId: user.id,
        availability: availabilitySlots.filter(slot => slot.isAvailable && slot.timeSlots.length > 0)
      };

      await api.post(`/api/doctors/${user.id}/availability`, availabilityData);
      alert("Availability updated successfully!");
      
    } catch (error) {
      console.error('Failed to save availability:', error);
      alert("Failed to save availability. Please try again.");
    } finally {
      setSaving(false);
    }
  };

  const handleAppointmentAction = async (appointmentId, action) => {
    try {
      const endpoint = action === 'accept' 
        ? `/api/appointments/${appointmentId}/confirm`
        : `/api/appointments/${appointmentId}/cancel`;
      
      await api.patch(endpoint);
      loadMyAppointments();
      alert(`Appointment ${action}ed successfully!`);
    } catch (error) {
      console.error(`Failed to ${action} appointment:`, error);
      alert(`Failed to ${action} appointment. Please try again.`);
    }
  };

  const getStatusBadge = (status) => {
    const statusStyles = {
      PENDING: { backgroundColor: '#fff3cd', color: '#856404', border: '1px solid #ffeaa7' },
      CONFIRMED: { backgroundColor: '#d1edff', color: '#0c5460', border: '1px solid #b8daff' },
      CANCELLED: { backgroundColor: '#f8d7da', color: '#721c24', border: '1px solid #f5c6cb' },
      COMPLETED: { backgroundColor: '#d4edda', color: '#155724', border: '1px solid #c3e6cb' }
    };
    
    return statusStyles[status] || statusStyles.PENDING;
  };

  if (user?.role !== 'DOCTOR') {
    return (
      <div className="container">
        <div className="card feedback error">
          Access Denied: Only doctors can set availability.
        </div>
      </div>
    );
  }

  return (
    <div className="container">
      <div className="header">
        <h1>Set Availability</h1>
        <div className="subtle">Manage your weekly availability and appointments</div>
      </div>

      {/* Pending Appointments Section */}
      <div className="card" style={{ marginBottom: '30px' }}>
        <h2 style={{ marginBottom: '20px', color: '#333' }}>
          📋 Pending Appointments
          {loadingAppointments && <span className="subtle"> (Loading...)</span>}
        </h2>
        
        {!loadingAppointments && (
          <>
            {appointments.filter(apt => apt.status === 'PENDING').length === 0 ? (
              <div className="subtle" style={{ padding: '20px', textAlign: 'center' }}>
                No pending appointments at the moment.
              </div>
            ) : (
              <div className="container grid grid-2" style={{ gap: '15px' }}>
                {appointments
                  .filter(apt => apt.status === 'PENDING')
                  .map(appointment => (
                    <div key={appointment.id} className="card" style={{ 
                      padding: '15px', 
                      borderLeft: '4px solid #ffc107',
                      backgroundColor: '#fffbf0'
                    }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start', marginBottom: '10px' }}>
                        <div>
                          <strong>{appointment.patient?.name || 'Unknown Patient'}</strong>
                          <div className="subtle">ID: {appointment.patient?.id}</div>
                        </div>
                        <span style={{
                          padding: '2px 8px',
                          borderRadius: '12px',
                          fontSize: '0.8em',
                          ...getStatusBadge(appointment.status)
                        }}>
                          {appointment.status}
                        </span>
                      </div>
                      
                      <div style={{ marginBottom: '10px' }}>
                        <div>📅 {appointment.appointmentDate}</div>
                        <div>⏰ {appointment.appointmentTime}</div>
                        <div style={{ marginTop: '5px' }}>
                          <strong>Reason:</strong> {appointment.reason}
                        </div>
                      </div>

                      <div style={{ display: 'flex', gap: '10px', marginTop: '15px' }}>
                        <button
                          onClick={() => handleAppointmentAction(appointment.id, 'accept')}
                          style={{
                            backgroundColor: '#28a745',
                            color: 'white',
                            border: 'none',
                            padding: '6px 12px',
                            borderRadius: '4px',
                            cursor: 'pointer',
                            fontSize: '0.9em'
                          }}
                        >
                          ✓ Accept
                        </button>
                        <button
                          onClick={() => handleAppointmentAction(appointment.id, 'decline')}
                          style={{
                            backgroundColor: '#dc3545',
                            color: 'white',
                            border: 'none',
                            padding: '6px 12px',
                            borderRadius: '4px',
                            cursor: 'pointer',
                            fontSize: '0.9em'
                          }}
                        >
                          ✗ Decline
                        </button>
                      </div>
                    </div>
                  ))
                }
              </div>
            )}
          </>
        )}
      </div>

      {/* Weekly Availability Section */}
      <div className="card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
          <h2 style={{ margin: 0, color: '#333' }}>🗓️ Weekly Availability</h2>
          <button
            onClick={saveAvailability}
            disabled={saving}
            style={{
              backgroundColor: '#007bff',
              color: 'white',
              border: 'none',
              padding: '10px 20px',
              borderRadius: '5px',
              cursor: saving ? 'not-allowed' : 'pointer',
              opacity: saving ? 0.6 : 1
            }}
          >
            {saving ? 'Saving...' : '💾 Save Availability'}
          </button>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '25px' }}>
          {availabilitySlots.map(daySlot => (
            <div key={daySlot.day} className="card" style={{ 
              padding: '20px', 
              backgroundColor: daySlot.isAvailable ? '#f8f9fa' : '#ffffff',
              border: `2px solid ${daySlot.isAvailable ? '#007bff' : '#dee2e6'}`
            }}>
              <div style={{ 
                display: 'flex', 
                justifyContent: 'space-between', 
                alignItems: 'center', 
                marginBottom: '15px' 
              }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                  <input
                    type="checkbox"
                    checked={daySlot.isAvailable}
                    onChange={() => toggleDayAvailability(daySlot.day)}
                    style={{ transform: 'scale(1.2)' }}
                  />
                  <h3 style={{ 
                    margin: 0, 
                    color: daySlot.isAvailable ? '#007bff' : '#6c757d' 
                  }}>
                    {daySlot.dayLabel}
                  </h3>
                  {daySlot.timeSlots.length > 0 && (
                    <span className="badge" style={{ 
                      backgroundColor: '#28a745', 
                      color: 'white' 
                    }}>
                      {daySlot.timeSlots.length} slots
                    </span>
                  )}
                </div>

                {daySlot.isAvailable && (
                  <div style={{ display: 'flex', gap: '5px', flexWrap: 'wrap' }}>
                    <button
                      onClick={() => handleQuickSet(daySlot.day, 'morning')}
                      style={{ 
                        padding: '4px 8px', 
                        fontSize: '0.8em',
                        backgroundColor: '#ffc107',
                        border: 'none',
                        borderRadius: '3px',
                        cursor: 'pointer'
                      }}
                    >
                      Morning
                    </button>
                    <button
                      onClick={() => handleQuickSet(daySlot.day, 'afternoon')}
                      style={{ 
                        padding: '4px 8px', 
                        fontSize: '0.8em',
                        backgroundColor: '#17a2b8',
                        color: 'white',
                        border: 'none',
                        borderRadius: '3px',
                        cursor: 'pointer'
                      }}
                    >
                      Afternoon
                    </button>
                    <button
                      onClick={() => handleQuickSet(daySlot.day, 'evening')}
                      style={{ 
                        padding: '4px 8px', 
                        fontSize: '0.8em',
                        backgroundColor: '#6f42c1',
                        color: 'white',
                        border: 'none',
                        borderRadius: '3px',
                        cursor: 'pointer'
                      }}
                    >
                      Evening
                    </button>
                    <button
                      onClick={() => handleQuickSet(daySlot.day, 'all')}
                      style={{ 
                        padding: '4px 8px', 
                        fontSize: '0.8em',
                        backgroundColor: '#28a745',
                        color: 'white',
                        border: 'none',
                        borderRadius: '3px',
                        cursor: 'pointer'
                      }}
                    >
                      All Day
                    </button>
                    <button
                      onClick={() => handleQuickSet(daySlot.day, 'clear')}
                      style={{ 
                        padding: '4px 8px', 
                        fontSize: '0.8em',
                        backgroundColor: '#dc3545',
                        color: 'white',
                        border: 'none',
                        borderRadius: '3px',
                        cursor: 'pointer'
                      }}
                    >
                      Clear
                    </button>
                  </div>
                )}
              </div>

              {daySlot.isAvailable && (
                <div style={{ 
                  display: 'grid', 
                  gridTemplateColumns: 'repeat(auto-fill, minmax(80px, 1fr))', 
                  gap: '8px' 
                }}>
                  {timeSlots.map(timeSlot => (
                    <button
                      key={timeSlot}
                      onClick={() => toggleTimeSlot(daySlot.day, timeSlot)}
                      style={{
                        padding: '8px 4px',
                        fontSize: '0.85em',
                        border: '1px solid #dee2e6',
                        borderRadius: '4px',
                        backgroundColor: daySlot.timeSlots.includes(timeSlot) ? '#007bff' : 'white',
                        color: daySlot.timeSlots.includes(timeSlot) ? 'white' : '#333',
                        cursor: 'pointer',
                        transition: 'all 0.2s ease'
                      }}
                    >
                      {timeSlot}
                    </button>
                  ))}
                </div>
              )}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default SetAvailability;