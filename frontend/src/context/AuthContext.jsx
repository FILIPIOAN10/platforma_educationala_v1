import { createContext, useContext, useEffect, useState } from 'react';
import { api } from '../api.js';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    api.get('/auth/me')
      .then((u) => { if (!cancelled) setUser(u); })
      .catch(() => { if (!cancelled) setUser(null); })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, []);

  const login = async (username, password) => {
    const u = await api.post('/auth/login', { username, password });
    setUser(u);
    return u;
  };

  const register = async (payload) => {
    const u = await api.post('/auth/register', payload);
    setUser(u);
    return u;
  };

  const logout = async () => {
    try { await api.post('/auth/logout'); } catch (_) {}
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, logout, register }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
