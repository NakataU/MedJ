import { apiClient } from './client';
import type { Page, PractitionerOutView, PractitionerCreateInput } from '../types';

export const getAllPractitioners = async (
  page: number = 0,
  size: number = 10
): Promise<Page<PractitionerOutView>> => {
  const response = await apiClient.get<Page<PractitionerOutView>>('/practitioner/all', {
    params: { page, size },
  });
  return response.data;
};

export const getPractitionerById = async (id: number): Promise<PractitionerOutView> => {
  const response = await apiClient.get<PractitionerOutView>(`/practitioner/${id}`);
  return response.data;
};

export const createPractitioner = async (
  data: PractitionerCreateInput
): Promise<PractitionerOutView> => {
  const response = await apiClient.post<PractitionerOutView>('/practitioner/add', data);
  return response.data;
};

export const updatePractitioner = async (
  id: number,
  data: PractitionerCreateInput
): Promise<PractitionerOutView> => {
  const response = await apiClient.put<PractitionerOutView>(`/practitioner/${id}`, data);
  return response.data;
};
