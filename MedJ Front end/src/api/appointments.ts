import { apiClient } from './client';
import type { Page, AppointmentOutView, AppointmentCreateInput } from '../types';

export const linkDocumentsToAppointment = async (
  appointmentId: number,
  documentIds: number[]
): Promise<void> => {
  await apiClient.put(`/appointment/${appointmentId}/documents`, documentIds);
};

export const getAllAppointments = async (
  page: number = 0,
  size: number = 10
): Promise<Page<AppointmentOutView>> => {
  const response = await apiClient.get<Page<AppointmentOutView>>('/appointment/all', {
    params: { page, size },
  });
  console.log('appointments response:', JSON.stringify(response.data.content[0], null, 2));
  return response.data;
};

export const getAppointmentById = async (id: number): Promise<AppointmentOutView | null> => {
  const response = await apiClient.get<AppointmentOutView>(`/appointment/${id}`);
  return response.data;
};

export const addPractitionerToAppointment = async (
  appointmentId: number,
  practitionerId: number
): Promise<AppointmentOutView> => {
  const response = await apiClient.put<AppointmentOutView>(
    `/appointment/${appointmentId}/practitioner/${practitionerId}`
  );
  return response.data;
};

export const updateAppointment = async (
  id: number,
  appointment: AppointmentCreateInput
): Promise<AppointmentOutView> => {
  const response = await apiClient.put<AppointmentOutView>(`/appointment/${id}`, {
    name: appointment.name,
    place: appointment.place,
    date: appointment.date,
    practitionerId: appointment.practitionerId ?? null,
  });
  return response.data;
};

export const createAppointment = async (
  appointment: AppointmentCreateInput,
  documentIds?: number[]
): Promise<AppointmentOutView> => {
  const formData = new FormData();

  formData.append(
    'appointment',
    new Blob([JSON.stringify({
      name: appointment.name,
      place: appointment.place,
      date: appointment.date,
      practitionerId: appointment.practitionerId ?? null,
      documents: documentIds ?? [],
    })], { type: 'application/json' })
  );

  const response = await apiClient.post<AppointmentOutView>('/appointment/add', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
  return response.data;
};
