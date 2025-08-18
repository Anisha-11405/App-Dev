import React, { useState, useEffect } from "react";
import api from "../utils/api";

const BookAppointment = () => {
  const [form, setForm] = useState({
    patientId: "",
    doctorId: "",
    appointmentDate: "",
    appointmentTime: "",
    reason: ""
  });
  const [loading, setLoading] = useState(false);
  const [patients, setPatients] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [loadingData, setLoadingData] = useState(true);

  useEffect(() => {
    const loadData = async () => {
      try {
        const [patientsRes, doctorsRes] = await Promise.all([
          api.get('/api/patients'),
          api.get('/api/doctors')
        ]);
        setPatients(patientsRes.data || []);
        setDoctors(doctorsRes.data || []);
      } catch (error) {
        console.error('Failed to load patients/doctors:', error);
      } finally {
        setLoadingData(false);
      }
    };
    loadData();
  }, []);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    
    try {
      const appointmentData = {
        patient: {
          patientId: parseInt(form.patientId)
        },
        doctor: {
          doctorId: parseInt(form.doctorId)
        },
        appointmentDate: form.appointmentDate,
        appointmentTime: form.appointmentTime,
        reason: form.reason
      };

      console.log("Booking appointment:", appointmentData);

      const response = await api.post("/api/appointments", appointmentData);
      
      console.log("Appointment booked successfully:", response.data);
      alert("Appointment booked successfully!");
      
      setForm({
        patientId: "",
        doctorId: "",
        appointmentDate: "",
        appointmentTime: "",
        reason: ""
      });
    } catch (error) {
      console.error("Failed to book appointment:", error);
      const errorMessage = error.response?.data?.error || 
                          error.response?.data?.message || 
                          "Failed to book appointment";
      alert(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  if (loadingData) {
    return (
      <div className="page-container">
        <div className="loading-message">Loading patients and doctors...</div>
      </div>
    );
  }

  return (
    <div className="page-container">
      <div className="page-header">
        <h1>Book Appointment</h1>
        <p>Fill in the details to create a new appointment</p>
      </div>

      <div className="form-container">
        <div className="form-header">
          <h2>New Appointment</h2>
          <p>Please fill in all the required information</p>
        </div>

        <form onSubmit={handleSubmit} className="appointment-form">
          <div className="form-row">
            <div className="form-group">
              <label className="form-label">
                Patient <span className="required">*</span>
              </label>
              <select
                name="patientId"
                value={form.patientId}
                onChange={handleChange}
                className="form-input"
                required
              >
                <option value="">Select Patient</option>
                {patients.map((patient) => (
                  <option key={patient.id} value={patient.id}>
                    {patient.name} (ID: {patient.id})
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">
                Doctor <span className="required">*</span>
              </label>
              <select
                name="doctorId"
                value={form.doctorId}
                onChange={handleChange}
                className="form-input"
                required
              >
                <option value="">Select Doctor</option>
                {doctors.map((doctor) => (
                  <option key={doctor.id} value={doctor.id}>
                    Dr. {doctor.name} - {doctor.specialization || 'General'} (ID: {doctor.id})
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label">
                Appointment Date <span className="required">*</span>
              </label>
              <input
                type="date"
                name="appointmentDate"
                value={form.appointmentDate}
                onChange={handleChange}
                className="form-input"
                min={new Date().toISOString().split('T')[0]}
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label">
                Appointment Time <span className="required">*</span>
              </label>
              <input
                type="time"
                name="appointmentTime"
                value={form.appointmentTime}
                onChange={handleChange}
                className="form-input"
                required
              />
            </div>
          </div>

          <div className="form-group full-width">
            <label className="form-label">
              Reason for Appointment <span className="required">*</span>
            </label>
            <textarea
              name="reason"
              value={form.reason}
              onChange={handleChange}
              className="form-textarea"
              placeholder="e.g., General consultation, Follow-up, Check-up"
              rows="4"
              required
            />
          </div>

          <div className="form-actions">
            <button 
              type="submit" 
              className="submit-btn"
              disabled={loading}
            >
              {loading ? (
                <>
                  <span className="spinner"></span>
                  Booking...
                </>
              ) : (
                'Book Appointment'
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default BookAppointment;