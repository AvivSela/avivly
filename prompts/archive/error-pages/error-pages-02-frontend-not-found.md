# Task: Frontend — NotFound Component + Route Registration

## Goal

1. Create `frontend/src/components/NotFound.jsx` — a 404 catch-all page.
2. Update `frontend/src/App.jsx` — import `NotFound` and add a `path="*"` wildcard route as the last `<Route>`.

These two changes must be done together because the component is useless without the route.

---

## File 1: Create `frontend/src/components/NotFound.jsx`

No existing file. Create it from scratch.

### Full component

```jsx
import { Link } from 'react-router-dom';

export default function NotFound() {
  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <header className="bg-blue-600 text-white px-6 py-4 shadow">
        <h1 className="text-2xl font-bold">URL Shortener</h1>
      </header>

      <main className="flex-1 flex items-center justify-center p-4">
        <div className="bg-white rounded-xl shadow p-10 text-center max-w-md w-full">
          <p className="text-8xl font-extrabold text-blue-600 leading-none mb-4">404</p>
          <h2 className="text-xl font-semibold text-gray-900 mb-2">Page Not Found</h2>
          <p className="text-sm text-gray-500 mb-6">
            The page you're looking for doesn't exist or has been moved.
          </p>
          <Link
            to="/"
            className="inline-block bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium px-6 py-2.5 rounded-lg"
          >
            Back to Dashboard
          </Link>
        </div>
      </main>
    </div>
  );
}
```

---

## File 2: Update `frontend/src/App.jsx`

Current relevant section (lines 1–8 and 77–85):

```jsx
// Existing imports (do not remove any)
import { useState, useEffect, useCallback } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import LinkForm from './components/LinkForm';
import LinksTable from './components/LinksTable';
import AnalyticsPanel from './components/AnalyticsPanel';
import LinkExpired from './components/LinkExpired';
import Footer from './components/Footer';
import { getLinks, deleteLink } from './api';

// ...

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={dashboard} />
        <Route path="/link-expired" element={<LinkExpired />} />
      </Routes>
    </BrowserRouter>
  );
```

### Required changes

1. Add import after the `LinkExpired` import line:
   ```jsx
   import NotFound from './components/NotFound';
   ```

2. Add the wildcard route as the **last** `<Route>` inside `<Routes>` (after `/link-expired`):
   ```jsx
   <Route path="*" element={<NotFound />} />
   ```

   The wildcard **must** come last to avoid shadowing `/link-expired`.

### Final `<Routes>` block after the change

```jsx
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={dashboard} />
        <Route path="/link-expired" element={<LinkExpired />} />
        <Route path="*" element={<NotFound />} />
      </Routes>
    </BrowserRouter>
  );
```

---

## Constraints

- Do not modify any other logic in `App.jsx`.
- No new dependencies — `react-router-dom` is already installed.
- Use Tailwind CSS utility classes only (no inline `style` props).
- No comments in source files.

---

## Verification

```bash
cd frontend && npm run dev
```

1. Navigate to `http://localhost:5173/unknown-path` → `NotFound` renders with "404" and "Page Not Found".
2. Click "Back to Dashboard" → lands on `/`.
3. Navigate to `/link-expired` → still renders the `LinkExpired` component (not `NotFound`).
4. Resize to 375 px width → no horizontal scroll, text is legible.
