import { useEffect, useState } from 'react';
import { api } from '../api.js';

const ROLES = ['STUDENT', 'TEACHER', 'PARENT', 'ADMIN'];

export default function AdminUsers() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({
    username: '', password: '', fullName: '', email: '',
    role: 'STUDENT', gradeLevel: 5,
  });
  const [linkParent, setLinkParent] = useState({ parentId: '', studentId: '' });

  const reload = () => {
    setLoading(true);
    api.get('/admin/users').then(setUsers).finally(() => setLoading(false));
  };
  useEffect(reload, []);

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      await api.post('/admin/users', {
        ...form,
        gradeLevel: form.role === 'STUDENT' ? Number(form.gradeLevel) : null,
      });
      setShowForm(false);
      setForm({ username: '', password: '', fullName: '', email: '', role: 'STUDENT', gradeLevel: 5 });
      reload();
    } catch (err) { setError(err.message); }
  };

  const remove = async (id) => {
    if (!window.confirm('Stergi utilizatorul?')) return;
    await api.del(`/admin/users/${id}`);
    reload();
  };

  const link = async (e) => {
    e.preventDefault();
    setError('');
    try {
      await api.post(`/admin/parents/${linkParent.parentId}/children/${linkParent.studentId}`);
      setLinkParent({ parentId: '', studentId: '' });
      reload();
    } catch (err) { setError(err.message); }
  };

  const unlink = async (parentId, childId) => {
    await api.del(`/admin/parents/${parentId}/children/${childId}`);
    reload();
  };

  if (loading) return <div className="center"><div className="spinner" /></div>;

  const parents = users.filter((u) => u.role === 'PARENT');
  const students = users.filter((u) => u.role === 'STUDENT');

  return (
    <div className="container">
      <div className="flex-row between">
        <h1>Utilizatori</h1>
        <button className="btn" onClick={() => setShowForm(!showForm)}>
          {showForm ? 'Inchide' : '+ Utilizator nou'}
        </button>
      </div>

      {error && <div className="error">{error}</div>}

      {showForm && (
        <div className="card">
          <h3>Cont nou</h3>
          <form onSubmit={submit}>
            <div className="grid grid-2">
              <div className="form-row">
                <label>Username</label>
                <input className="input" required value={form.username}
                       onChange={(e) => setForm({ ...form, username: e.target.value })} />
              </div>
              <div className="form-row">
                <label>Nume complet</label>
                <input className="input" required value={form.fullName}
                       onChange={(e) => setForm({ ...form, fullName: e.target.value })} />
              </div>
              <div className="form-row">
                <label>Email</label>
                <input className="input" type="email" value={form.email}
                       onChange={(e) => setForm({ ...form, email: e.target.value })} />
              </div>
              <div className="form-row">
                <label>Parola</label>
                <input className="input" type="password" required value={form.password}
                       onChange={(e) => setForm({ ...form, password: e.target.value })} />
              </div>
              <div className="form-row">
                <label>Rol</label>
                <select className="select" value={form.role}
                        onChange={(e) => setForm({ ...form, role: e.target.value })}>
                  {ROLES.map((r) => <option key={r}>{r}</option>)}
                </select>
              </div>
              {form.role === 'STUDENT' && (
                <div className="form-row">
                  <label>Clasa</label>
                  <select className="select" value={form.gradeLevel}
                          onChange={(e) => setForm({ ...form, gradeLevel: e.target.value })}>
                    {[5, 6, 7, 8].map((g) => <option key={g} value={g}>Clasa {g}</option>)}
                  </select>
                </div>
              )}
            </div>
            <button className="btn" type="submit">Creeaza</button>
          </form>
        </div>
      )}

      <div className="card">
        <h3>Toti utilizatorii</h3>
        <table>
          <thead>
            <tr>
              <th>Username</th><th>Nume</th><th>Rol</th><th>Clasa</th><th>Email</th><th>Copii</th><th></th>
            </tr>
          </thead>
          <tbody>
            {users.map((u) => (
              <tr key={u.id}>
                <td><code>{u.username}</code></td>
                <td>{u.fullName}</td>
                <td>{u.role}</td>
                <td>{u.gradeLevel || '-'}</td>
                <td>{u.email || '-'}</td>
                <td>
                  {u.role === 'PARENT' && u.childrenIds.length > 0
                    ? u.childrenIds.map((cid) => {
                        const c = users.find((x) => x.id === cid);
                        return (
                          <span key={cid} style={{ marginRight: 6 }}>
                            {c ? c.username : `#${cid}`}{' '}
                            <button className="btn btn-secondary" style={{ padding: '2px 6px', fontSize: 11 }}
                                    onClick={() => unlink(u.id, cid)}>x</button>
                          </span>
                        );
                      })
                    : '-'}
                </td>
                <td>
                  <button className="btn btn-danger" style={{ padding: '4px 8px' }} onClick={() => remove(u.id)}>
                    Sterge
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="card">
        <h3>Asociaza parinte ↔ elev</h3>
        <form onSubmit={link} className="flex-row" style={{ gap: 12 }}>
          <select className="select" required value={linkParent.parentId}
                  onChange={(e) => setLinkParent({ ...linkParent, parentId: e.target.value })}>
            <option value="">Parinte...</option>
            {parents.map((p) => <option key={p.id} value={p.id}>{p.fullName} (@{p.username})</option>)}
          </select>
          <select className="select" required value={linkParent.studentId}
                  onChange={(e) => setLinkParent({ ...linkParent, studentId: e.target.value })}>
            <option value="">Elev...</option>
            {students.map((s) => <option key={s.id} value={s.id}>{s.fullName} (@{s.username}, cls.{s.gradeLevel})</option>)}
          </select>
          <button className="btn" type="submit">Asociaza</button>
        </form>
      </div>
    </div>
  );
}
