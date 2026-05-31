import { apiClient } from './client';
import type { DocumentListOutView } from '../types';

export interface SummaryResponse {
  summary: string;
  usedDocuments: DocumentListOutView[];
}

export const getMedicalSummary = async (prompt: string, lang: string): Promise<SummaryResponse> => {
  const response = await apiClient.post<SummaryResponse>('/document/medical-summary', { prompt, lang });
  return response.data;
};

export interface MedicalSummaryResult {
  qrBase64: string;
  pdfBase64: string;
}

export const generateMedicalSummary = async (summary: string, lang: string): Promise<MedicalSummaryResult> => {
  const response = await apiClient.post<MedicalSummaryResult>('/qr/generate', { summary, lang });
  return response.data;
};
