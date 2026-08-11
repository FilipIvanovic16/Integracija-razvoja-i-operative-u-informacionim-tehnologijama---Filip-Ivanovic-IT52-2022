import { useState } from 'react'
import { useNavigate, useLocation, Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import { apiErrorMessage } from '../api/client'

export default function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function onSubmit(e) {
    e.preventDefault()
    setError('')
    if (!email || !password) {
      setError('Unesite email i lozinku.')
      return
    }
    setLoading(true)
    try {
      await login(email, password)
      navigate(location.state?.from || '/', { replace: true })
    } catch (err) {
      setError(apiErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="form">
      <h1 className="page-title center">Prijava</h1>
      {error && <div className="alert alert-error">{error}</div>}
      <form onSubmit={onSubmit}>
        <div className="field">
          <label>Email</label>
          <input className="input" type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
        </div>
        <div className="field">
          <label>Lozinka</label>
          <input className="input" type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
        </div>
        <button className="btn btn-primary btn-block" disabled={loading}>
          {loading ? 'Prijavljivanje…' : 'Prijavi se'}
        </button>
      </form>
      <p className="center muted mt">
        Nemate nalog? <Link to="/register">Registrujte se</Link>
      </p>
      <div className="alert alert-info mt">
        <strong>Demo nalozi:</strong><br />
        Admin: admin@chronoshop.rs / Admin123!<br />
        Kupac: kupac@chronoshop.rs / Kupac123!
      </div>
    </div>
  )
}
