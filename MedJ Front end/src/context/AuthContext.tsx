import { createContext, useContext, useState, useEffect, type ReactNode } from 'react';
import { apiClient } from '../api/client';

interface AuthUser {
  email: string;
  firstName: string;
  token: string;
}

interface AuthContextType {
  user: AuthUser | null;
  isAuthenticated: boolean;
  loginUser: (token: string, email: string, firstName: string) => void;
  logoutUser: () => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(() => {
    const stored = localStorage.getItem('auth_user');
    return stored ? JSON.parse(stored) : null;
  });

  useEffect(() => {
    const interceptor = apiClient.interceptors.request.use((config) => {
      const stored = localStorage.getItem('auth_user');
      if (stored) {
        const parsed: AuthUser = JSON.parse(stored);
        config.headers['Authorization'] = `Bearer ${parsed.token}`;
      }
      return config;
    });
    return () => apiClient.interceptors.request.eject(interceptor);
  }, []);

  const loginUser = (token: string, email: string, firstName: string) => {
    const authUser: AuthUser = { token, email, firstName };
    localStorage.setItem('auth_user', JSON.stringify(authUser));
    setUser(authUser);
  };

  const logoutUser = () => {
    localStorage.removeItem('auth_user');
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, isAuthenticated: !!user, loginUser, logoutUser }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider');
  return ctx;
}
