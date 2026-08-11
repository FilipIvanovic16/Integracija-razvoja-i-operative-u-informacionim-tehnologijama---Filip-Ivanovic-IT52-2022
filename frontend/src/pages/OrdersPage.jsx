import { useEffect, useState } from 'react'
import api, { apiErrorMessage } from '../api/client'
import Pagination from '../components/Pagination.jsx'
import { formatPrice, formatDate, STATUS_LABELS } from '../utils/format'

export default function OrdersPage() {
  const [data, setData] = useState({ content: [], totalPages: 0, page: 0 })
  const [page, setPage] = useState(0)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setLoading(true)
    api
      .get('/orders', { params: { page, size: 8, sort: 'createdAt,desc' } })
      .then((r) => setData(r.data))
      .catch((e) => setError(apiErrorMessage(e)))
      .finally(() => setLoading(false))
  }, [page])

  return (
    <div>
      <h1 className="page-title">Moje porudžbine</h1>
      {error && <div className="alert alert-error">{error}</div>}
      {loading ? (
        <div className="loading">Učitavanje…</div>
      ) : data.content.length === 0 ? (
        <div className="alert alert-info">Još uvek nemate porudžbina.</div>
      ) : (
        <>
          {data.content.map((o) => (
            <div className="card mt" key={o.id} style={{ padding: 18 }}>
              <div className="row-between">
                <div>
                  <strong>{o.orderNumber}</strong>
                  <span className={`pill pill-${o.status}`} style={{ marginLeft: 10 }}>
                    {STATUS_LABELS[o.status] || o.status}
                  </span>
                </div>
                <span className="muted">{formatDate(o.createdAt)}</span>
              </div>
              <table className="table mt">
                <tbody>
                  {o.items.map((it, idx) => (
                    <tr key={idx}>
                      <td>{it.watchName}</td>
                      <td className="muted">{it.referenceNumber}</td>
                      <td>{it.quantity} ×</td>
                      <td>{formatPrice(it.unitPrice)}</td>
                      <td style={{ textAlign: 'right' }}>{formatPrice(it.lineTotal)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
              <div className="row-between mt">
                <span className="muted">Isporuka: {o.shippingStreet}, {o.shippingCity}</span>
                <strong>Ukupno: {formatPrice(o.totalAmount)}</strong>
              </div>
            </div>
          ))}
          <Pagination page={data.page} totalPages={data.totalPages} onChange={setPage} />
        </>
      )}
    </div>
  )
}
