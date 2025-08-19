import React, { useState } from 'react';
import api from '../utils/api';

export default function AppointmentForm({ onAppointmentCreated }) {
  const [formData, setFormData] = useState({
    patientId: '',
    doctorId: '',
    appointmentDate: '',
    appointmentTime: '',
    reason: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const appointmentData = {
        patient: { patientId: parseInt(formData.patientId) },
        doctor: { doctorId: parseInt(formData.doctorId) },
        appointmentDate: formData.appointmentDate,
        appointmentTime: formData.appointmentTime,
        reason: formData.reason
      };

      const response = await api.post('/api/appointments', appointmentData);

      if (onAppointmentCreated) onAppointmentCreated(response.data);

      setFormData({
        patientId: '',
        doctorId: '',
        appointmentDate: '',
        appointmentTime: '',
        reason: ''
      });

      alert('Appointment booked successfully!');
    } catch (err) {
      let errorMessage = 'Failed to book appointment';
      if (err.response?.data?.error) {
        errorMessage = err.response.data.error;
      } else if (err.response?.data) {
        errorMessage = typeof err.response.data === 'string' 
          ? err.response.data 
          : JSON.stringify(err.response.data);
      } else if (err.message) {
        errorMessage = err.message;
      }
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  return (
    <div className="form-wrapper">
      <div className="card">
        <h2 className="form-title">Book an Appointment</h2>

        {error && <div className="error-message">{error}</div>}

        <form onSubmit={handleSubmit} className="form-content">
          <label>
            Patient ID
            <input
              type="number"
              name="patientId"
              placeholder="Enter Patient ID"
              value={formData.patientId}
              onChange={handleChange}
              required
              disabled={loading}
            />
          </label>

          <label>
            Doctor ID
            <input
              type="number"
              name="doctorId"
              placeholder="Enter Doctor ID"
              value={formData.doctorId}
              onChange={handleChange}
              required
              disabled={loading}
            />
          </label>

          <label>
            Appointment Date
            <input
              type="date"
              name="appointmentDate"
              value={formData.appointmentDate}
              onChange={handleChange}
              required
              disabled={loading}
              min={new Date().toISOString().split('T')[0]}
            />
          </label>

          <label>
            Appointment Time
            <input
              type="time"
              name="appointmentTime"
              value={formData.appointmentTime}
              onChange={handleChange}
              required
              disabled={loading}
            />
          </label>

          <label>
            Reason
            <textarea
              name="reason"
              placeholder="Reason for appointment..."
              value={formData.reason}
              onChange={handleChange}
              required
              disabled={loading}
              rows={3}
            />
          </label>

          <button type="submit" disabled={loading} className="btn-submit">
            {loading ? 'Booking...' : 'Book Appointment'}
          </button>
        </form>
      </div>
    </div>
  );
}
