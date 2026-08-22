import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { loadStripe } from '@stripe/stripe-js'
import { Elements, PaymentElement, useStripe, useElements } from '@stripe/react-stripe-js'
import api, { apiErrorMessage } from '../api/client'
import { useCart } from '../context/CartContext.jsx'
import { formatPrice } from '../utils/format'

const PUBLISHABLE_KEY = import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY
const stripePromise =
  PUBLISHABLE_KEY && !PUBLISHABLE_KEY.includes('xxx') ? loadStripe(PUBLISHABLE_KEY) : null

function PaymentForm({ orderNumber, onPaid }) {
  const stripe = useStripe()
  const elements = useElements()
  const [error, setError] = useState('')
  const [processing, setProcessing] = useState(false)

  async function onSubmit(e) {
    e.preventDefault()
    if (!stripe || !elements) return
    setProcessing(true)
    setError('')
    const { error: stripeError, paymentIntent } = await stripe.confirmPayment({
      elements,
      redirect: 'if_required',
    })
    if (stripeError) {
      setError(stripeError.message)
      setProcessing(false)
      return
    }
    if (paymentIntent && paymentIntent.status === 'succeeded') {
      onPaid()
    } else {
      setError('Plaćanje nije potvrđeno. Status: ' + (paymentIntent?.status || 'nepoznat'))
      setProcessing(false)
    }
  }

  return (
    <form onSubmit={onSubmit}>
      <p className="muted">
        Porudžbina <strong>{orderNumber}</strong>
      </p>
      <div className="stripe-box">
        <PaymentElement />
      </div>
      {error && <div className="alert alert-error mt">{error}</div>}
      <button className="btn btn-primary btn-block mt" disabled={!stripe || processing}>
        {processing ? 'Obrada plaćanja…' : 'Plati'}
      </button>
      <p className="muted mt center" style={{ fontSize: '.82rem' }}>
        Test kartica: 4242 4242 4242 4242 · bilo koji budući datum · bilo koji CVC
      </p>
    </form>
  )
}

