import { useEffect, useState } from 'react';
import { api } from '../api.js';

export default function ParentDashboard() {
  const [progress, setProgress] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get('/parent/dashboard').then(setProgress).finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="center"><div className="spinner" /></div>;

  if (progress.length === 0) {
    return (
      <div className="container">
        <div className="card">
          <p className="muted">Nu ai inca niciun copil asociat contului. Cere administratorului sa te asocieze.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="container">
      <h1>Progresul copiilor</h1>
      {progress.map((p) => <ChildSection key={p.child.id} p={p} />)}
    </div>
  );
}

function ChildSection({ p }) {
  const strong = [...(p.bySubject || [])].sort((a, b) => b.average - a.average).slice(0, 1)[0];
  const weak = [...(p.bySubject || [])].sort((a, b) => a.average - b.average).slice(0, 1)[0];

  return (
    <div className="card">
      <h2>{p.child.fullName} <span className="muted">- clasa {p.child.gradeLevel}</span></h2>

      <div className="grid grid-4">
        <div className="stat">
          <div className="value">{(p.overallAverage || 0).toFixed(2)}</div>
          <div className="label">Media generala</div>
        </div>
        <div className="stat">
          <div className="value">{p.testsTaken}</div>
          <div className="label">Teste rezolvate</div>
        </div>
        <div className="stat">
          <div className="value" style={{ color: '#10b981' }}>{strong ? strong.subject : '-'}</div>
          <div className="label">Materie tare {strong ? `(${strong.average.toFixed(2)})` : ''}</div>
        </div>
        <div className="stat">
          <div className="value" style={{ color: '#ef4444' }}>{weak ? weak.subject : '-'}</div>
          <div className="label">Materie de imbunatatit {weak ? `(${weak.average.toFixed(2)})` : ''}</div>
        </div>
      </div>

      <div className="grid grid-2" style={{ marginTop: 14 }}>
        <div>
          <h3>Medii pe materie</h3>
          {p.bySubject.length === 0 ? <p className="muted">Inca fara teste rezolvate.</p> : (
            <div>
              {p.bySubject.map((s) => (
                <div key={s.subject} style={{ marginBottom: 8 }}>
                  <div className="flex-row between">
                    <span>{s.subject} <span className="muted">({s.testsTaken} teste)</span></span>
                    <strong>{s.average.toFixed(2)}</strong>
                  </div>
                  <div className="bar">
                    <div style={{ width: `${(s.average / 10) * 100}%` }} />
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
        <div>
          <h3>Evolutie in timp</h3>
          {Object.keys(p.evolution || {}).length === 0 ? <p className="muted">Insuficiente date.</p> : (
            <div>
              {Object.entries(p.evolution).map(([day, v]) => (
                <div key={day} style={{ marginBottom: 6 }}>
                  <div className="flex-row between">
                    <span>{day}</span><strong>{v.toFixed(2)}</strong>
                  </div>
                  <div className="bar"><div style={{ width: `${(v / 10) * 100}%` }} /></div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      <h3 style={{ marginTop: 14 }}>Ultimele rezultate</h3>
      {p.recent.length === 0 ? <p className="muted">Fara rezultate.</p> : (
        <table>
          <thead>
            <tr><th>Test</th><th>Materie</th><th>Capitol</th><th>Scor</th><th>Nota</th><th>Data</th></tr>
          </thead>
          <tbody>
            {p.recent.map((r) => (
              <tr key={r.submissionId}>
                <td>{r.testTitle}</td>
                <td>{r.subject}</td>
                <td>{r.chapter || '-'}</td>
                <td>{r.score}/{r.maxScore}</td>
                <td><strong>{r.grade.toFixed(2)}</strong></td>
                <td>{new Date(r.submittedAt).toLocaleString('ro-RO')}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
