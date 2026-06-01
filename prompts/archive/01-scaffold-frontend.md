# Agent Prompt: Scaffold React Frontend (TASK-02)

## Project Context
You are building an **analytics-driven URL shortener** from scratch.
Working directory: project root (where `frontend/` will live).

## Your Task
Create the Vite + React frontend project structure.

## Files to Create

### `frontend/package.json`
```json
{
  "name": "urlshortener-frontend",
  "private": true,
  "version": "0.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "axios": "^1.6.0",
    "react": "^18.2.0",
    "react-dom": "^18.2.0",
    "recharts": "^2.10.0"
  },
  "devDependencies": {
    "@tailwindcss/vite": "^4.0.0",
    "@vitejs/plugin-react": "^4.2.0",
    "tailwindcss": "^4.0.0",
    "vite": "^5.0.0"
  }
}
```

### `frontend/vite.config.js`
```js
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [
    react(),
    tailwindcss(),
  ],
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
```

### `frontend/index.html`
```html
<!doctype html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>URL Shortener</title>
  </head>
  <body>
    <div id="root"></div>
    <script type="module" src="/src/main.jsx"></script>
  </body>
</html>
```

### `frontend/src/main.jsx`
```jsx
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
```

### `frontend/src/App.jsx`
```jsx
export default function App() {
  return <div>URL Shortener</div>
}
```

### `frontend/tailwind.config.js`
```js
/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {},
  },
  plugins: [],
}
```

### `frontend/src/index.css`
```css
@import "tailwindcss";
```

## Acceptance Criteria
- All 7 files exist in the correct paths
- `npm --prefix frontend install` exits 0
- `npm --prefix frontend run build` produces `frontend/dist/` directory
- Vite proxy for `/api` points to `http://localhost:8080`
