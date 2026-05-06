import { createContext, useContext, useState, useEffect, type ReactNode } from 'react';
import { apiClient } from '../api/client';
import type { AuthResponse } from '../types';

interface AuthUser {
  id: number;
  username: string;
  role: 'ADMIN' | 'REGULAR';
  accessToken: string;
}

interface AuthContextType {
  user: AuthUser | null;
  isAuthenticated: boolean;
  loginUser: (response: AuthResponse) => void;
  logoutUser: () => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

const STORAGE_KEY = 'auth_user';

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(() => {
    const stored = localStorage.getItem(STORAGE_KEY);
    return stored ? JSON.parse(stored) : null;
  });

  // Attach the token to every outgoing request
  useEffect(() => {
    const interceptor = apiClient.interceptors.request.use((config) => {
      const stored = localStorage.getItem(STORAGE_KEY);
      if (stored) {
        const parsed: AuthUser = JSON.parse(stored);
        config.headers['Authorization'] = `Bearer ${parsed.accessToken}`;
      }
      return config;
    });
    return () => apiClient.interceptors.request.eject(interceptor);
  }, []);

  const loginUser = (response: AuthResponse) => {
    const authUser: AuthUser = {
      id: response.id,
      username: response.username,
      role: response.role,
      accessToken: response.accessToken,
    };
    localStorage.setItem(STORAGE_KEY, JSON.stringify(authUser));
    setUser(authUser);
  };

  const logoutUser = () => {
    localStorage.removeItem(STORAGE_KEY);
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
