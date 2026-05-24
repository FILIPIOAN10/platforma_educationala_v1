import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    username: '', password: '', fullName: '', email: '', gradeLevel: 5,
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const onChange = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await register({
        ...form,
        role: 'STUDENT',
        gradeLevel: Number(form.gradeLevel),
      });
      navigate('/');
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">
        <h1>Inregistrare</h1>
        <p>Cont de elev nou (clasa V-VIII)</p>
        {error && <div className="error">{error}</div>}
        <form onSubmit={submit}>
          <div className="form-row">
            <label>Nume complet</label>
            <input className="input" required value={form.fullName} onChange={onChange('fullName')} />
          </div>
          <div className="form-row">
            <label>Utilizator</label>
            <input className="input" required value={form.username} onChange={onChange('username')} />
          </div>
          <div className="form-row">
            <label>Email</label>
            <input className="input" type="email" value={form.email} onChange={onChange('email')} />
          </div>
          <div className="form-row">
            <label>Parola</label>
            <input className="input" type="password" required value={form.password} onChange={onChange('password')} />
          </div>
          <div className="form-row">
            <label>Clasa</label>
            <select className="select" value={form.gradeLevel} onChange={onChange('gradeLevel')}>
              <option value="5">a V-a</option>
              <option value="6">a VI-a</option>
              <option value="7">a VII-a</option>
              <option value="8">a VIII-a</option>
            </select>
          </div>
          <button className="btn" type="submit" style={{ width: '100%' }} disabled={loading}>
            {loading ? 'Se trimite...' : 'Creeaza cont'}
          </button>
        </form>
        <p className="muted" style={{ marginTop: 14 }}>
          Ai deja cont? <Link to="/login">Intra in cont</Link>
        </p>
      </div>
    </div>
  );
}
