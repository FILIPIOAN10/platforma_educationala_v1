import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api.js';

export default function TeacherDashboard() {
  const [data, setData] = useState(null);
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      api.get('/teacher/dashboard'),
      api.get('/teacher/results'),
    ]).then(([d, r]) => {
      setData(d);
      setResults(r);
    }).finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="center"><div className="spinner" /></div>;

  return (
    <div className="container">
      <h1>Dashboard profesor</h1>

      <div className="grid grid-4">
        <div className="stat">
          <div className="value">{data.totalTests}</div>
          <div className="label">Teste create</div>
        </div>
        <div className="stat">
          <div className="value">{data.totalSubmissions}</div>
          <div className="label">Rezolvari elevi</div>
        </div>
        <div className="stat">
          <div className="value">{(data.averageGrade || 0).toFixed(2)}</div>
          <div className="label">Media generala</div>
        </div>
        <div className="stat">
          <div className="value">{Object.keys(data.averageBySubject || {}).length}</div>
          <div className="label">Materii predate</div>
        </div>
      </div>

      <div className="grid grid-2" style={{ marginTop: 18 }}>
        <div className="card">
          <h2>Media pe materie</h2>
          <BarList data={data.averageBySubject} maxValue={10} />
        </div>
        <div className="card">
          <h2>Numarul de rezolvari pe test</h2>
          <BarList data={data.submissionsByTest} />
        </div>
      </div>

      <h2 className="section-title">Ultimele rezultate ale elevilor</h2>
      <div className="card">
        {results.length === 0 ? (
          <p className="muted">Niciun elev nu a dat inca un test.</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Elev</th>
                <th>Test</th>
                <th>Materie</th>
                <th>Scor</th>
                <th>Nota</th>
                <th>Data</th>
              </tr>
            </thead>
            <tbody>
              {results.slice(0, 20).map((s) => (
                <tr key={s.submissionId}>
                  <td>{s.studentName}<br /><span className="muted">@{s.studentUsername}</span></td>
                  <td><Link to={`/teacher/tests/${s.testId}`}>{s.testTitle}</Link></td>
                  <td>{s.subject}</td>
                  <td>{s.score}/{s.maxScore}</td>
                  <td><strong>{s.grade.toFixed(2)}</strong></td>
                  <td>{new Date(s.submittedAt).toLocaleString('ro-RO')}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}

function BarList({ data, maxValue }) {
  const entries = Object.entries(data || {});
  if (entries.length === 0) return <p className="muted">Fara date.</p>;
  const max = maxValue ?? Math.max(...entries.map(([, v]) => v), 1);
  return (
    <div>
      {entries.map(([k, v]) => (
        <div key={k} style={{ marginBottom: 10 }}>
          <div className="flex-row between" style={{ marginBottom: 4 }}>
            <span>{k}</span>
            <strong>{Number(v).toFixed(2)}</strong>
          </div>
          <div className="bar"><div style={{ width: `${(v / max) * 100}%` }} /></div>
        </div>
      ))}
    </div>
  );
}
