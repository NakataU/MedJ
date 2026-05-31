import { apiClient } from './client';
import type { LoginRequest, RegisterRequest, ChangePasswordRequest, AuthResponse } from '../types';

export const login = async (data: LoginRequest): Promise<AuthResponse> => {
  const response = await apiClient.post<AuthResponse>('/user/login', data);
  return response.data;
};

export const register = async (data: RegisterRequest): Promise<void> => {
  await apiClient.post('/user/register', data);
};

export const changePassword = async (data: ChangePasswordRequest): Promise<void> => {
  await apiClient.put('/user/change-password', data);
};

export interface UserProfile {
  username: string;
  firstName: string | null;
  lastName: string | null;
  phone: string | null;
  address: string | null;
}

export const getProfile = async (userId: number): Promise<UserProfile> => {
  const res = await apiClient.get<UserProfile>(`/user/profile/${userId}`);
  return res.data;
};

export const updateProfile = async (
  userId: number,
  data: { firstName: string; lastName: string; phone: string; address: string }
): Promise<UserProfile> => {
  const res = await apiClient.put<UserProfile>(`/user/profile/${userId}`, data);
  return res.data;
};
