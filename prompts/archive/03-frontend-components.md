# Agent Prompt: Frontend React Components (TASK-19, TASK-20, TASK-21)

## Project Context
You are building an **analytics-driven URL shortener** frontend in React + Vite + Tailwind CSS.
The API client already exists at `frontend/src/api.js` with these exports:
```js
getLinks()                        // GET /api/links
createLink(data)                  // POST /api/links
updateLink(id, data)              // PUT /api/links/{id}
deleteLink(id)                    // DELETE /api/links/{id}
getAnalytics(shortCode)           // GET /api/links/{shortCode}/analytics
```

The `recharts` library is available.

## Your Task
Create three React components: the link creation/edit form, the links table, and the analytics panel.

## Files to Create

### `frontend/src/components/LinkForm.jsx`

Props: `{ onCreated, editTarget, onUpdated, onCancel }`

- When `editTarget` is null/undefined: render a "Create Link" form that calls `createLink`, then calls `onCreated()`
- When `editTarget` is set: render an "Edit Link" form pre-filled with `editTarget` data, calls `updateLink(editTarget.id, data)`, then calls `onUpdated()`
- Fields (all controlled inputs):
  - `originalUrl` — text, required
  - `customAlias` — text, optional
  - `strategy` — `<select>` with options: `RANDOM_BASE62`, `CUSTOM`
  - `expiresAt` — `datetime-local` input, optional
  - `maxClicks` — number input, optional (min=1)
  - `tags` — text input, optional
- Form clears after successful submit
- Shows inline error message if the API request fails
- Show a Cancel button (calls `onCancel`) only when `editTarget` is set

```jsx
import { useState, useEffect } from 'react';
import { createLink, updateLink } from '../api';

export default function LinkForm({ onCreated, editTarget, onUpdated, onCancel }) {
  const emptyForm = { originalUrl: '', customAlias: '', strategy: 'RANDOM_BASE62', expiresAt: '', maxClicks: '', tags: '' };
  const [form, setForm] = useState(emptyForm);
  const [error, setError] = useState('');

  useEffect(() => {
    if (editTarget) {
      setForm({
        originalUrl: editTarget.originalUrl || '',
        customAlias: editTarget.shortCode || '',
        strategy: editTarget.strategy || 'RANDOM_BASE62',
        expiresAt: editTarget.expiresAt ? editTarget.expiresAt.slice(0, 16) : '',
        maxClicks: editTarget.maxClicks ?? '',
        tags: editTarget.tags || '',
      });
    } else {
      setForm(emptyForm);
    }
  }, [editTarget]);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    const payload = {
      originalUrl: form.originalUrl,
      customAlias: form.customAlias || undefined,
      strategy: form.strategy,
      expiresAt: form.expiresAt || undefined,
      maxClicks: form.maxClicks ? parseInt(form.maxClicks) : undefined,
      tags: form.tags || undefined,
    };
    try {
      if (editTarget) {
        await updateLink(editTarget.id, payload);
        onUpdated();
      } else {
        await createLink(payload);
        setForm(emptyForm);
        onCreated();
      }
    } catch (err) {
      setError(err.response?.data?.message || 'An error occurred. Please try again.');
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-3 p-4 bg-white rounded shadow">
      <h2 className="text-lg font-semibold">{editTarget ? 'Edit Link' : 'Create Link'}</h2>
      {error && <p className="text-red-500 text-sm">{error}</p>}
      <div>
        <label className="block text-sm font-medium">Original URL *</label>
        <input name="originalUrl" value={form.originalUrl} onChange={handleChange} required
          className="w-full border rounded px-2 py-1 text-sm" placeholder="https://example.com/long-url" />
      </div>
      <div>
        <label className="block text-sm font-medium">Custom Alias</label>
        <input name="customAlias" value={form.customAlias} onChange={handleChange}
          className="w-full border rounded px-2 py-1 text-sm" placeholder="my-alias" />
      </div>
      <div>
        <label className="block text-sm font-medium">Strategy</label>
        <select name="strategy" value={form.strategy} onChange={handleChange}
          className="w-full border rounded px-2 py-1 text-sm">
          <option value="RANDOM_BASE62">RANDOM_BASE62</option>
          <option value="CUSTOM">CUSTOM</option>
        </select>
      </div>
      <div>
        <label className="block text-sm font-medium">Expires At</label>
        <input name="expiresAt" type="datetime-local" value={form.expiresAt} onChange={handleChange}
          className="w-full border rounded px-2 py-1 text-sm" />
      </div>
      <div>
        <label className="block text-sm font-medium">Max Clicks</label>
        <input name="maxClicks" type="number" min="1" value={form.maxClicks} onChange={handleChange}
          className="w-full border rounded px-2 py-1 text-sm" placeholder="Unlimited" />
      </div>
      <div>
        <label className="block text-sm font-medium">Tags</label>
        <input name="tags" value={form.tags} onChange={handleChange}
          className="w-full border rounded px-2 py-1 text-sm" placeholder="tag1,tag2" />
      </div>
      <div className="flex gap-2">
        <button type="submit" className="bg-blue-600 text-white px-4 py-1 rounded text-sm hover:bg-blue-700">
          {editTarget ? 'Update' : 'Create'}
        </button>
        {editTarget && (
          <button type="button" onClick={onCancel} className="border px-4 py-1 rounded text-sm hover:bg-gray-100">
            Cancel
          </button>
        )}
      </div>
    </form>
  );
}
```

