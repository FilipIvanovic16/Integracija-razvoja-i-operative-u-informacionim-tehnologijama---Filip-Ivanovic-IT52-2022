import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api, { apiErrorMessage } from '../api/client'
import { formatDate } from '../utils/format'
import WatchCard from '../components/WatchCard.jsx'

const EMPTY_ADDRESS = {
  label: '',
  street: '',
  city: '',
  postalCode: '',
  country: 'Srbija',
  phone: '',
}

const TABS = [
  { key: 'overview', label: 'Pregled' },
  { key: 'addresses', label: 'Adrese' },
  { key: 'wishlist', label: 'Lista želja' },
]

export default function AccountPage() {
  const [tab, setTab] = useState('overview')
  const [profile, setProfile] = useState(null)
  const [addresses, setAddresses] = useState([])
  const [wishlist, setWishlist] = useState([])
  const [form, setForm] = useState(EMPTY_ADDRESS)
  const [error, setError] = useState('')
  const [msg, setMsg] = useState('')

  function loadAll() {
    api
      .get('/account/me')
      .then((r) => setProfile(r.data))
      .catch(() => {})
    api
      .get('/account/addresses')
      .then((r) => setAddresses(r.data))
      .catch(() => {})
    api
      .get('/account/wishlist')
      .then((r) => setWishlist(r.data))
      .catch(() => {})
  }

  useEffect(loadAll, [])

  const set = (field) => (e) => setForm({ ...form, [field]: e.target.value })

  async function addAddress(e) {
    e.preventDefault()
    setError('')
    setMsg('')
    try {
      await api.post('/account/addresses', form)
      setForm(EMPTY_ADDRESS)
      setMsg('Adresa je sačuvana.')
      loadAll()
    } catch (err) {
      setError(apiErrorMessage(err))
    }
  }

  async function removeAddress(id) {
    await api.delete(`/account/addresses/${id}`)
    loadAll()
  }

  async function removeWish(watchId) {
    await api.delete(`/account/wishlist/${watchId}`)
    loadAll()
  }

  return (
    <div className="account-shell">
      {/* ── Sidebar ── */}
      <aside className="account-nav">
        {profile && (
          <div className="account-user-card">
            <div className="account-avatar">
              {profile.firstName?.[0]}
              {profile.lastName?.[0]}
            </div>
            <div>
              <strong className="account-fullname">
                {profile.firstName} {profile.lastName}
              </strong>
              <p className="muted account-email">{profile.email}</p>
            </div>
          </div>
        )}
        <nav>
          {TABS.map((t) => (
            <button
              key={t.key}
              className={`account-nav-item${tab === t.key ? ' active' : ''}`}
              onClick={() => {
                setTab(t.key)
                setError('')
                setMsg('')
              }}
            >
              {t.label}
              {t.key === 'wishlist' && wishlist.length > 0 && (
                <span className="badge" style={{ marginLeft: 8 }}>
                  {wishlist.length}
                </span>
              )}
            </button>
          ))}
        </nav>
      </aside>

      {/* ── Content ── */}
      <div className="account-content">
        {/* ══ OVERVIEW ══ */}
        {tab === 'overview' && profile && (
          <div>
            <h2 className="account-tab-title">Pregled naloga</h2>
            <div className="account-info-card card">
              <div className="account-info-row">
                <span className="acc-label">Ime</span>
                <span>{profile.firstName}</span>
              </div>
              <div className="account-info-row">
                <span className="acc-label">Prezime</span>
                <span>{profile.lastName}</span>
              </div>
              <div className="account-info-row">
                <span className="acc-label">Email</span>
                <span>{profile.email}</span>
              </div>
              <div className="account-info-row">
                <span className="acc-label">Uloga</span>
                <span className={`pill pill-${profile.role === 'ADMIN' ? 'SHIPPED' : 'PAID'}`}>
                  {profile.role}
                </span>
              </div>
              <div className="account-info-row">
                <span className="acc-label">Lozinka</span>
                <span className="muted">••••••••</span>
              </div>
              <div className="account-info-row">
                <span className="acc-label">Član od</span>
                <span>{formatDate(profile.createdAt)}</span>
              </div>
            </div>

            <div className="account-stats">
              <div className="acc-stat card">
                <span className="acc-stat-val">{addresses.length}</span>
                <span className="acc-stat-lbl">Sačuvane adrese</span>
              </div>
              <div className="acc-stat card">
                <span className="acc-stat-val">{wishlist.length}</span>
                <span className="acc-stat-lbl">Lista želja</span>
              </div>
            </div>
          </div>
        )}

        {/* ══ ADDRESSES ══ */}
        {tab === 'addresses' && (
          <div>
            <h2 className="account-tab-title">Adrese za isporuku</h2>
            {error && <div className="alert alert-error">{error}</div>}
            {msg && <div className="alert alert-success">{msg}</div>}

            {addresses.length === 0 ? (
              <p className="muted">Nemate sačuvanih adresa.</p>
            ) : (
              <div className="address-list">
                {addresses.map((a) => (
                  <div className="address-card card" key={a.id}>
                    <div className="address-card-inner">
                      <div>
                        {a.label && <p className="address-label">{a.label}</p>}
                        <p className="address-line">{a.street}</p>
                        <p className="address-line muted">
                          {a.postalCode} {a.city}, {a.country}
                        </p>
                        {a.phone && <p className="address-line muted">{a.phone}</p>}
                      </div>
                      <button className="btn btn-ghost btn-sm" onClick={() => removeAddress(a.id)}>
                        Obriši
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}

            <div className="card address-form-card">
              <h3 className="address-form-title">Dodaj novu adresu</h3>
              <form onSubmit={addAddress}>
                <div className="form-row">
                  <div className="field">
                    <label>Oznaka (npr. Kuća)</label>
                    <input className="input" value={form.label} onChange={set('label')} />
                  </div>
                  <div className="field">
                    <label>Telefon</label>
                    <input className="input" value={form.phone} onChange={set('phone')} />
                  </div>
                </div>
                <div className="field">
                  <label>Ulica i broj *</label>
                  <input className="input" required value={form.street} onChange={set('street')} />
                </div>
                <div className="form-row">
                  <div className="field">
                    <label>Grad *</label>
                    <input className="input" required value={form.city} onChange={set('city')} />
                  </div>
                  <div className="field">
                    <label>Poštanski broj *</label>
                    <input
                      className="input"
                      required
                      value={form.postalCode}
                      onChange={set('postalCode')}
                    />
                  </div>
                </div>
                <div className="field">
                  <label>Država *</label>
                  <input
                    className="input"
                    required
                    value={form.country}
                    onChange={set('country')}
                  />
                </div>
                <button className="btn btn-primary">Sačuvaj adresu</button>
              </form>
            </div>
          </div>
        )}

        {/* ══ WISHLIST ══ */}
        {tab === 'wishlist' && (
          <div>
            <h2 className="account-tab-title">Lista želja</h2>
            {wishlist.length === 0 ? (
              <div className="wishlist-empty">
                <p className="muted">Lista želja je prazna.</p>
                <Link to="/catalog" className="btn btn-primary">
                  Istraži katalog
                </Link>
              </div>
            ) : (
              <div className="wishlist-grid">
                {wishlist.map((item) => (
                  <div key={item.id} className="wishlist-item-wrap">
                    <WatchCard watch={item.watch} />
                    <button
                      className="btn btn-ghost btn-sm btn-block wishlist-remove-btn"
                      onClick={() => removeWish(item.watch.id)}
                    >
                      ♥ Ukloni iz liste
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  )
}
