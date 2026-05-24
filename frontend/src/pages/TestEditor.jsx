import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { api } from '../api.js';

const SUBJECTS = ['MATEMATICA', 'ROMANA', 'ISTORIE', 'GEOGRAFIE', 'BIOLOGIE', 'FIZICA', 'CHIMIE', 'ENGLEZA', 'INFORMATICA'];
const DIFFICULTIES = ['USOR', 'MEDIU', 'GREU'];

const blankQuestion = (orderIndex) => ({
  type: 'MULTIPLE_CHOICE',
  prompt: '',
  imageUrl: '',
  correctAnswerText: '',
  explanation: '',
  points: 1,
  orderIndex,
  options: [
    { text: '', correct: true, orderIndex: 0 },
    { text: '', correct: false, orderIndex: 1 },
  ],
});

export default function TestEditor() {
  const { id } = useParams();
  const isEdit = Boolean(id);
  const navigate = useNavigate();

  const [test, setTest] = useState({
    title: '', description: '', subject: 'MATEMATICA', gradeLevel: 5,
    chapter: '', difficulty: 'USOR', questions: [blankQuestion(0)],
  });
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);
  const [loading, setLoading] = useState(isEdit);

  useEffect(() => {
    if (isEdit) {
      api.get(`/teacher/tests/${id}`).then((t) => {
        setTest({
          title: t.title, description: t.description || '',
          subject: t.subject, gradeLevel: t.gradeLevel,
          chapter: t.chapter || '', difficulty: t.difficulty || 'USOR',
          questions: t.questions.map((q, i) => ({
            type: q.type, prompt: q.prompt, imageUrl: q.imageUrl || '',
            correctAnswerText: q.correctAnswerText || '',
            explanation: q.explanation || '',
            points: q.points || 1, orderIndex: q.orderIndex ?? i,
            options: (q.options || []).map((o, oi) => ({
              text: o.text, correct: !!o.correct, orderIndex: o.orderIndex ?? oi,
            })),
          })),
        });
      }).finally(() => setLoading(false));
    }
  }, [id, isEdit]);

  const setField = (k, v) => setTest({ ...test, [k]: v });

  const setQ = (qi, q) => {
    const qs = [...test.questions];
    qs[qi] = q;
    setTest({ ...test, questions: qs });
  };

  const addQuestion = () => setTest({
    ...test,
    questions: [...test.questions, blankQuestion(test.questions.length)],
  });

  const removeQuestion = (qi) => {
    const qs = test.questions.filter((_, i) => i !== qi).map((q, i) => ({ ...q, orderIndex: i }));
    setTest({ ...test, questions: qs });
  };

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    setSaving(true);
    try {
      const payload = {
        ...test,
        gradeLevel: Number(test.gradeLevel),
        questions: test.questions.map((q, i) => ({
          ...q,
          orderIndex: i,
          points: Number(q.points) || 1,
          options: q.type === 'MULTIPLE_CHOICE' ? (q.options || []).map((o, oi) => ({
            ...o, orderIndex: oi,
          })) : [],
        })),
      };
      if (isEdit) {
        await api.put(`/teacher/tests/${id}`, payload);
      } else {
        await api.post('/teacher/tests', payload);
      }
      navigate('/teacher/tests');
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <div className="center"><div className="spinner" /></div>;

  return (
    <div className="container">
      <h1>{isEdit ? 'Editare test' : 'Test nou'}</h1>

      {error && <div className="error">{error}</div>}

      <form onSubmit={submit}>
        <div className="card">
          <div className="grid grid-2">
            <div className="form-row">
              <label>Titlu</label>
              <input className="input" required value={test.title}
                     onChange={(e) => setField('title', e.target.value)} />
            </div>
            <div className="form-row">
              <label>Materie</label>
              <select className="select" value={test.subject}
                      onChange={(e) => setField('subject', e.target.value)}>
                {SUBJECTS.map((s) => <option key={s}>{s}</option>)}
              </select>
            </div>
            <div className="form-row">
              <label>Clasa</label>
              <select className="select" value={test.gradeLevel}
                      onChange={(e) => setField('gradeLevel', Number(e.target.value))}>
                {[5, 6, 7, 8].map((g) => <option key={g} value={g}>a {['','','','','','V','VI','VII','VIII'][g]}-a</option>)}
              </select>
            </div>
            <div className="form-row">
              <label>Dificultate</label>
              <select className="select" value={test.difficulty}
                      onChange={(e) => setField('difficulty', e.target.value)}>
                {DIFFICULTIES.map((d) => <option key={d}>{d}</option>)}
              </select>
            </div>
            <div className="form-row">
              <label>Capitol</label>
              <input className="input" value={test.chapter}
                     onChange={(e) => setField('chapter', e.target.value)} />
            </div>
            <div className="form-row">
              <label>Descriere</label>
              <textarea className="textarea" rows={2} value={test.description}
                        onChange={(e) => setField('description', e.target.value)} />
            </div>
          </div>
        </div>

        {test.questions.map((q, qi) => (
          <QuestionEditor
            key={qi}
            q={q}
            index={qi}
            onChange={(nq) => setQ(qi, nq)}
            onRemove={() => removeQuestion(qi)}
          />
        ))}

        <div className="flex-row" style={{ gap: 10 }}>
          <button type="button" className="btn btn-secondary" onClick={addQuestion}>
            + Adauga intrebare
          </button>
          <button type="submit" className="btn" disabled={saving}>
            {saving ? 'Se salveaza...' : (isEdit ? 'Salveaza modificarile' : 'Creeaza testul')}
          </button>
        </div>
      </form>
    </div>
  );
}

