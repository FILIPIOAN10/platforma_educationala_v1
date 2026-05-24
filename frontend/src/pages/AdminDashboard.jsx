import { useEffect, useState } from 'react';
import { api } from '../api.js';

export default function AdminDashboard() {
  const [data, setData] = useState(null);

  useEffect(() => { api.get('/admin/analytics').then(setData); }, []);

  if (!data) return <div className="center"><div className="spinner" /></div>;

  return (
    <div className="container">
      <h1>Analytics platforma</h1>

      <div className="grid grid-4">
        <Stat label="Utilizatori" value={data.totalUsers} />
        <Stat label="Elevi" value={data.totalStudents} />
        <Stat label="Profesori" value={data.totalTeachers} />
        <Stat label="Parinti" value={data.totalParents} />
        <Stat label="Admin" value={data.totalAdmins} />
        <Stat label="Teste" value={data.totalTests} />
        <Stat label="Submisii" value={data.totalSubmissions} />
        <Stat label="Media generala" value={(data.averageGrade || 0).toFixed(2)} />
      </div>

      <div className="grid grid-2" style={{ marginTop: 18 }}>
        <div className="card">
          <h2>Distributie elevi pe clasa</h2>
          <Bars data={data.studentsByGrade} />
        </div>
        <div className="card">
          <h2>Distributie teste pe clasa</h2>
          <Bars data={data.testsByGrade} />
        </div>
        <div className="card">
          <h2>Engagement (clicks/views)</h2>
          <Bars data={data.eventCounts} />
        </div>
        <div className="card">
          <h2>Evenimente recente</h2>
          <table>
            <thead>
              <tr><th>Utilizator</th><th>Eveniment</th><th>Test</th><th>Cand</th></tr>
            </thead>
            <tbody>
              {data.recentEvents.slice(0, 12).map((e) => (
                <tr key={e.id}>
                  <td>{e.username}</td>
                  <td>{e.eventType}</td>
                  <td>{e.testId || '-'}</td>
                  <td>{new Date(e.occurredAt).toLocaleString('ro-RO')}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

function Stat({ label, value }) {
  return (
    <div className="stat">
      <div className="value">{value}</div>
      <div className="label">{label}</div>
    </div>
  );
}

function Bars({ data }) {
  const entries = Object.entries(data || {});
  if (!entries.length) return <p className="muted">Fara date.</p>;
  const max = Math.max(...entries.map(([, v]) => v), 1);
  return (
    <div>
      {entries.map(([k, v]) => (
        <div key={k} style={{ marginBottom: 10 }}>
          <div className="flex-row between" style={{ marginBottom: 4 }}>
            <span>{k}</span><strong>{v}</strong>
          </div>
          <div className="bar"><div style={{ width: `${(v / max) * 100}%` }} /></div>
        </div>
      ))}
    </div>
  );
}
