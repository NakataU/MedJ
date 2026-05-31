import { apiClient } from './client';
import type { Page, DocumentListOutView, DocumentOutView } from '../types';

export const getDocumentsByAppointmentId = async (
  appointmentId: number,
  page: number,
  size: number
): Promise<Page<DocumentListOutView>> => {
  const res = await apiClient.get(`/document/all/byAppointment/${appointmentId}`, {
    params: { page, size },
  });
  return res.data;
};

export const uploadDocuments = async (
  files: File[],
  documentCategoryIds?: (number | null)[]
): Promise<DocumentOutView[]> => {
  const formData = new FormData();
  files.forEach((file) => formData.append('document', file));
  if (documentCategoryIds && documentCategoryIds.length > 0) {
    documentCategoryIds.forEach((id) => {
      formData.append('documentCategories', id !== null ? String(id) : '');
    });
  }
  const res = await apiClient.post<DocumentOutView[]>('/document/add', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return res.data;
};

export const getDocumentsByUserId = async (
  userId: number,
  page: number,
  size: number,
  filters?: { documentTypeId?: number; medicalSpecialtyId?: number; medicalCategoryId?: number }
): Promise<Page<DocumentListOutView>> => {
  const params: Record<string, number> = { page, size };
  if (filters?.documentTypeId) params.documentTypeId = filters.documentTypeId;
  if (filters?.medicalSpecialtyId) params.medicalSpecialtyId = filters.medicalSpecialtyId;
  if (filters?.medicalCategoryId) params.medicalCategoryId = filters.medicalCategoryId;
  const res = await apiClient.get(`/document/all/${userId}`, { params });
  return res.data;
};

export const getAllUserDocuments = async (userId: number): Promise<DocumentListOutView[]> => {
  const res = await apiClient.get<Page<DocumentListOutView>>(`/document/all/${userId}`, {
    params: { page: 0, size: 200 },
  });
  return res.data.content;
};

export const getDocumentById = async (id: number): Promise<DocumentOutView> => {
  const res = await apiClient.get(`/document/${id}`);
  return res.data;
};

export const deleteDocument = async (id: number): Promise<void> => {
  await apiClient.delete(`/document/${id}`);
};

export const getPreviewUrl = (id: number): string =>
  `${apiClient.defaults.baseURL}/document/${id}/content`;

export const getDownloadUrl = (id: number): string =>
  `${apiClient.defaults.baseURL}/document/${id}/download`;

export const fetchDocumentBlob = async (id: number): Promise<string> => {
  const res = await apiClient.get(`/document/${id}/content`, {
    responseType: 'blob',
  });
  return URL.createObjectURL(res.data);
};

export const fetchDocumentDownloadBlob = async (id: number): Promise<string> => {
  const res = await apiClient.get(`/document/${id}/download`, {
    responseType: 'blob',
  });
  return URL.createObjectURL(res.data);
};

export const updateDocumentCategories = async (
  id: number,
  categories: { documentTypeId: number; medicalSpecialtyId: number; medicalCategoryId: number }
): Promise<void> => {
  await apiClient.put(`/document/${id}/categories`, categories);
};

export const updateDocumentContent = async (
  id: number,
  content: string
): Promise<DocumentOutView> => {
  const res = await apiClient.put<DocumentOutView>(`/document/${id}/content`, content, {
    headers: { 'Content-Type': 'text/plain' },
  });
  return res.data;
};