function QuestionEditor({ q, index, onChange, onRemove }) {
  const setF = (k, v) => onChange({ ...q, [k]: v });

  const setOpt = (oi, ko, vo) => {
    const opts = [...q.options];
    opts[oi] = { ...opts[oi], [ko]: vo };
    if (ko === 'correct' && vo) {
      // single correct
      opts.forEach((o, i) => { if (i !== oi) o.correct = false; });
    }
    onChange({ ...q, options: opts });
  };
  const addOpt = () => onChange({ ...q, options: [...q.options, { text: '', correct: false, orderIndex: q.options.length }] });
  const rmOpt = (oi) => onChange({ ...q, options: q.options.filter((_, i) => i !== oi) });

  return (
    <div className="card">
      <div className="flex-row between">
        <h3>Intrebarea {index + 1}</h3>
        <button type="button" className="btn btn-danger" style={{ padding: '6px 12px' }} onClick={onRemove}>
          Sterge
        </button>
      </div>

      <div className="grid grid-2">
        <div className="form-row">
          <label>Tip</label>
          <select className="select" value={q.type}
                  onChange={(e) => setF('type', e.target.value)}>
            <option value="MULTIPLE_CHOICE">Varianta multipla</option>
            <option value="TRUE_FALSE">Adevarat / Fals</option>
            <option value="SHORT_ANSWER">Raspuns scurt</option>
          </select>
        </div>
        <div className="form-row">
          <label>Punctaj</label>
          <input type="number" min="1" className="input" value={q.points}
                 onChange={(e) => setF('points', Number(e.target.value))} />
        </div>
      </div>

      <div className="form-row">
        <label>Enunt</label>
        <textarea className="textarea" rows={2} required value={q.prompt}
                  onChange={(e) => setF('prompt', e.target.value)} />
      </div>

      <div className="form-row">
        <label>URL imagine (optional, ex: exercitiu scanat)</label>
        <input className="input" value={q.imageUrl}
               onChange={(e) => setF('imageUrl', e.target.value)}
               placeholder="https://..." />
      </div>

      {q.type === 'MULTIPLE_CHOICE' && (
        <div>
          <label style={{ fontWeight: 600, fontSize: 13 }}>Optiuni (marcheaza raspunsul corect)</label>
          {q.options.map((o, oi) => (
            <div key={oi} className="flex-row" style={{ marginTop: 6 }}>
              <input
                type="radio"
                name={`q${index}-correct`}
                checked={!!o.correct}
                onChange={() => setOpt(oi, 'correct', true)}
              />
              <input
                className="input"
                style={{ flex: 1 }}
                value={o.text}
                onChange={(e) => setOpt(oi, 'text', e.target.value)}
                placeholder={`Optiunea ${oi + 1}`}
              />
              <button type="button" className="btn btn-secondary" style={{ padding: '6px 10px' }}
                      onClick={() => rmOpt(oi)} disabled={q.options.length <= 2}>
                X
              </button>
            </div>
          ))}
          <button type="button" className="btn btn-secondary" style={{ marginTop: 8, padding: '6px 12px' }}
                  onClick={addOpt}>+ Optiune</button>
        </div>
      )}

      {q.type === 'TRUE_FALSE' && (
        <div className="form-row">
          <label>Raspuns corect</label>
          <select className="select" value={q.correctAnswerText || 'true'}
                  onChange={(e) => setF('correctAnswerText', e.target.value)}>
            <option value="true">Adevarat</option>
            <option value="false">Fals</option>
          </select>
        </div>
      )}

      {q.type === 'SHORT_ANSWER' && (
        <div className="form-row">
          <label>Raspuns corect (text exact, case-insensitive)</label>
          <input className="input" required value={q.correctAnswerText}
                 onChange={(e) => setF('correctAnswerText', e.target.value)} />
        </div>
      )}

      <div className="form-row">
        <label>Explicatie (afisata dupa raspunsul gresit)</label>
        <textarea className="textarea" rows={2} value={q.explanation}
                  onChange={(e) => setF('explanation', e.target.value)} />
      </div>
    </div>
  );
}
