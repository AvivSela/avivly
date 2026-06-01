import axios from 'axios';

const api = axios.create({ baseURL: '/api' });

export const getLinks = () => api.get('/links');
export const createLink = (data) => api.post('/links', data);
export const updateLink = (id, data) => api.put(`/links/${id}`, data);
export const deleteLink = (id) => api.delete(`/links/${id}`);
export const getAnalytics = (shortCode) => api.get(`/links/${shortCode}/analytics`);
