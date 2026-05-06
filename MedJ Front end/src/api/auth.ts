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
