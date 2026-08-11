import { useEffect, useState } from 'react'
import api, { apiErrorMessage } from '../../api/client'
import Pagination from '../../components/Pagination.jsx'
import { formatDate } from '../../utils/format'

export default function AdminUsers() {
  const [data, setData] = useState({ content: [], totalPages: 0, page: 0 })
  const [q, setQ] = useState('')
  const [page, setPage] = useState(0)
  const [error, setError] = useState('')

  function load() {
    const params = { page, size: 15, sort: 'createdAt,desc' }
    if (q.trim()) params.q = q.trim()
    api.get('/admin/users', { params })
      .then((r) => setData(r.data))
      .catch((e) => setError(apiErrorMessage(e)))
  }

  useEffect(() => {
    const t = setTimeout(load, 300)
    return () => clearTimeout(t)
  }, [page, q])

  async function changeRole(id, role) {
    try { await api.put(`/admin/users/${id}/role`, { role }); load() }
    catch (e) { setError(apiErrorMessage(e)) }
  }

  async function toggleEnabled(id, value) {
    try { await api.put(`/admin/users/${id}/enabled`, null, { params: { value } }); load() }
    catch (e) { setError(apiErrorMessage(e)) }
  }

  async function remove(id) {
    if (!confirm('Obrisati korisnika?')) return
    try { await api.delete(`/admin/users/${id}`); load() }
    catch (e) { setError(apiErrorMessage(e)) }
  }

  return (
    <div>
      <div className="row-between">
        <h3 style={{ margin: 0 }}>Korisnici</h3>
        <input className="input" placeholder="Pretraga po emailu/prezimenu…" value={q} onChange={(e) => { setQ(e.target.value); setPage(0) }} />
      </div>
      {error && <div className="alert alert-error">{error}</div>}

      <table className="table">
        <thead>
          <tr><th>Ime</th><th>Email</th><th>Uloga</th><th>Aktivan</th><th>Registrovan</th><th></th></tr>
        </thead>
        <tbody>
          {data.content.map((u) => (
            <tr key={u.id}>
              <td>{u.firstName} {u.lastName}</td>
              <td>{u.email}</td>
              <td>
                <select className="select" value={u.role} onChange={(e) => changeRole(u.id, e.target.value)}>
                  <option value="CUSTOMER">CUSTOMER</option>
                  <option value="ADMIN">ADMIN</option>
                </select>
              </td>
              <td>
                <button className="btn btn-ghost btn-sm" onClick={() => toggleEnabled(u.id, !u.enabled)}>
                  {u.enabled ? 'Da' : 'Ne'}
                </button>
              </td>
              <td className="muted">{formatDate(u.createdAt)}</td>
              <td style={{ textAlign: 'right' }}>
                <button className="btn btn-danger btn-sm" onClick={() => remove(u.id)}>Obriši</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      <Pagination page={data.page} totalPages={data.totalPages} onChange={setPage} />
    </div>
  )
}
