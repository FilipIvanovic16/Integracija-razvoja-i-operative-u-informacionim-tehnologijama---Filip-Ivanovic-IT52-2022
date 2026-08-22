import { useEffect, useState } from 'react'
import api, { apiErrorMessage } from '../../api/client'
import Pagination from '../../components/Pagination.jsx'
import { formatPrice, formatDate, STATUS_LABELS } from '../../utils/format'

export default function AdminOrders() {
  const [data, setData] = useState({ content: [], totalPages: 0, page: 0 })
  const [status, setStatus] = useState('')
  const [page, setPage] = useState(0)
  const [error, setError] = useState('')

  function load() {
    const params = { page, size: 15, sort: 'createdAt,desc' }
    if (status) params.status = status
    api
      .get('/admin/orders', { params })
      .then((r) => setData(r.data))
      .catch((e) => setError(apiErrorMessage(e)))
  }

  useEffect(load, [page, status])

  async function changeStatus(id, newStatus) {
    try {
      await api.put(`/admin/orders/${id}/status`, { status: newStatus })
      load()
    } catch (e) {
      setError(apiErrorMessage(e))
    }
  }

  return (
    <div>
      <div className="row-between">
        <h3 style={{ margin: 0 }}>Porudžbine</h3>
        <select
          className="select"
          value={status}
          onChange={(e) => {
            setStatus(e.target.value)
            setPage(0)
          }}
        >
          <option value="">Svi statusi</option>
          {Object.keys(STATUS_LABELS).map((s) => (
            <option key={s} value={s}>
              {STATUS_LABELS[s]}
            </option>
          ))}
        </select>
      </div>
      {error && <div className="alert alert-error">{error}</div>}

      <table className="table">
        <thead>
          <tr>
            <th>Broj</th>
            <th>Kupac</th>
            <th>Iznos</th>
            <th>Datum</th>
            <th>Status</th>
            <th>Promeni</th>
          </tr>
        </thead>
        <tbody>
          {data.content.map((o) => (
            <tr key={o.id}>
              <td>{o.orderNumber}</td>
              <td>{o.customerName}</td>
              <td>{formatPrice(o.totalAmount)}</td>
              <td className="muted">{formatDate(o.createdAt)}</td>
              <td>
                <span className={`pill pill-${o.status}`}>{STATUS_LABELS[o.status]}</span>
              </td>
              <td>
                <select
                  className="select"
                  value={o.status}
                  onChange={(e) => changeStatus(o.id, e.target.value)}
                >
                  {Object.keys(STATUS_LABELS).map((s) => (
                    <option key={s} value={s}>
                      {STATUS_LABELS[s]}
                    </option>
                  ))}
                </select>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      <Pagination page={data.page} totalPages={data.totalPages} onChange={setPage} />
    </div>
  )
}
