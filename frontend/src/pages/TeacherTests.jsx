import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api.js';
import { useAuth } from '../context/AuthContext.jsx';

export default function TeacherTests() {
  const { user } = useAuth();
  const isAdmin = user.role === 'ADMIN';
  const [tests, setTests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showAll, setShowAll] = useState(isAdmin);

  const reload = () => {
    setLoading(true);
    const url = showAll ? '/teacher/tests/all' : '/teacher/tests';
    api.get(url).then(setTests).finally(() => setLoading(false));
  };

  useEffect(reload, [showAll]);

  const remove = async (id) => {
    if (!window.confirm('Sigur vrei sa stergi testul?')) return;
    await api.del(`/teacher/tests/${id}`);
    reload();
  };

  if (loading) return <div className="center"><div className="spinner" /></div>;

  return (
    <div className="container">
      <div className="flex-row between">
        <h1>{showAll ? 'Toate testele' : 'Testele mele'}</h1>
        <div className="flex-row">
          {!isAdmin && (
            <button className="btn btn-secondary" onClick={() => setShowAll(!showAll)}>
              {showAll ? 'Doar testele mele' : 'Toate testele'}
            </button>
          )}
          <Link to="/teacher/tests/new" className="btn">+ Test nou</Link>
        </div>
      </div>

      <div className="card">
        {tests.length === 0 ? (
          <p className="muted">Nu exista teste.</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Titlu</th>
                <th>Materie</th>
                <th>Clasa</th>
                <th>Capitol</th>
                <th>Intrebari</th>
                <th>Autor</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {tests.map((t) => (
                <tr key={t.id}>
                  <td><strong>{t.title}</strong></td>
                  <td>{t.subject}</td>
                  <td>{t.gradeLevel}</td>
                  <td>{t.chapter || '-'}</td>
                  <td>{t.numberOfQuestions}</td>
                  <td>{t.creatorName || '-'}</td>
                  <td>
                    <Link to={`/teacher/tests/${t.id}`} className="btn btn-secondary" style={{ marginRight: 6, padding: '6px 10px' }}>
                      Editeaza
                    </Link>
                    <button className="btn btn-danger" style={{ padding: '6px 10px' }} onClick={() => remove(t.id)}>
                      Sterge
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
