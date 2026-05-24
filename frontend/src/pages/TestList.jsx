import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api.js';

const GRADE_LABEL = { 5: 'a V-a', 6: 'a VI-a', 7: 'a VII-a', 8: 'a VIII-a' };

export default function TestList() {
  const [tests, setTests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('');
  const [subject, setSubject] = useState('');

  useEffect(() => {
    api.get('/student/tests').then(setTests).finally(() => setLoading(false));
  }, []);

  const subjects = useMemo(() => Array.from(new Set(tests.map(t => t.subject))), [tests]);
  const filtered = tests.filter((t) =>
    (subject === '' || t.subject === subject)
    && (filter === '' || t.title.toLowerCase().includes(filter.toLowerCase()) ||
        (t.chapter && t.chapter.toLowerCase().includes(filter.toLowerCase()))));

  if (loading) return <div className="center"><div className="spinner" /></div>;

  return (
    <div className="container">
      <h1>Teste pentru clasa ta</h1>

      <div className="card flex-row" style={{ gap: 12 }}>
        <input
          className="input"
          placeholder="Cauta dupa titlu sau capitol..."
          value={filter}
          onChange={(e) => setFilter(e.target.value)}
          style={{ flex: 1 }}
        />
        <select className="select" value={subject} onChange={(e) => setSubject(e.target.value)} style={{ maxWidth: 220 }}>
          <option value="">Toate materiile</option>
          {subjects.map((s) => <option key={s} value={s}>{s}</option>)}
        </select>
      </div>

      <div className="grid grid-3">
        {filtered.map((t) => (
          <div key={t.id} className="test-card">
            <div className="meta">
              <span className="badge">{t.subject}</span>
              <span className="badge badge-grade">Clasa {GRADE_LABEL[t.gradeLevel]}</span>
              {t.difficulty && <span className={`badge badge-difficulty-${t.difficulty}`}>{t.difficulty}</span>}
            </div>
            <h3 style={{ margin: 0 }}>{t.title}</h3>
            {t.description && <p className="muted" style={{ margin: 0 }}>{t.description}</p>}
            <div className="muted">
              {t.numberOfQuestions} intrebari{t.chapter ? ` - ${t.chapter}` : ''} - autor: {t.creatorName || 'sistem'}
            </div>
            <Link to={`/student/tests/${t.id}`} className="btn" style={{ alignSelf: 'flex-start', marginTop: 6 }}>
              Incepe testul
            </Link>
          </div>
        ))}
        {filtered.length === 0 && <p className="muted">Niciun test gasit.</p>}
      </div>
    </div>
  );
}
