import { useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { api } from '../api.js';

export default function TestTaking() {
  const { id } = useParams();
  const [test, setTest] = useState(null);
  const [answers, setAnswers] = useState({});
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');
  const [startedAt] = useState(() => Date.now());

  useEffect(() => {
    api.get(`/student/tests/${id}`).then(setTest)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [id]);

  const setAnswer = (qid, val) => setAnswers({ ...answers, [qid]: val });

  const submit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError('');
    try {
      const payload = {
        testId: Number(id),
        answers: test.questions.map((q) => ({
          questionId: q.id,
          answer: answers[q.id] ?? '',
        })),
        timeSpentSeconds: Math.round((Date.now() - startedAt) / 1000),
      };
      const res = await api.post('/student/submissions', payload);
      setResult(res);
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <div className="center"><div className="spinner" /></div>;
  if (error && !test) return <div className="container"><div className="error">{error}</div></div>;

  if (result) return <ResultView result={result} test={test} />;

  return (
    <div className="container">
      <Link to="/student/tests" className="muted">&larr; Inapoi la teste</Link>
      <h1 style={{ marginBottom: 4 }}>{test.title}</h1>
      <p className="muted">
        {test.subject} - clasa {test.gradeLevel}{test.chapter ? ` - ${test.chapter}` : ''}
      </p>

      {error && <div className="error">{error}</div>}

      <form onSubmit={submit}>
        {test.questions.map((q, idx) => (
          <QuestionCard
            key={q.id}
            q={q}
            index={idx}
            value={answers[q.id]}
            onChange={(v) => setAnswer(q.id, v)}
          />
        ))}
        <div className="card flex-row between">
          <span className="muted">{Object.keys(answers).length}/{test.questions.length} raspunse</span>
          <button type="submit" className="btn" disabled={submitting}>
            {submitting ? 'Se trimite...' : 'Trimite testul'}
          </button>
        </div>
      </form>
    </div>
  );
}

function QuestionCard({ q, index, value, onChange }) {
  return (
    <div className="question-box">
      <div className="prompt">
        {index + 1}. {q.prompt}
      </div>
      {q.imageUrl && (
        <img src={q.imageUrl} alt="exercitiu" style={{ maxWidth: '100%', borderRadius: 8, marginBottom: 12 }} />
      )}
      {q.type === 'MULTIPLE_CHOICE' && (
        <div>
          {q.options.map((o) => (
            <label key={o.id} className={`option ${String(value) === String(o.id) ? 'selected' : ''}`}>
              <input
                type="radio"
                name={`q${q.id}`}
                value={o.id}
                checked={String(value) === String(o.id)}
                onChange={() => onChange(String(o.id))}
              />
              {o.text}
            </label>
          ))}
        </div>
      )}
      {q.type === 'TRUE_FALSE' && (
        <div>
          {[
            { v: 'true', label: 'Adevarat' },
            { v: 'false', label: 'Fals' },
          ].map((o) => (
            <label key={o.v} className={`option ${value === o.v ? 'selected' : ''}`}>
              <input
                type="radio"
                name={`q${q.id}`}
                value={o.v}
                checked={value === o.v}
                onChange={() => onChange(o.v)}
              />
              {o.label}
            </label>
          ))}
        </div>
      )}
      {q.type === 'SHORT_ANSWER' && (
        <input
          className="input"
          value={value || ''}
          onChange={(e) => onChange(e.target.value)}
          placeholder="Raspunsul tau..."
        />
      )}
    </div>
  );
}

function ResultView({ result, test }) {
  return (
    <div className="container">
      <div className="results-summary">
        <div style={{ fontSize: 14, opacity: 0.85 }}>Test finalizat</div>
        <h2 style={{ margin: '4px 0' }}>{result.testTitle}</h2>
        <div className="grade">{result.grade.toFixed(2)}</div>
        <div className="score">Scor: {result.score} / {result.maxScore}</div>
      </div>

      <h3>Raspunsurile tale</h3>
      {result.feedback.map((f, idx) => (
        <FeedbackCard key={f.questionId} f={f} index={idx} />
      ))}

      <div className="flex-row" style={{ marginTop: 18 }}>
        <Link to="/student/tests" className="btn btn-secondary">Inapoi la teste</Link>
        <Link to="/student/results" className="btn">Vezi toate rezultatele</Link>
      </div>
    </div>
  );
}

function FeedbackCard({ f, index }) {
  const cls = f.correct ? 'feedback-correct' : 'feedback-wrong';
  return (
    <div className="question-box">
      <div className="prompt">{index + 1}. {f.prompt}</div>
      <div className={cls}>
        <div className="feedback-row">
          <div className="label">Raspunsul tau</div>
          <div className={f.correct ? 'value value-correct' : 'value value-wrong'}>
            {f.givenAnswer || <em>(nicio selectie)</em>}
          </div>
        </div>
        {!f.correct && (
          <div className="correct-answer-banner">
            Raspuns corect: <strong>{f.correctAnswer}</strong>
          </div>
        )}
        {f.explanation && (
          <div className="muted" style={{ marginTop: 8 }}>
            <strong>Explicatie:</strong> {f.explanation}
          </div>
        )}
        <div className="muted" style={{ marginTop: 6 }}>
          {f.pointsEarned}/{f.pointsMax} puncte
        </div>
      </div>
    </div>
  );
}
