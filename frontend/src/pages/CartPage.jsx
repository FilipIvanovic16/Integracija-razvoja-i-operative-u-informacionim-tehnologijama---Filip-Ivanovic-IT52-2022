import { Link, useNavigate } from 'react-router-dom'
import { useCart } from '../context/CartContext.jsx'
import { useAuth } from '../context/AuthContext.jsx'
import { formatPrice } from '../utils/format'

export default function CartPage() {
  const { items, updateQuantity, removeItem, total } = useCart()
  const { user } = useAuth()
  const navigate = useNavigate()

  if (items.length === 0) {
    return (
      <div>
        <h1 className="page-title">Korpa</h1>
        <div className="alert alert-info">Vaša korpa je prazna. <Link to="/">Pogledajte katalog →</Link></div>
      </div>
    )
  }

  function proceed() {
    if (!user) navigate('/login', { state: { from: '/checkout' } })
    else navigate('/checkout')
  }

  return (
    <div>
      <h1 className="page-title">Korpa</h1>
      <div className="grid" style={{ gridTemplateColumns: '2fr 1fr', alignItems: 'start' }}>
        <div className="card" style={{ padding: '6px 18px' }}>
          {items.map((i) => (
            <div className="cart-row" key={i.watchId}>
              {i.imageUrl ? <img src={i.imageUrl} alt={i.name} /> : <div className="image-placeholder">⌚</div>}
              <div>
                <div style={{ fontWeight: 600 }}>{i.name}</div>
                <div className="muted" style={{ fontSize: '.82rem' }}>{i.brand}</div>
              </div>
              <div className="qty">
                <input
                  className="input"
                  type="number"
                  min="1"
                  max={i.stockQuantity}
                  value={i.quantity}
                  onChange={(e) => updateQuantity(i.watchId, Number(e.target.value))}
                />
              </div>
              <div>{formatPrice(Number(i.price) * i.quantity)}</div>
              <button className="btn btn-ghost btn-sm" onClick={() => removeItem(i.watchId)}>Ukloni</button>
            </div>
          ))}
        </div>

        <div className="summary">
          <h3 style={{ marginTop: 0 }}>Pregled</h3>
          <div className="summary-row"><span>Broj artikala</span><span>{items.length}</span></div>
          <div className="summary-row summary-total"><span>Ukupno</span><span>{formatPrice(total)}</span></div>
          <button className="btn btn-primary btn-block mt" onClick={proceed}>
            Nastavi na plaćanje
          </button>
        </div>
      </div>
    </div>
  )
}