export default function CheckoutPage() {
  const { items, total, clear } = useCart()
  const navigate = useNavigate()

  const [savedAddresses, setSavedAddresses] = useState([])
  const [selectedAddrId, setSelectedAddrId] = useState('')
  const [shipping, setShipping] = useState({
    shippingStreet: '',
    shippingCity: '',
    shippingPostalCode: '',
    shippingCountry: 'Srbija',
  })
  const [clientSecret, setClientSecret] = useState('')
  const [orderNumber, setOrderNumber] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [done, setDone] = useState(false)

  useEffect(() => {
    api
      .get('/account/addresses')
      .then((r) => {
        const addrs = r.data || []
        setSavedAddresses(addrs)
        if (addrs.length > 0) {
          applyAddress(addrs[0])
          setSelectedAddrId(String(addrs[0].id))
        }
      })
      .catch(() => {})
  }, [])

  function applyAddress(a) {
    setShipping({
      shippingStreet: a.street,
      shippingCity: a.city,
      shippingPostalCode: a.postalCode,
      shippingCountry: a.country,
    })
  }

  function handleAddrSelect(e) {
    const id = e.target.value
    setSelectedAddrId(id)
    if (id === '') {
      setShipping({
        shippingStreet: '',
        shippingCity: '',
        shippingPostalCode: '',
        shippingCountry: 'Srbija',
      })
    } else {
      const found = savedAddresses.find((a) => String(a.id) === id)
      if (found) applyAddress(found)
    }
  }

  const set = (field) => (e) => setShipping({ ...shipping, [field]: e.target.value })

  async function createOrderAndIntent(e) {
    e.preventDefault()
    setError('')
    if (items.length === 0) {
      setError('Korpa je prazna.')
      return
    }
    setLoading(true)
    try {
      const orderRes = await api.post('/orders', {
        items: items.map((i) => ({ watchId: i.watchId, quantity: i.quantity })),
        ...shipping,
      })
      setOrderNumber(orderRes.data.orderNumber)
      const intentRes = await api.post('/payments/create-intent', { orderId: orderRes.data.id })
      setClientSecret(intentRes.data.clientSecret)
    } catch (err) {
      setError(apiErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  function handlePaid() {
    clear()
    setDone(true)
  }

  if (done) {
    return (
      <div className="form center">
        <h1 className="page-title">Hvala na kupovini! ✓</h1>
        <div className="alert alert-success">
          Plaćanje za porudžbinu <strong>{orderNumber}</strong> je uspešno. Potvrda je zabeležena
          preko Stripe webhook-a.
        </div>
        <button className="btn btn-primary" onClick={() => navigate('/orders')}>
          Moje porudžbine
        </button>
      </div>
    )
  }

  return (
    <div>
      <h1 className="page-title">Plaćanje</h1>
      <div
        className="grid"
        style={{ gridTemplateColumns: '1.2fr 1fr', alignItems: 'start', gap: 24 }}
      >
        <div className="card" style={{ padding: 22 }}>
          {!clientSecret ? (
            <form onSubmit={createOrderAndIntent}>
              <h3 style={{ marginTop: 0 }}>Adresa za isporuku</h3>
              {error && <div className="alert alert-error">{error}</div>}

              {/* ── Saved address picker ── */}
              {savedAddresses.length > 0 && (
                <div className="saved-addr-picker">
                  <p className="saved-addr-title">Sačuvane adrese</p>
                  <div className="saved-addr-list">
                    {savedAddresses.map((a) => (
                      <label
                        key={a.id}
                        className={`saved-addr-item${selectedAddrId === String(a.id) ? ' selected' : ''}`}
                      >
                        <input
                          type="radio"
                          name="savedAddr"
                          value={a.id}
                          checked={selectedAddrId === String(a.id)}
                          onChange={handleAddrSelect}
                        />
                        <div className="saved-addr-info">
                          {a.label && <strong>{a.label}</strong>}
                          <span>
                            {a.street}, {a.postalCode} {a.city}
                          </span>
                        </div>
                      </label>
                    ))}
                    <label className={`saved-addr-item${selectedAddrId === '' ? ' selected' : ''}`}>
                      <input
                        type="radio"
                        name="savedAddr"
                        value=""
                        checked={selectedAddrId === ''}
                        onChange={handleAddrSelect}
                      />
                      <div className="saved-addr-info">
                        <strong>Nova adresa</strong>
                        <span>Unesi ručno</span>
                      </div>
                    </label>
                  </div>
                </div>
              )}

              {/* ── Address form ── */}
              <div className={savedAddresses.length > 0 ? 'addr-form-divider' : ''}>
                {savedAddresses.length > 0 && (
                  <p className="muted" style={{ fontSize: '.82rem', marginBottom: 12 }}>
                    Možeš izmeniti detalje ispod:
                  </p>
                )}
                <div className="field">
                  <label>Ulica i broj</label>
                  <input
                    className="input"
                    required
                    value={shipping.shippingStreet}
                    onChange={set('shippingStreet')}
                  />
                </div>
                <div className="form-row">
                  <div className="field">
                    <label>Grad</label>
                    <input
                      className="input"
                      required
                      value={shipping.shippingCity}
                      onChange={set('shippingCity')}
                    />
                  </div>
                  <div className="field">
                    <label>Poštanski broj</label>
                    <input
                      className="input"
                      required
                      value={shipping.shippingPostalCode}
                      onChange={set('shippingPostalCode')}
                    />
                  </div>
                </div>
                <div className="field">
                  <label>Država</label>
                  <input
                    className="input"
                    required
                    value={shipping.shippingCountry}
                    onChange={set('shippingCountry')}
                  />
                </div>
              </div>

              <button className="btn btn-primary btn-block" disabled={loading}>
                {loading ? 'Kreiranje porudžbine…' : 'Potvrdi i nastavi na plaćanje'}
              </button>
            </form>
          ) : stripePromise ? (
            <Elements
              stripe={stripePromise}
              options={{ clientSecret, appearance: { theme: 'night' } }}
            >
              <PaymentForm orderNumber={orderNumber} onPaid={handlePaid} />
            </Elements>
          ) : (
            <div className="alert alert-info">
              Porudžbina <strong>{orderNumber}</strong> je kreirana, ali Stripe publishable ključ
              nije podešen (VITE_STRIPE_PUBLISHABLE_KEY). Unesite svoj <code>pk_test_…</code> ključ
              u <code>.env</code>.
            </div>
          )}
        </div>

        <div className="summary">
          <h3 style={{ marginTop: 0 }}>Vaša porudžbina</h3>
          {items.map((i) => (
            <div className="summary-row" key={i.watchId}>
              <span>
                {i.name} × {i.quantity}
              </span>
              <span>{formatPrice(Number(i.price) * i.quantity)}</span>
            </div>
          ))}
          <div className="summary-row summary-total">
            <span>Ukupno</span>
            <span>{formatPrice(total)}</span>
          </div>
        </div>
      </div>
    </div>
  )
}
