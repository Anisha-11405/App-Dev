import React, { useEffect, useState } from "react";
import api from "../utils/api";

export default function Patients() {
  const [list, setList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadPatients = async () => {
    try {
      setLoading(true);
      setError("");
      
      console.log("Loading patients...");
      const res = await api.get("/api/patients");
      console.log("Patients loaded:", res.data);
      
      setList(res.data || []);
    } catch (err) {
      console.error("Failed to load patients:", err);
      
      let errorMessage = "Failed to load patients";
      if (err.response?.status === 401) {
        errorMessage = "Unauthorized - Please login again";
      } else if (err.response?.status === 403) {
        errorMessage = "Access denied - Insufficient permissions";
      } else if (err.response?.data?.error) {
        errorMessage = err.response.data.error;
      } else if (err.response?.data?.message) {
        errorMessage = err.response.data.message;
      } else if (err.message) {
        errorMessage = err.message;
      }
      
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadPatients();
  }, []);

  if (loading) {
    return (
      <div className="container">
        <div className="header">
          <h1>Patients</h1>
        </div>
        <div className="card">Loading patients...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container">
        <div className="header">
          <h1>Patients</h1>
        </div>
        <div className="card feedback error">
          {error}
          <button 
            onClick={loadPatients}
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
        <h1>Patients</h1>
        <div className="subtle">View all registered patients</div>
      </div>
      
      <div className="card">
        <div style={{ marginBottom: '10px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <span>Total Patients: {list.length}</span>
          <button onClick={loadPatients}>🔄 Refresh</button>
        </div>
        
        <table className="table">
          <thead>
            <tr>
              <th>Patient ID</th>
              <th>Name</th>
              <th>Email</th>
              <th>Phone</th>
              <th>Date of Birth</th>
              <th>Age</th>
            </tr>
          </thead>
          <tbody>
            {list.map((patient) => {
              let age = null;
              if (patient.dateOfBirth) {
                const birthDate = new Date(patient.dateOfBirth);
                const today = new Date();
                age = today.getFullYear() - birthDate.getFullYear();
                const monthDiff = today.getMonth() - birthDate.getMonth();
                if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
                  age--;
                }
              }

              return (
                <tr key={patient.id}>
                  <td>{patient.id}</td>
                  <td>{patient.name}</td>
                  <td>{patient.email}</td>
                  <td>{patient.phoneNumber}</td>
                  <td>
                    {patient.dateOfBirth 
                      ? new Date(patient.dateOfBirth).toLocaleDateString()
                      : 'Not provided'
                    }
                  </td>
                  <td>{age !== null ? `${age} years` : '-'}</td>
                </tr>
              );
            })}
            {list.length === 0 && (
              <tr>
                <td colSpan="6" className="subtle" style={{ textAlign: 'center', padding: '20px' }}>
                  No patients found.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}