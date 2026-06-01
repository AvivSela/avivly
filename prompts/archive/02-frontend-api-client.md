# Agent Prompt: Frontend API Client (TASK-18)

## Project Context
You are building an **analytics-driven URL shortener**.
The frontend Vite + React project already exists at `frontend/` with `axios` as a dependency.
Vite is configured to proxy `/api` → `http://localhost:8080`.

## Your Task
Create the centralized API client module used by all frontend components.

## Files to Create

### `frontend/src/api.js`
```js
import axios from 'axios';

const api = axios.create({ baseURL: '/api' });

export const getLinks = () => api.get('/links');
export const createLink = (data) => api.post('/links', data);
export const updateLink = (id, data) => api.put(`/links/${id}`, data);
export const deleteLink = (id) => api.delete(`/links/${id}`);
export const getAnalytics = (shortCode) => api.get(`/links/${shortCode}/analytics`);
```

## Acceptance Criteria
- File is at `frontend/src/api.js`
- All 5 functions are exported: `getLinks`, `createLink`, `updateLink`, `deleteLink`, `getAnalytics`
- Base URL is `/api` — NOT hardcoded to `http://localhost:8080` (Vite proxy handles it in dev, nginx handles it in production)
- Uses `axios.create` with `baseURL` so all requests are relative
