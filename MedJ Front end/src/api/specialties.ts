import { apiClient } from './client';
import type { Page, SpecialtyOutView } from '../types';

export const getAllSpecialties = async (
  page: number = 0,
  size: number = 100
): Promise<Page<SpecialtyOutView>> => {
  const response = await apiClient.get<Page<SpecialtyOutView>>('/specialty/all', {
    params: { page, size },
  });
  return response.data;
};
