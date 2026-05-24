import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { api } from '../api.js';

export default function StudentResults() {
  const { id } = useParams();
  const [submissions, setSubmissions] = useState([]);
  const [detail, setDetail] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    if (id) {
      api.get(`/student/submissions/${id}`).then(setDetail).finally(() => setLoading(false));
    } else {
      api.get('/student/submissions').then(setSubmissions).finally(() => setLoading(false));
    }
  }, [id]);

  if (loading) return <div className="center"><div className="spinner" /></div>;

  if (id && detail) {
    return (
      <div className="container">
        <Link to="/student/results" className="muted">&larr; Toate rezultatele</Link>
        <div className="results-summary">
          <h2 style={{ margin: '4px 0' }}>{detail.testTitle}</h2>
          <div className="grade">{detail.grade.toFixed(2)}</div>
          <div className="score">Scor: {detail.score} / {detail.maxScore}</div>
          <div style={{ opacity: 0.85 }}>{new Date(detail.submittedAt).toLocaleString('ro-RO')}</div>
        </div>
        <h3>Raspunsuri</h3>
        {detail.feedback.map((f, idx) => (
          <div key={f.questionId} className="question-box">
            <div className="prompt">{idx + 1}. {f.prompt}</div>
            <div className={f.correct ? 'feedback-correct' : 'feedback-wrong'}>
              <div className="muted">Raspunsul tau:</div>
              <div className={f.correct ? 'value-correct' : 'value-wrong'}>
                {f.givenAnswer || <em>(fara raspuns)</em>}
              </div>
              {!f.correct && (
                <div className="correct-answer-banner">
                  Raspuns corect: <strong>{f.correctAnswer}</strong>
                </div>
              )}
              {f.explanation && (
                <div className="muted" style={{ marginTop: 6 }}>
                  <strong>Explicatie:</strong> {f.explanation}
                </div>
              )}
            </div>
          </div>
        ))}
      </div>
    );
  }

  return (
    <div className="container">
      <h1>Rezultatele mele</h1>
      <div className="card">
        {submissions.length === 0 ? (
          <p className="muted">Nu ai rezolvat inca niciun test.</p>
        ) : (
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
              {submissions.map((s) => (
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
        )}
      </div>
    </div>
  );
}
