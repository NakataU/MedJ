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
  size: number
): Promise<Page<DocumentListOutView>> => {
  const res = await apiClient.get(`/document/all/${userId}`, {
    params: { page, size },
  });
  return res.data;
};

export const getAllUserDocuments = async (): Promise<DocumentListOutView[]> => {
  const res = await apiClient.get<Page<DocumentListOutView>>('/document/all/1', {
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