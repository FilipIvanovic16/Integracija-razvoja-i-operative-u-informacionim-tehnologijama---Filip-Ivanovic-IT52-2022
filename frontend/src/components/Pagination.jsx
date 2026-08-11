export default function Pagination({ page, totalPages, onChange }) {
  if (totalPages <= 1) return null
  return (
    <div className="pagination">
      <button className="btn btn-ghost" disabled={page <= 0} onClick={() => onChange(page - 1)}>
        ← Prethodna
      </button>
      <span className="page-info">
        Strana {page + 1} / {totalPages}
      </span>
      <button
        className="btn btn-ghost"
        disabled={page >= totalPages - 1}
        onClick={() => onChange(page + 1)}
      >
        Sledeća →
      </button>
    </div>
  )
}
