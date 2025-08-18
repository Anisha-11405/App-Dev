import React, { useState } from "react";

export default function Profile() {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [saved, setSaved] = useState(false);

  const save = (e) => {
    e.preventDefault();
    setSaved(true);
    setTimeout(() => setSaved(false), 2000);
  };

  return (
    <div className="container" style={{ maxWidth: 680 }}>
      <div className="header">
        <h1>Profile</h1>
        <div className="subtle">Update your basic information (local only demo).</div>
      </div>
      {saved && <div className="feedback success">Profile saved (demo)</div>}
      <form onSubmit={save} className="card">
        <div className="field">
          <label className="label">Name</label>
          <input className="input" value={name} onChange={(e) => setName(e.target.value)} placeholder="Your name" />
        </div>
        <div className="field">
          <label className="label">Email</label>
          <input className="input" type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="you@example.com" />
        </div>
        <button className="btn" type="submit">Save</button>
      </form>
    </div>
  );
}
