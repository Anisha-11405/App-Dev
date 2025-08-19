import React, { useEffect, useState } from "react";
import api from "../utils/api";

export default function Doctors() {
  const [list, setList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadDoctors = async () => {
    try {
      setLoading(true);
      setError("");
      
      console.log("Loading doctors...");
      const res = await api.get("/api/doctors");
      console.log("Doctors loaded:", res.data);
      
      setList(res.data || []);
    } catch (err) {
      console.error("Failed to load doctors:", err);
      
      let errorMessage = "Failed to load doctors";
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
    loadDoctors();
  }, []);

  if (loading) {
    return (
      <div className="container">
        <div className="header">
          <h1>Doctors</h1>
        </div>
        <div className="card">Loading doctors...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container">
        <div className="header">
          <h1>Doctors</h1>
        </div>
        <div className="card feedback error">
          {error}
          <button 
            onClick={loadDoctors}
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
        <h1>Doctors</h1>
        <div className="subtle">View all available doctors</div>
      </div>
      
      <div style={{ marginBottom: '20px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <span>Total Doctors: {list.length}</span>
        <button onClick={loadDoctors} className="btn">🔄 Refresh</button>
      </div>

      <div className="container grid grid-3">
        {list.map((doctor) => (
          <div key={doctor.id} className="card">
            <div style={{ display: 'flex', alignItems: 'center', marginBottom: '10px' }}>
              <div style={{ 
                width: '50px', 
                height: '50px', 
                backgroundColor: '#007bff', 
                borderRadius: '50%', 
                display: 'flex', 
                alignItems: 'center', 
                justifyContent: 'center', 
                color: 'white', 
                fontWeight: 'bold',
                marginRight: '15px'
              }}>
                Dr
              </div>
              <div>
                <h3 style={{ margin: '0', fontSize: '1.2em' }}>
                  Dr. {doctor.name}
                </h3>
                <div className="badge" style={{ marginTop: '5px' }}>
                  ID: {doctor.id}
                </div>
              </div>
            </div>
            
            <div style={{ marginBottom: '10px' }}>
              <strong>Specialization:</strong>
              <div className="subtle" style={{ marginTop: '2px' }}>
                {doctor.specialization || "General Medicine"}
              </div>
            </div>
            
            <div style={{ marginBottom: '10px' }}>
              <strong>Email:</strong>
              <div className="subtle" style={{ marginTop: '2px' }}>
                {doctor.email}
              </div>
            </div>
            
            {doctor.phoneNumber && (
              <div style={{ marginBottom: '10px' }}>
                <strong>Phone:</strong>
                <div className="subtle" style={{ marginTop: '2px' }}>
                  {doctor.phoneNumber}
                </div>
              </div>
            )}
            
            <div style={{ marginTop: '15px', padding: '10px', backgroundColor: '#f8f9fa', borderRadius: '5px' }}>
              <div style={{ fontSize: '0.9em', color: '#6c757d' }}>
                Available for appointments
              </div>
            </div>
          </div>
        ))}
        
        {list.length === 0 && (
          <div className="card" style={{ gridColumn: '1 / -1', textAlign: 'center', padding: '40px' }}>
            <h3>No doctors found</h3>
            <p className="subtle">There are currently no doctors registered in the system.</p>
          </div>
        )}
      </div>
    </div>
  );
}