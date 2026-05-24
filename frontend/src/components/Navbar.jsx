import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

const ROLE_LABEL = {
  STUDENT: 'Elev',
  TEACHER: 'Profesor',
  ADMIN: 'Administrator',
  PARENT: 'Parinte',
};

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  if (!user) {
    return (
      <nav className="navbar">
        <Link to="/" className="brand">
          <span className="logo">Edu</span>
          <span>Platform</span>
        </Link>
        <div className="nav-user">
          <Link to="/login" className="btn btn-ghost">Autentificare</Link>
        </div>
      </nav>
    );
  }

  return (
    <nav className="navbar">
      <Link to="/" className="brand">
        <span className="logo">Edu</span>
        <span>Platform</span>
      </Link>
      <div className="nav-links">
        {user.role === 'STUDENT' && (
          <>
            <NavLink to="/student">Acasa</NavLink>
            <NavLink to="/student/tests">Teste</NavLink>
            <NavLink to="/student/results">Rezultate</NavLink>
          </>
        )}
        {user.role === 'TEACHER' && (
          <>
            <NavLink to="/teacher">Dashboard</NavLink>
            <NavLink to="/teacher/tests">Testele mele</NavLink>
            <NavLink to="/teacher/tests/new">Creeaza test</NavLink>
          </>
        )}
        {user.role === 'ADMIN' && (
          <>
            <NavLink to="/admin">Analytics</NavLink>
            <NavLink to="/admin/users">Utilizatori</NavLink>
            <NavLink to="/teacher/tests">Teste</NavLink>
          </>
        )}
        {user.role === 'PARENT' && (
          <>
            <NavLink to="/parent">Dashboard copii</NavLink>
          </>
        )}
      </div>
      <div className="nav-user">
        <span style={{ opacity: 0.9 }}>
          {user.fullName}
          {user.gradeLevel ? ` (cls. ${user.gradeLevel})` : ''} - {ROLE_LABEL[user.role] || user.role}
        </span>
        <button className="btn btn-ghost" onClick={async () => { await logout(); navigate('/login'); }}>
          Logout
        </button>
      </div>
    </nav>
  );
}
