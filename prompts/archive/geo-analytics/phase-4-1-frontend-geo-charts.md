# Phase 4.1 — Add Top Countries / Top Cities Charts to `AnalyticsPanel.jsx`

## Context

React frontend at `frontend/`. Charting library: `recharts` (already installed).
File: `frontend/src/components/AnalyticsPanel.jsx`

The component fetches analytics data via `getAnalytics(shortCode)` and renders a
`BarChart` for daily clicks. It uses `recharts` components:
`BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer` (already imported).

The backend now returns `topCountries: [{country, clicks}]` and
`topCities: [{city, country, clicks}]` (from Phase 3.4/3.5).
The API response field for daily clicks is `clicksByDay` on the frontend
(map `clicksOverTime` from the backend if needed — check `api.js` for the mapping).

**Prerequisites:** Phase 3.5 complete — backend `GET /api/links/{shortCode}/analytics`
returns `topCountries` and `topCities` arrays.

## Objective

Add two horizontal bar charts below the existing daily-clicks chart, plus an
empty-state placeholder when both geo arrays are empty.

## Design spec

| Chart | Data field | Bar fill | Y-axis label |
|-------|-----------|----------|--------------|
| Top Countries | `data.topCountries` | `#3b82f6` | `country` |
| Top Cities | `data.topCities` | `#10b981` | `"city, country"` (concatenated) |

Empty state: when `topCountries` and `topCities` are both empty (or absent),
show the text: **"Geographic data not yet available for this link."**

## Implementation

Edit `frontend/src/components/AnalyticsPanel.jsx`.

Replace the file content with:

```jsx
import { useState, useEffect } from 'react';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Cell } from 'recharts';
import { getAnalytics } from '../api';

export default function AnalyticsPanel({ shortCode, onClose }) {
  const [data, setData] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    getAnalytics(shortCode)
      .then((res) => setData(res.data))
      .catch(() => setError('Failed to load analytics.'));
  }, [shortCode]);

  const geoEmpty =
    !data?.topCountries?.length && !data?.topCities?.length;

  const cityLabel = (entry) =>
    entry.city && entry.country ? `${entry.city}, ${entry.country}` : (entry.city ?? '');

  return (
    <div className="bg-white rounded-xl shadow p-6">
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-lg font-semibold">
          Analytics — <span className="font-mono text-blue-600">{shortCode}</span>
        </h2>
        <button onClick={onClose} className="text-gray-400 hover:text-gray-600 text-xl leading-none">
          &times;
        </button>
      </div>

      {error && <p className="text-red-500 text-sm">{error}</p>}

      {data && (
        <div className="flex flex-col gap-6">
          <p className="text-sm text-gray-500">
            Total clicks: <span className="font-semibold text-gray-800">{data.totalClicks}</span>
          </p>

          {data.clicksByDay?.length > 0 && (
            <>
              <h3 className="text-sm font-medium text-gray-600">Clicks over time</h3>
              <ResponsiveContainer width="100%" height={200}>
                <BarChart data={data.clicksByDay}>
                  <XAxis dataKey="date" tick={{ fontSize: 11 }} />
                  <YAxis allowDecimals={false} tick={{ fontSize: 11 }} />
                  <Tooltip />
                  <Bar dataKey="clicks" fill="#3b82f6" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </>
          )}

          {data.topCountries?.length > 0 && (
            <>
              <h3 className="text-sm font-medium text-gray-600">Top countries</h3>
              <ResponsiveContainer width="100%" height={Math.max(120, data.topCountries.length * 32)}>
                <BarChart data={data.topCountries} layout="vertical">
                  <XAxis type="number" allowDecimals={false} tick={{ fontSize: 11 }} />
                  <YAxis type="category" dataKey="country" tick={{ fontSize: 11 }} width={90} />
                  <Tooltip />
                  <Bar dataKey="clicks" fill="#3b82f6" radius={[0, 4, 4, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </>
          )}

          {data.topCities?.length > 0 && (
            <>
              <h3 className="text-sm font-medium text-gray-600">Top cities</h3>
              <ResponsiveContainer width="100%" height={Math.max(120, data.topCities.length * 32)}>
                <BarChart
                  data={data.topCities.map((e) => ({ ...e, label: cityLabel(e) }))}
                  layout="vertical"
                >
                  <XAxis type="number" allowDecimals={false} tick={{ fontSize: 11 }} />
                  <YAxis type="category" dataKey="label" tick={{ fontSize: 11 }} width={130} />
                  <Tooltip />
                  <Bar dataKey="clicks" fill="#10b981" radius={[0, 4, 4, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </>
          )}

          {geoEmpty && (
            <p className="text-sm text-gray-400 italic">
              Geographic data not yet available for this link.
            </p>
          )}
        </div>
      )}

      {!data && !error && <p className="text-sm text-gray-400">Loading...</p>}
    </div>
  );
}
```

## Verify

1. `cd frontend && npm run build` — must succeed with no errors.
2. Start the dev server (`npm run dev`) and open a link's analytics panel.
3. **Golden path:** with geo data present, both horizontal bar charts render below
   the daily-clicks chart.
4. **Empty state:** for a link with no geo data, the text
   "Geographic data not yet available for this link." appears in place of the charts.
5. Check that the existing daily-clicks chart and total-clicks count are unaffected.

## Commit

`feat: add Top Countries and Top Cities bar charts to AnalyticsPanel (Phase 4.1)`
