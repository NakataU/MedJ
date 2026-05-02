import { apiClient } from './client';

export const getMedicalSummary = async (prompt: string): Promise<string> => {
  const response = await apiClient.post<string>('/document/medical-summary', JSON.stringify(prompt));
  return response.data;
};

export interface MedicalSummaryResult {
  qrBase64: string;
  pdfBase64: string;
}

export const generateMedicalSummary = async (prompt: string): Promise<MedicalSummaryResult> => {
  const response = await apiClient.post<MedicalSummaryResult>('/qr/generate', JSON.stringify(prompt));
  return response.data;
};
