import { useState, useEffect } from 'react';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts';
import { getAnalytics } from '../api';

export default function AnalyticsPanel({ shortCode, onClose }) {
  const [data, setData] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    getAnalytics(shortCode)
      .then((res) => setData(res.data))
      .catch(() => setError('Failed to load analytics.'));
  }, [shortCode]);

  return (
    <div className="bg-white rounded-xl shadow p-6">
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-lg font-semibold">Analytics — <span className="font-mono text-blue-600">{shortCode}</span></h2>
        <button onClick={onClose} className="text-gray-400 hover:text-gray-600 text-xl leading-none">&times;</button>
      </div>

      {error && <p className="text-red-500 text-sm">{error}</p>}

      {data && (
        <div className="flex flex-col gap-4">
          <p className="text-sm text-gray-500">Total clicks: <span className="font-semibold text-gray-800">{data.totalClicks}</span></p>
          {data.clicksByDay?.length > 0 && (
            <ResponsiveContainer width="100%" height={200}>
              <BarChart data={data.clicksByDay}>
                <XAxis dataKey="date" tick={{ fontSize: 11 }} />
                <YAxis allowDecimals={false} tick={{ fontSize: 11 }} />
                <Tooltip />
                <Bar dataKey="clicks" fill="#3b82f6" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>
      )}

      {!data && !error && <p className="text-sm text-gray-400">Loading...</p>}
    </div>
  );
}
