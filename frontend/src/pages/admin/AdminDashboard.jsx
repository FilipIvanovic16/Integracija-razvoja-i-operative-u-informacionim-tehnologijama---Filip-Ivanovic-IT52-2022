import { useEffect, useState } from 'react'
import api from '../../api/client'
import { formatPrice, formatDate, PAYMENT_STATUS_LABELS } from '../../utils/format'

export default function AdminDashboard() {
  const [stats, setStats] = useState({ watches: 0, orders: 0, users: 0, revenue: 0 })
  const [recent, setRecent] = useState([])

  useEffect(() => {
    Promise.all([
      api.get('/watches', { params: { size: 1, activeOnly: false } }),
      api.get('/admin/orders', { params: { size: 1 } }),
      api.get('/admin/users', { params: { size: 1 } }),
      api.get('/admin/payments', { params: { size: 100, status: 'SUCCEEDED' } }),
    ]).then(([w, o, u, p]) => {
      const revenue = p.data.content.reduce((s, t) => s + Number(t.amount), 0)
      setStats({
        watches: w.data.totalElements,
        orders: o.data.totalElements,
        users: u.data.totalElements,
        revenue,
      })
    }).catch(() => {})

    api.get('/admin/payments', { params: { size: 5 } }).then((r) => setRecent(r.data.content)).catch(() => {})
  }, [])

  return (
    <div>
      <div className="stat-grid">
        <div className="stat"><div className="v">{stats.watches}</div><div className="l">Satova u katalogu</div></div>
        <div className="stat"><div className="v">{stats.orders}</div><div className="l">Porudžbina</div></div>
        <div className="stat"><div className="v">{stats.users}</div><div className="l">Korisnika</div></div>
        <div className="stat"><div className="v">{formatPrice(stats.revenue)}</div><div className="l">Prihod (plaćeno)</div></div>
      </div>

      <h3>Najnovije transakcije</h3>
      {recent.length === 0 ? (
        <p className="muted">Još uvek nema transakcija.</p>
      ) : (
        <table className="table">
          <thead><tr><th>Porudžbina</th><th>Kupac</th><th>Iznos</th><th>Status</th><th>Vreme</th></tr></thead>
          <tbody>
            {recent.map((t) => (
              <tr key={t.id}>
                <td>{t.orderNumber}</td>
                <td>{t.customerEmail}</td>
                <td>{formatPrice(t.amount)}</td>
                <td><span className={`pill pill-${t.status}`}>{PAYMENT_STATUS_LABELS[t.status]}</span></td>
                <td className="muted">{formatDate(t.createdAt)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}
