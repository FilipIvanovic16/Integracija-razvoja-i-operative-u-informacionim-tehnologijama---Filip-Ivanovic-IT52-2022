import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import { apiErrorMessage } from '../api/client'

export default function RegisterPage() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirm: '',
  })
  const [errors, setErrors] = useState({})
  const [serverError, setServerError] = useState('')
  const [loading, setLoading] = useState(false)

  function set(field) {
    return (e) => setForm({ ...form, [field]: e.target.value })
  }

  function validate() {
    const er = {}
    if (!form.firstName.trim()) er.firstName = 'Ime je obavezno.'
    if (!form.lastName.trim()) er.lastName = 'Prezime je obavezno.'
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) er.email = 'Unesite ispravan email.'
    if (form.password.length < 6) er.password = 'Lozinka mora imati najmanje 6 karaktera.'
    if (form.password !== form.confirm) er.confirm = 'Lozinke se ne poklapaju.'
    setErrors(er)
    return Object.keys(er).length === 0
  }

  async function onSubmit(e) {
    e.preventDefault()
    setServerError('')
    if (!validate()) return
    setLoading(true)
    try {
      await register({
        firstName: form.firstName,
        lastName: form.lastName,
        email: form.email,
        password: form.password,
      })
      navigate('/', { replace: true })
    } catch (err) {
      setServerError(apiErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="form">
      <h1 className="page-title center">Registracija</h1>
      {serverError && <div className="alert alert-error">{serverError}</div>}
      <form onSubmit={onSubmit} noValidate>
        <div className="form-row">
          <div className="field">
            <label>Ime</label>
            <input className="input" value={form.firstName} onChange={set('firstName')} />
            {errors.firstName && <span className="field-error">{errors.firstName}</span>}
          </div>
          <div className="field">
            <label>Prezime</label>
            <input className="input" value={form.lastName} onChange={set('lastName')} />
            {errors.lastName && <span className="field-error">{errors.lastName}</span>}
          </div>
        </div>
        <div className="field">
          <label>Email</label>
          <input className="input" type="email" value={form.email} onChange={set('email')} />
          {errors.email && <span className="field-error">{errors.email}</span>}
        </div>
        <div className="field">
          <label>Lozinka</label>
          <input
            className="input"
            type="password"
            value={form.password}
            onChange={set('password')}
          />
          {errors.password && <span className="field-error">{errors.password}</span>}
        </div>
        <div className="field">
          <label>Potvrda lozinke</label>
          <input className="input" type="password" value={form.confirm} onChange={set('confirm')} />
          {errors.confirm && <span className="field-error">{errors.confirm}</span>}
        </div>
        <button className="btn btn-primary btn-block" disabled={loading}>
          {loading ? 'Kreiranje naloga…' : 'Registruj se'}
        </button>
      </form>
      <p className="center muted mt">
        Već imate nalog? <Link to="/login">Prijavite se</Link>
      </p>
    </div>
  )
}
