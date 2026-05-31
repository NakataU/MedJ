import { apiClient } from './client';
import type { Page, CategoryOutView, CategoryTarget, CategoryType } from '../types';

export const getAllCategories = async (
  target: CategoryTarget,
  page: number = 0,
  size: number = 100
): Promise<Page<CategoryOutView>> => {
  const response = await apiClient.get<Page<CategoryOutView>>('/category/all', {
    params: { target, page, size },
  });
  return response.data;
};

export const getCategoriesByType = async (
  target: CategoryTarget,
  categoryType: CategoryType
): Promise<CategoryOutView[]> => {
  const response = await apiClient.get<CategoryOutView[]>('/category/all/byType', {
    params: { target, categoryType },
  });
  return response.data;
};
