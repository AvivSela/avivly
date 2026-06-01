# Agent Prompt: Wire App.jsx — Main Application Shell (TASK-22)

## Project Context
You are building an **analytics-driven URL shortener** frontend.

The following components already exist at `frontend/src/components/`:

**`LinkForm.jsx`** — Props: `{ onCreated, editTarget, onUpdated, onCancel }`
- Creates or edits a link based on whether `editTarget` is set

**`LinksTable.jsx`** — Props: `{ links, onEdit, onDelete, onViewStats }`
- Renders a table of links with Edit / Delete / Stats actions
- `onDelete(id)` is called after user confirms

**`AnalyticsPanel.jsx`** — Props: `{ shortCode, onClose }`
- Modal overlay showing analytics for a given short code

**API client** at `frontend/src/api.js`:
- `getLinks()`, `createLink(data)`, `updateLink(id, data)`, `deleteLink(id)`, `getAnalytics(shortCode)`

## Your Task
Wire everything together in `frontend/src/App.jsx`.

## File to Edit

### `frontend/src/App.jsx`
```jsx
import { useState, useEffect, useCallback } from 'react';
import LinkForm from './components/LinkForm';
import LinksTable from './components/LinksTable';
import AnalyticsPanel from './components/AnalyticsPanel';
import { getLinks, deleteLink } from './api';

export default function App() {
  const [links, setLinks] = useState([]);
  const [editTarget, setEditTarget] = useState(null);
  const [analyticsShortCode, setAnalyticsShortCode] = useState(null);
  const [loadError, setLoadError] = useState('');

  const fetchLinks = useCallback(async () => {
    try {
      const res = await getLinks();
      setLinks(res.data);
      setLoadError('');
    } catch {
      setLoadError('Failed to load links.');
    }
  }, []);

  useEffect(() => {
    fetchLinks();
  }, [fetchLinks]);

  const handleDelete = async (id) => {
    try {
      await deleteLink(id);
      fetchLinks();
    } catch {
      alert('Failed to delete link.');
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-blue-600 text-white px-6 py-4 shadow">
        <h1 className="text-2xl font-bold">URL Shortener</h1>
      </header>

      <main className="max-w-6xl mx-auto px-4 py-6">
        {loadError && <p className="text-red-500 mb-4">{loadError}</p>}

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
          <LinkForm
            onCreated={fetchLinks}
            editTarget={editTarget}
            onUpdated={() => { setEditTarget(null); fetchLinks(); }}
            onCancel={() => setEditTarget(null)}
          />
          <LinksTable
            links={links}
            onEdit={setEditTarget}
            onDelete={handleDelete}
            onViewStats={setAnalyticsShortCode}
          />
        </div>

        {analyticsShortCode && (
          <AnalyticsPanel
            shortCode={analyticsShortCode}
            onClose={() => setAnalyticsShortCode(null)}
          />
        )}
      </main>
    </div>
  );
}
```

## Acceptance Criteria
- `App.jsx` fetches links on mount and after every mutation (create, update, delete)
- `editTarget` drives `LinkForm` into edit mode; cleared after update or cancel
- `analyticsShortCode` drives `AnalyticsPanel` visibility; clicking Stats on a row opens the panel for that row's short code
- Clicking Stats → `AnalyticsPanel` opens; clicking Close → panel closes
- `handleDelete(id)` calls `deleteLink(id)` then re-fetches (the `window.confirm` is inside `LinksTable`, not here)
- Layout uses a responsive two-column grid (form on left, table on right on medium+ screens)
