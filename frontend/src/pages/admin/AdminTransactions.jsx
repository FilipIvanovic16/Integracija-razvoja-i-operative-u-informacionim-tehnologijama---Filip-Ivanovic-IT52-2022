import { useEffect, useState } from 'react'
import api, { apiErrorMessage } from '../../api/client'
import Pagination from '../../components/Pagination.jsx'
import { formatPrice, formatDate, PAYMENT_STATUS_LABELS } from '../../utils/format'

export default function AdminTransactions() {
  const [data, setData] = useState({ content: [], totalPages: 0, page: 0 })
  const [status, setStatus] = useState('')
  const [page, setPage] = useState(0)
  const [error, setError] = useState('')

  useEffect(() => {
    const params = { page, size: 15, sort: 'createdAt,desc' }
    if (status) params.status = status
    api
      .get('/admin/payments', { params })
      .then((r) => setData(r.data))
      .catch((e) => setError(apiErrorMessage(e)))
  }, [page, status])

  return (
    <div>
      <div className="row-between">
        <h3 style={{ margin: 0 }}>Transakcije (Stripe)</h3>
        <select
          className="select"
          value={status}
          onChange={(e) => {
            setStatus(e.target.value)
            setPage(0)
          }}
        >
          <option value="">Svi statusi</option>
          {Object.keys(PAYMENT_STATUS_LABELS).map((s) => (
            <option key={s} value={s}>
              {PAYMENT_STATUS_LABELS[s]}
            </option>
          ))}
        </select>
      </div>
      <p className="muted">
        Podaci o uplatama dolaze automatski preko Stripe webhook-a nakon potvrde naplate.
      </p>
      {error && <div className="alert alert-error">{error}</div>}

      {data.content.length === 0 ? (
        <div className="alert alert-info">Nema transakcija za prikaz.</div>
      ) : (
        <>
          <table className="table">
            <thead>
              <tr>
                <th>Porudžbina</th>
                <th>Kupac</th>
                <th>Iznos</th>
                <th>Status</th>
                <th>PaymentIntent</th>
                <th>Plaćeno</th>
              </tr>
            </thead>
            <tbody>
              {data.content.map((t) => (
                <tr key={t.id}>
                  <td>{t.orderNumber}</td>
                  <td>
                    {t.customerName}
                    <br />
                    <span className="muted" style={{ fontSize: '.8rem' }}>
                      {t.customerEmail}
                    </span>
                  </td>
                  <td>{formatPrice(t.amount)}</td>
                  <td>
                    <span className={`pill pill-${t.status}`}>
                      {PAYMENT_STATUS_LABELS[t.status]}
                    </span>
                  </td>
                  <td className="muted" style={{ fontSize: '.78rem' }}>
                    {t.stripePaymentIntentId}
                  </td>
                  <td className="muted">{t.paidAt ? formatDate(t.paidAt) : '-'}</td>
                </tr>
              ))}
            </tbody>
          </table>
          <Pagination page={data.page} totalPages={data.totalPages} onChange={setPage} />
        </>
      )}
    </div>
  )
}
