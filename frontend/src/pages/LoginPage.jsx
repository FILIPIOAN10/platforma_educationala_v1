import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

export default function LoginPage() {
  const { login, user } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  if (user) {
    navigate('/');
    return null;
  }

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await login(username, password);
      navigate('/');
    } catch (err) {
      setError(err.message || 'Autentificare esuata');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">
        <h1>EduPlatform</h1>
        <p>Platforma de teste pentru clasele V-VIII</p>

        {error && <div className="error">{error}</div>}

        <form onSubmit={submit}>
          <div className="form-row">
            <label>Utilizator</label>
            <input
              className="input"
              autoFocus
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
          </div>
          <div className="form-row">
            <label>Parola</label>
            <input
              type="password"
              className="input"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>
          <button type="submit" className="btn" style={{ width: '100%' }} disabled={loading}>
            {loading ? 'Se autentifica...' : 'Autentifica-te'}
          </button>
        </form>

        <p className="muted" style={{ marginTop: 16 }}>
          Cont nou? <Link to="/register">Inregistreaza-te ca elev</Link>
        </p>

        <div className="demo-credentials">
          <strong>Conturi demo:</strong><br />
          Elevi: <code>elev5</code> / <code>elev6</code> / <code>elev7</code> / <code>elev8</code> &nbsp; (parola <code>elev123</code>)<br />
          Profesori: <code>prof.matei</code> / <code>prof.popescu</code> &nbsp; (<code>prof123</code>)<br />
          Admin: <code>admin</code> / <code>admin123</code><br />
          Parinte: <code>parinte</code> / <code>parinte123</code>
        </div>
      </div>
    </div>
  );
}
