import React, { useState, useEffect } from "react";
import { useUser } from "../App";
import api from "../utils/api";

const BookAppointment = () => {
  const { user } = useUser();
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
        let patientsData = [];
        let doctorsData = [];

        if (user?.role === "PATIENT") {
          patientsData = [user];
          setForm(prev => ({ ...prev, patientId: user.id }));
        } else if (user?.role === "ADMIN") {
          const patientsRes = await api.get("/api/patients");
          patientsData = patientsRes.data || [];
        }

        const doctorsRes = await api.get("/api/doctors");
        doctorsData = doctorsRes.data || [];

        setPatients(patientsData);
        setDoctors(doctorsData);
      } catch (error) {
        console.error("Failed to load patients/doctors:", error);
        alert("Failed to load required data. Please refresh the page.");
      } finally {
        setLoadingData(false);
      }
    };
    loadData();
  }, [user]);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      const appointmentData = {
        patientId: user?.role === "PATIENT" ? user.id : parseInt(form.patientId),
        doctorId: parseInt(form.doctorId),
        appointmentDate: form.appointmentDate,
        appointmentTime: form.appointmentTime,
        reason: form.reason
      };

      console.log("Booking appointment:", appointmentData);

      const response = await api.post("/api/appointments", appointmentData);

      console.log("Appointment booked successfully:", response.data);
      alert("Appointment booked successfully!");

      setForm({
        patientId: user?.role === "PATIENT" ? user.id : "",
        doctorId: "",
        appointmentDate: "",
        appointmentTime: "",
        reason: ""
      });
    } catch (error) {
      console.error(
        "Failed to book appointment:",
        error.response ? error.response.data : error.message
      );

      const errorMessage =
        error.response?.data?.error ||
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
        <div className="loading-message">Loading appointment booking form...</div>
      </div>
    );
  }

  return (
    <div className="page-container">
      <div className="page-header">
        <h1>Book Appointment</h1>
        <p>
          {user?.role === "PATIENT"
            ? "Schedule your appointment with a doctor"
            : "Create a new appointment for a patient"}
        </p>
      </div>

      <div className="form-container">
        <div className="form-header">
          <h2>New Appointment</h2>
          <p>Please fill in all the required information</p>
        </div>

        <form onSubmit={handleSubmit} className="appointment-form">
          <div className="form-row">
            {user?.role === "PATIENT" ? (
              <div className="form-group">
                <label className="form-label">
                  Patient <span className="required">*</span>
                </label>
                <div className="patient-info-display">
                  <div
                    style={{
                      padding: "12px",
                      backgroundColor: "#e9ecef",
                      borderRadius: "4px",
                      border: "1px solid #ced4da"
                    }}
                  >
                    <strong>{user.name}</strong>
                    <div className="subtle">ID: {user.id}</div>
                    <div className="subtle">Email: {user.email}</div>
                  </div>
                </div>
              </div>
            ) : (
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
                      {patient.name} (ID: {patient.id}) - {patient.email}
                    </option>
                  ))}
                </select>
              </div>
            )}

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
                    Dr. {doctor.name} - {doctor.specialization || "General"} (ID: {doctor.id})
                  </option>
                ))}
              </select>
              {doctors.length === 0 && (
                <div className="form-help">No doctors available at the moment.</div>
              )}
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
                min={new Date().toISOString().split("T")[0]}
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label">
                Appointment Time <span className="required">*</span>
              </label>
              <select
                name="appointmentTime"
                value={form.appointmentTime}
                onChange={handleChange}
                className="form-input"
                required
              >
                <option value="">Select Time</option>
                <optgroup label="Morning">
                  <option value="09:00">09:00 AM</option>
                  <option value="09:30">09:30 AM</option>
                  <option value="10:00">10:00 AM</option>
                  <option value="10:30">10:30 AM</option>
                  <option value="11:00">11:00 AM</option>
                  <option value="11:30">11:30 AM</option>
                  <option value="12:00">12:00 PM</option>
                </optgroup>
                <optgroup label="Afternoon">
                  <option value="14:00">02:00 PM</option>
                  <option value="14:30">02:30 PM</option>
                  <option value="15:00">03:00 PM</option>
                  <option value="15:30">03:30 PM</option>
                  <option value="16:00">04:00 PM</option>
                  <option value="16:30">04:30 PM</option>
                </optgroup>
                <optgroup label="Evening">
                  <option value="17:00">05:00 PM</option>
                  <option value="17:30">05:30 PM</option>
                  <option value="18:00">06:00 PM</option>
                </optgroup>
              </select>
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
              placeholder="Please describe the reason for your appointment (e.g., General consultation, Follow-up visit, Specific health concern)"
              rows="4"
              required
            />
          </div>

          <div className="form-actions">
            <button
              type="submit"
              className="submit-btn"
              disabled={loading || doctors.length === 0}
            >
              {loading ? (
                <>
                  <span className="spinner"></span>
                  Booking...
                </>
              ) : (
                "Book Appointment"
              )}
            </button>

            {doctors.length === 0 && (
              <div className="form-help error">
                Cannot book appointment: No doctors available.
              </div>
            )}
          </div>
        </form>
      </div>

      <style jsx>{`
        .patient-info-display {
          margin-top: 5px;
        }

        .form-help {
          margin-top: 5px;
          font-size: 0.9em;
          color: #6c757d;
        }

        .form-help.error {
          color: #dc3545;
        }

        .subtle {
          font-size: 0.85em;
          color: #6c757d;
          margin-top: 2px;
        }

        .spinner {
          display: inline-block;
          width: 12px;
          height: 12px;
          border: 2px solid transparent;
          border-top: 2px solid currentColor;
          border-radius: 50%;
          animation: spin 1s linear infinite;
          margin-right: 8px;
        }

        @keyframes spin {
          0% { transform: rotate(0deg); }
          100% { transform: rotate(360deg); }
        }

        .loading-message {
          text-align: center;
          padding: 40px;
          font-size: 1.1em;
          color: #666;
        }
      `}</style>
    </div>
  );
};

export default BookAppointment;
