import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api.js';
import { useAuth } from '../context/AuthContext.jsx';

const GRADE_LABEL = { 5: 'a V-a', 6: 'a VI-a', 7: 'a VII-a', 8: 'a VIII-a' };

export default function StudentDashboard() {
  const { user } = useAuth();
  const [tests, setTests] = useState([]);
  const [submissions, setSubmissions] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      api.get('/student/tests'),
      api.get('/student/submissions'),
    ]).then(([t, s]) => {
      setTests(t);
      setSubmissions(s);
    }).finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="center"><div className="spinner" /></div>;

  const avg = submissions.length === 0
    ? 0
    : submissions.reduce((a, b) => a + b.grade, 0) / submissions.length;

  return (
    <div className="container">
      <h1>Salut, {user.fullName}!</h1>
      <p className="muted">Esti elev in clasa {GRADE_LABEL[user.gradeLevel] || user.gradeLevel}.</p>

      <div className="grid grid-4" style={{ marginTop: 18 }}>
        <div className="stat">
          <div className="value">{tests.length}</div>
          <div className="label">Teste disponibile</div>
        </div>
        <div className="stat">
          <div className="value">{submissions.length}</div>
          <div className="label">Teste rezolvate</div>
        </div>
        <div className="stat">
          <div className="value">{avg.toFixed(2)}</div>
          <div className="label">Media generala</div>
        </div>
        <div className="stat">
          <div className="value">{submissions.filter(s => s.grade >= 9).length}</div>
          <div className="label">Note &ge; 9</div>
        </div>
      </div>

      <h2 className="section-title">Teste sugerate</h2>
      <div className="grid grid-3">
        {tests.slice(0, 6).map((t) => (
          <div key={t.id} className="test-card">
            <div className="meta">
              <span className="badge">{t.subject}</span>
              <span className="badge badge-grade">Clasa {GRADE_LABEL[t.gradeLevel]}</span>
              {t.difficulty && <span className={`badge badge-difficulty-${t.difficulty}`}>{t.difficulty}</span>}
            </div>
            <h3 style={{ margin: 0 }}>{t.title}</h3>
            <div className="muted">{t.numberOfQuestions} intrebari{t.chapter ? ` - ${t.chapter}` : ''}</div>
            <Link to={`/student/tests/${t.id}`} className="btn" style={{ alignSelf: 'flex-start', marginTop: 6 }}>
              Incepe testul
            </Link>
          </div>
        ))}
      </div>

      {submissions.length > 0 && (
        <>
          <h2 className="section-title">Ultimele rezultate</h2>
          <div className="card">
            <table>
              <thead>
                <tr>
                  <th>Test</th>
                  <th>Materie</th>
                  <th>Scor</th>
                  <th>Nota</th>
                  <th>Data</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {submissions.slice(0, 5).map((s) => (
                  <tr key={s.submissionId}>
                    <td>{s.testTitle}</td>
                    <td>{s.subject}</td>
                    <td>{s.score}/{s.maxScore}</td>
                    <td><strong>{s.grade.toFixed(2)}</strong></td>
                    <td>{new Date(s.submittedAt).toLocaleString('ro-RO')}</td>
                    <td><Link to={`/student/results/${s.submissionId}`}>Detalii</Link></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}
    </div>
  );
}
