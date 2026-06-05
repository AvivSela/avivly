# Task: Frontend — LinkExpired Redesign

## Goal

Fully rewrite `frontend/src/components/LinkExpired.jsx`.

The current file is a minimal placeholder (9 lines). Replace it with a polished, consistent error page that matches the visual style of the rest of the app.

No other files change in this task.

---

## Current file (for reference — replace entirely)

```jsx
export default function LinkExpired() {
  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center">
      <div className="bg-white rounded-xl shadow p-6 text-center">
        <p className="text-gray-600 text-sm">This link is no longer available.</p>
      </div>
    </div>
  );
}
```

---

## New implementation

### Full component

```jsx
import { Link } from 'react-router-dom';

export default function LinkExpired() {
  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <header className="bg-blue-600 text-white px-6 py-4 shadow">
        <h1 className="text-2xl font-bold">URL Shortener</h1>
      </header>

      <main className="flex-1 flex items-center justify-center p-4">
        <div className="bg-white rounded-xl shadow p-10 text-center max-w-md w-full">
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-red-50 mb-4">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="32"
              height="32"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
              strokeWidth={2}
              strokeLinecap="round"
              strokeLinejoin="round"
              className="text-red-500"
            >
              <path d="M3.98 8.223A10.477 10.477 0 001.934 12C3.226 16.338 7.244 19.5 12 19.5c.993 0 1.953-.138 2.863-.395M6.228 6.228A10.45 10.45 0 0112 4.5c4.756 0 8.773 3.162 10.065 7.498a10.523 10.523 0 01-4.293 5.774M6.228 6.228L3 3m3.228 3.228l3.65 3.65m7.894 7.894L21 21m-3.228-3.228l-3.65-3.65m0 0a3 3 0 10-4.243-4.243m4.242 4.242L9.88 9.88" />
            </svg>
          </div>

          <h2 className="text-xl font-semibold text-gray-900 mb-2">Link Unavailable</h2>
          <p className="text-sm text-gray-500 mb-6">
            This link has expired, reached its click limit, or no longer exists.
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

### Design notes

- **Header:** same `bg-blue-600` header as `App.jsx` — `<header className="bg-blue-600 text-white px-6 py-4 shadow">`.
- **Icon:** Heroicons `eye-slash` outline, inline SVG. Rendered inside a `rounded-full bg-red-50` container; `className="text-red-500"` on the `<svg>` so `stroke="currentColor"` inherits the red colour.
- **Card:** matches the `NotFound` card — `bg-white rounded-xl shadow p-10 text-center max-w-md w-full`.
- **CTA:** `Link to="/"` with identical Tailwind classes to `NotFound`.
- No changes to routing — `/link-expired` route stays in `App.jsx` and `RedirectController` still redirects expired codes there.

---

## Constraints

- Use `Link` from `react-router-dom` for the CTA (already a project dependency).
- Tailwind CSS utility classes only — no inline `style` props.
- No new dependencies.
- No comments in source files.

---

## Verification

```bash
cd frontend && npm run dev
```

1. Navigate to `http://localhost:5173/link-expired` → redesigned page renders with the eye-slash icon in a red circle, "Link Unavailable" heading, and "Back to Dashboard" button.
2. Click "Back to Dashboard" → lands on `/`.
3. Resize to 375 px width → no horizontal scroll, content is centred and legible.