---

### `frontend/src/components/LinksTable.jsx`

Props: `{ links, onEdit, onDelete, onViewStats }`

Columns: Short Link | Original URL | Clicks | Status | Created | Actions

```jsx
import { deleteLink } from '../api';

export default function LinksTable({ links, onEdit, onDelete, onViewStats }) {
  const handleDelete = async (link) => {
    if (!window.confirm(`Delete link "${link.shortCode}"?`)) return;
    onDelete(link.id);
  };

  if (!links || links.length === 0) {
    return <p className="text-gray-500 text-sm p-4">No links yet. Create one!</p>;
  }

  return (
    <div className="overflow-x-auto">
      <table className="w-full text-sm border-collapse">
        <thead>
          <tr className="bg-gray-100 text-left">
            <th className="p-2 border">Short Link</th>
            <th className="p-2 border">Original URL</th>
            <th className="p-2 border">Clicks</th>
            <th className="p-2 border">Status</th>
            <th className="p-2 border">Created</th>
            <th className="p-2 border">Actions</th>
          </tr>
        </thead>
        <tbody>
          {links.map((link) => (
            <tr key={link.id} className="hover:bg-gray-50">
              <td className="p-2 border">
                <a
                  href={`http://localhost:8080/${link.shortCode}`}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-blue-600 hover:underline"
                >
                  {link.shortCode}
                </a>
              </td>
              <td className="p-2 border" title={link.originalUrl}>
                {link.originalUrl.length > 40
                  ? link.originalUrl.slice(0, 40) + '…'
                  : link.originalUrl}
              </td>
              <td className="p-2 border text-center">{link.totalClicks}</td>
              <td className="p-2 border text-center">
                {link.active ? (
                  <span className="bg-green-100 text-green-700 px-2 py-0.5 rounded-full text-xs">Active</span>
                ) : (
                  <span className="bg-red-100 text-red-700 px-2 py-0.5 rounded-full text-xs">Inactive</span>
                )}
              </td>
              <td className="p-2 border">
                {link.createdAt ? link.createdAt.slice(0, 10) : '—'}
              </td>
              <td className="p-2 border">
                <div className="flex gap-1">
                  <button onClick={() => onEdit(link)}
                    className="text-xs border px-2 py-0.5 rounded hover:bg-gray-100">Edit</button>
                  <button onClick={() => handleDelete(link)}
                    className="text-xs border px-2 py-0.5 rounded hover:bg-red-50 text-red-600">Delete</button>
                  <button onClick={() => onViewStats(link.shortCode)}
                    className="text-xs border px-2 py-0.5 rounded hover:bg-blue-50 text-blue-600">Stats</button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
