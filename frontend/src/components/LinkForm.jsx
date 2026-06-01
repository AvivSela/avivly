import { useState, useEffect } from 'react';
import { createLink, updateLink } from '../api';

export default function LinkForm({ onCreated, editTarget, onUpdated, onCancel }) {
  const [originalUrl, setOriginalUrl] = useState('');
  const [customCode, setCustomCode] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (editTarget) {
      setOriginalUrl(editTarget.originalUrl);
      setCustomCode(editTarget.shortCode);
    } else {
      setOriginalUrl('');
      setCustomCode('');
    }
    setError('');
  }, [editTarget]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      if (editTarget) {
        await updateLink(editTarget.id, { originalUrl, shortCode: customCode });
        onUpdated();
      } else {
        await createLink({ originalUrl, shortCode: customCode || undefined });
        setOriginalUrl('');
        setCustomCode('');
        onCreated();
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Something went wrong.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="bg-white rounded-xl shadow p-6">
      <h2 className="text-lg font-semibold mb-4">{editTarget ? 'Edit Link' : 'Create Short Link'}</h2>
      <form onSubmit={handleSubmit} className="flex flex-col gap-3">
        <input
          type="url"
          placeholder="https://example.com/long-url"
          value={originalUrl}
          onChange={(e) => setOriginalUrl(e.target.value)}
          required
          className="border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400"
        />
        <input
          type="text"
          placeholder="Custom code (optional)"
          value={customCode}
          onChange={(e) => setCustomCode(e.target.value)}
          className="border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400"
        />
        {error && <p className="text-red-500 text-sm">{error}</p>}
        <div className="flex gap-2">
          <button
            type="submit"
            disabled={loading}
            className="bg-blue-600 text-white rounded-lg px-4 py-2 text-sm font-medium hover:bg-blue-700 disabled:opacity-50"
          >
            {loading ? 'Saving...' : editTarget ? 'Update' : 'Shorten'}
          </button>
          {editTarget && (
            <button
              type="button"
              onClick={onCancel}
              className="border rounded-lg px-4 py-2 text-sm hover:bg-gray-50"
            >
              Cancel
            </button>
          )}
        </div>
      </form>
    </div>
  );
}
