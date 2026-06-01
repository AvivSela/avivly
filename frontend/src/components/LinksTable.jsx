export default function LinksTable({ links, onEdit, onDelete, onViewStats }) {
  if (links.length === 0) {
    return (
      <div className="bg-white rounded-xl shadow p-6 flex items-center justify-center text-gray-400 text-sm">
        No links yet. Create one!
      </div>
    );
  }

  return (
    <div className="bg-white rounded-xl shadow p-6 overflow-auto">
      <h2 className="text-lg font-semibold mb-4">Your Links</h2>
      <table className="w-full text-sm">
        <thead>
          <tr className="text-left text-gray-500 border-b">
            <th className="pb-2">Short Code</th>
            <th className="pb-2">Original URL</th>
            <th className="pb-2">Clicks</th>
            <th className="pb-2"></th>
          </tr>
        </thead>
        <tbody>
          {links.map((link) => (
            <tr key={link.id} className="border-b last:border-0 hover:bg-gray-50">
              <td className="py-2 font-mono text-blue-600">
                <a href={`/api/r/${link.shortCode}`} target="_blank" rel="noreferrer">
                  {link.shortCode}
                </a>
              </td>
              <td className="py-2 max-w-[180px] truncate text-gray-600" title={link.originalUrl}>
                {link.originalUrl}
              </td>
              <td className="py-2 text-gray-500">{link.clickCount ?? 0}</td>
              <td className="py-2">
                <div className="flex gap-2 justify-end">
                  <button
                    onClick={() => onViewStats(link.shortCode)}
                    className="text-xs text-indigo-600 hover:underline"
                  >
                    Stats
                  </button>
                  <button
                    onClick={() => onEdit(link)}
                    className="text-xs text-yellow-600 hover:underline"
                  >
                    Edit
                  </button>
                  <button
                    onClick={() => onDelete(link.id)}
                    className="text-xs text-red-500 hover:underline"
                  >
                    Delete
                  </button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
