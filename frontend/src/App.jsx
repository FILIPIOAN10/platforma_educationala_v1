import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from './context/AuthContext.jsx';
import Navbar from './components/Navbar.jsx';
import LoginPage from './pages/LoginPage.jsx';
import RegisterPage from './pages/RegisterPage.jsx';
import StudentDashboard from './pages/StudentDashboard.jsx';
import TestList from './pages/TestList.jsx';
import TestTaking from './pages/TestTaking.jsx';
import StudentResults from './pages/StudentResults.jsx';
import TeacherDashboard from './pages/TeacherDashboard.jsx';
import TeacherTests from './pages/TeacherTests.jsx';
import TestEditor from './pages/TestEditor.jsx';
import AdminDashboard from './pages/AdminDashboard.jsx';
import AdminUsers from './pages/AdminUsers.jsx';
import ParentDashboard from './pages/ParentDashboard.jsx';

function Protected({ children, roles }) {
  const { user, loading } = useAuth();
  if (loading) return <div className="center"><div className="spinner" /></div>;
  if (!user) return <Navigate to="/login" replace />;
  if (roles && !roles.includes(user.role)) return <Navigate to="/" replace />;
  return children;
}

function HomeRedirect() {
  const { user, loading } = useAuth();
  if (loading) return <div className="center"><div className="spinner" /></div>;
  if (!user) return <Navigate to="/login" replace />;
  switch (user.role) {
    case 'STUDENT': return <Navigate to="/student" replace />;
    case 'TEACHER': return <Navigate to="/teacher" replace />;
    case 'ADMIN': return <Navigate to="/admin" replace />;
    case 'PARENT': return <Navigate to="/parent" replace />;
    default: return <Navigate to="/login" replace />;
  }
}

export default function App() {
  return (
    <div className="app">
      <Navbar />
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/" element={<HomeRedirect />} />

        <Route path="/student" element={<Protected roles={['STUDENT']}><StudentDashboard /></Protected>} />
        <Route path="/student/tests" element={<Protected roles={['STUDENT']}><TestList /></Protected>} />
        <Route path="/student/tests/:id" element={<Protected roles={['STUDENT']}><TestTaking /></Protected>} />
        <Route path="/student/results" element={<Protected roles={['STUDENT']}><StudentResults /></Protected>} />
        <Route path="/student/results/:id" element={<Protected roles={['STUDENT']}><StudentResults /></Protected>} />

        <Route path="/teacher" element={<Protected roles={['TEACHER','ADMIN']}><TeacherDashboard /></Protected>} />
        <Route path="/teacher/tests" element={<Protected roles={['TEACHER','ADMIN']}><TeacherTests /></Protected>} />
        <Route path="/teacher/tests/new" element={<Protected roles={['TEACHER','ADMIN']}><TestEditor /></Protected>} />
        <Route path="/teacher/tests/:id" element={<Protected roles={['TEACHER','ADMIN']}><TestEditor /></Protected>} />

        <Route path="/admin" element={<Protected roles={['ADMIN']}><AdminDashboard /></Protected>} />
        <Route path="/admin/users" element={<Protected roles={['ADMIN']}><AdminUsers /></Protected>} />

        <Route path="/parent" element={<Protected roles={['PARENT']}><ParentDashboard /></Protected>} />

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </div>
  );
}
