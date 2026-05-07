import axios from 'axios';

const API_BASE_URL = 'https://medj-back-end.onrender.com';
//For local tests
//const API_BASE_URL = 'http://localhost:8080';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const getFileUrl = (path: string) => {
  return `${API_BASE_URL}${path}`;
};
