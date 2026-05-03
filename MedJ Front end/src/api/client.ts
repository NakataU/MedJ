import axios from 'axios';

const API_BASE_URL = 'https://medj-back-end.onrender.com';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const getFileUrl = (path: string) => {
  return `${API_BASE_URL}${path}`;
};