```

---

### `frontend/src/components/AnalyticsPanel.jsx`

Props: `{ shortCode, onClose }`

Fetches analytics on mount, shows a `recharts` LineChart for clicks over time, and two lists for top referrers and top user agents.

```jsx
import { useState, useEffect } from 'react';
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts';
import { getAnalytics } from '../api';

export default function AnalyticsPanel({ shortCode, onClose }) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    setLoading(true);
    setError('');
    getAnalytics(shortCode)
      .then((res) => setData(res.data))
      .catch(() => setError('Failed to load analytics.'))
      .finally(() => setLoading(false));
  }, [shortCode]);

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg shadow-xl p-6 w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-xl font-semibold">Analytics: {shortCode}</h2>
          <button onClick={onClose} className="text-gray-500 hover:text-gray-800 text-lg font-bold">✕</button>
        </div>

        {loading && <p className="text-gray-500">Loading...</p>}
        {error && <p className="text-red-500">{error}</p>}

        {data && (
          <>
            <div className="mb-6 text-center">
              <p className="text-gray-500 text-sm">Total Clicks</p>
              <p className="text-5xl font-bold text-blue-600">{data.totalClicks}</p>
            </div>

            <div className="mb-6">
              <h3 className="font-medium mb-2">Clicks Over Time</h3>
              <ResponsiveContainer width="100%" height={200}>
                <LineChart data={data.clicksOverTime || []}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="date" tick={{ fontSize: 11 }} />
                  <YAxis allowDecimals={false} tick={{ fontSize: 11 }} />
                  <Tooltip />
                  <Line type="monotone" dataKey="count" stroke="#2563eb" dot={false} />
                </LineChart>
              </ResponsiveContainer>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <h3 className="font-medium mb-2">Top Referrers</h3>
                {data.topReferrers?.length > 0 ? (
                  <ul className="text-sm space-y-1">
                    {data.topReferrers.slice(0, 5).map((r) => (
                      <li key={r.referer} className="flex justify-between">
                        <span className="truncate max-w-[160px]" title={r.referer}>{r.referer}</span>
                        <span className="text-gray-500 ml-2">{r.count}</span>
                      </li>
                    ))}
                  </ul>
                ) : (
                  <p className="text-sm text-gray-400">No referrer data</p>
                )}
              </div>
              <div>
                <h3 className="font-medium mb-2">Top User Agents</h3>
                {data.topUserAgents?.length > 0 ? (
                  <ul className="text-sm space-y-1">
                    {data.topUserAgents.slice(0, 5).map((a) => (
                      <li key={a.userAgent} className="flex justify-between">
                        <span className="truncate max-w-[160px]" title={a.userAgent}>{a.userAgent}</span>
                        <span className="text-gray-500 ml-2">{a.count}</span>
                      </li>
                    ))}
                  </ul>
                ) : (
                  <p className="text-sm text-gray-400">No user agent data</p>
                )}
              </div>
            </div>
          </>
        )}

        <div className="mt-6 text-right">
          <button onClick={onClose} className="bg-gray-200 px-4 py-1.5 rounded text-sm hover:bg-gray-300">Close</button>
        </div>
      </div>
    </div>
  );
}
```

## Acceptance Criteria
- All 3 component files exist in `frontend/src/components/`
- `LinkForm`: form resets after successful create; inline error shown on failure; Cancel button visible only in edit mode
- `LinksTable`: empty state message when `links` is empty; `window.confirm` before delete; short link opens in new tab; status badge is green/red based on `isActive`
- `AnalyticsPanel`: uses `recharts` `LineChart`; shows loading state; handles empty `clicksOverTime` gracefully (empty chart, no crash); Close button calls `onClose`
