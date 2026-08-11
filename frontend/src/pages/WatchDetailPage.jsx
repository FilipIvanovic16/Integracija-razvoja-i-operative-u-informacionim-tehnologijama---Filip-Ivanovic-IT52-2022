import { useEffect, useState } from 'react'
import { useParams, Link, useNavigate } from 'react-router-dom'
import api, { apiErrorMessage } from '../api/client'
import { formatPrice } from '../utils/format'
import { useCart } from '../context/CartContext.jsx'
import { useAuth } from '../context/AuthContext.jsx'
import { useWishlist } from '../context/WishlistContext.jsx'

const MOVEMENT_LABELS = {
  AUTOMATIC:    'Automatik',
  MANUAL:       'Ručno navijanje',
  QUARTZ:       'Kvarc',
  SPRING_DRIVE: 'Spring Drive',
}
const GENDER_LABELS = { MENS: 'Muški', WOMENS: 'Ženski', UNISEX: 'Uniseks' }
const CONDITION_LABELS = { NEW: 'Nov', VERY_GOOD: 'Veoma dobro', GOOD: 'Dobro (OK)', DAMAGED: 'Oštećen' }
const MATERIAL_LABELS = {
  STAINLESS_STEEL: 'Nerđajući čelik',
  WHITE_GOLD:      'Belo zlato',
  YELLOW_GOLD:     'Žuto zlato',
  ROSE_GOLD:       'Roze zlato',
}
const DOCUMENTATION_LABELS = {
  PAPERS_AND_BOX: 'Papiri i kutija',
  BOX_ONLY:       'Samo kutija',
  PAPERS_ONLY:    'Samo papiri',
  NONE:           'Nema dokumentaciju',
}

const SELLER_RATINGS = { 1: 4.6, 2: 4.9, 3: 4.7, 4: 4.8, 5: 4.9, 6: 4.5 }

function StarRating({ score }) {
  const full  = Math.floor(score)
  const half  = score - full >= 0.5
  const empty = 5 - full - (half ? 1 : 0)
  return (
    <span className="star-rating">
      {'★'.repeat(full)}
      {half ? '½' : ''}
      {'☆'.repeat(empty)}
      <span className="star-score"> {score.toFixed(1)}</span>
    </span>
  )
}

export default function WatchDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { addItem } = useCart()
  const { user }    = useAuth()
  const { wishlistIds, addToWishlist, removeFromWishlist } = useWishlist()

  const [watch, setWatch] = useState(null)
  const [error, setError] = useState('')
  const [qty,   setQty]   = useState(1)
  const [msg,   setMsg]   = useState('')
  const [activeThumb, setActiveThumb] = useState(0)

  useEffect(() => {
    setActiveThumb(0)
    api.get(`/watches/${id}`)
      .then(r => setWatch(r.data))
      .catch(e => setError(apiErrorMessage(e)))
  }, [id])

  async function handleWishlistToggle() {
    const isWishlisted = watch && wishlistIds.has(watch.id)
    try {
      if (isWishlisted) {
        await removeFromWishlist(watch.id)
        setMsg('Uklonjeno iz liste želja.')
      } else {
        await addToWishlist(watch.id)
        setMsg('Dodato u listu želja.')
      }
    } catch (e) {
      setMsg(apiErrorMessage(e))
    }
  }

  function handleAddToCart() {
    addItem(watch, qty)
    setMsg('Dodato u korpu.')
  }

  if (error) return <div className="container"><div className="alert alert-error mt">{error}</div></div>
  if (!watch) return <div className="loading">Učitavanje…</div>

  const sellerRating = SELLER_RATINGS[watch.brand?.id] ?? 4.8
  const thumbImages  = watch.imageUrls?.length > 0 ? watch.imageUrls : (watch.imageUrl ? [watch.imageUrl] : [])

  return (
    <div className="detail-page">
      <div className="container">

        {/* ── Breadcrumb ── */}
        <nav className="breadcrumb">
          <Link to="/">Početna</Link>
          <span className="bc-sep">›</span>
          <Link to="/">Satovi</Link>
          <span className="bc-sep">›</span>
          <span className="bc-current">{watch.brand?.name} – {watch.name}</span>
        </nav>

        {/* ── Main 2-col layout ── */}
        <div className="detail-layout">

          {/* LEFT – gallery */}
          <div className="detail-gallery">
            <div className="gallery-main card">
              {watch.imageUrl
                ? <img src={thumbImages[activeThumb]} alt={watch.name} />
                : <div className="image-placeholder">⌚</div>
              }
            </div>
            <div className="gallery-thumbs">
              {thumbImages.map((src, i) => (
                <button
                  key={i}
                  className={`gallery-thumb${activeThumb === i ? ' active' : ''}`}
                  onClick={() => setActiveThumb(i)}
                >
                  {src
                    ? <img src={src} alt={`${watch.name} ${i + 1}`} />
                    : <span>⌚</span>
                  }
                </button>
              ))}
            </div>
          </div>

          {/* RIGHT – info */}
          <div className="detail-info">

            {/* Availability + condition */}
            <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', marginBottom: 14 }}>
              <div className={`availability-badge ${watch.inStock ? 'avail-in' : 'avail-out'}`} style={{ marginBottom: 0 }}>
                {watch.inStock ? '● Na stanju' : '● Nije na stanju'}
              </div>
              {watch.condition && (
                <div className={`availability-badge tag-cond-${watch.condition}`} style={{ marginBottom: 0 }}>
                  {watch.condition === 'NEW' ? 'Nov' : `Polovan · ${CONDITION_LABELS[watch.condition]}`}
                </div>
              )}
            </div>

            {/* Title */}
            <p className="detail-brand-name">{watch.brand?.name}</p>
            <h1 className="detail-title">{watch.name}</h1>
            <p className="detail-ref">Referenca: <strong>{watch.referenceNumber}</strong>
              &nbsp;·&nbsp;{watch.category?.name}
            </p>

            {/* Price */}
            <div className="detail-price-row">
              <span className="detail-price">{formatPrice(watch.price)}</span>
            </div>

            {/* Description */}
            {watch.description && (
              <p className="detail-desc">{watch.description}</p>
            )}

            {msg && <div className="alert alert-info mt">{msg}</div>}

            {/* Actions */}
            {watch.inStock ? (
              <div className="detail-actions">
                <div className="qty-control">
                  <button className="qty-btn" onClick={() => setQty(q => Math.max(1, q - 1))}>−</button>
                  <span className="qty-val">{qty}</span>
                  <button className="qty-btn" onClick={() => setQty(q => Math.min(watch.stockQuantity, q + 1))}>+</button>
                </div>
                <button className="btn btn-primary btn-buy" onClick={handleAddToCart}>
                  Dodaj u korpu
                </button>
              </div>
            ) : (
              <div className="alert alert-error mt">Trenutno nema na stanju.</div>
            )}

            {user && watch && (
              <button
                className={`btn btn-block mt${wishlistIds.has(watch.id) ? ' btn-wish-remove' : ' btn-ghost'}`}
                onClick={handleWishlistToggle}
              >
                {wishlistIds.has(watch.id) ? '♥  Ukloni sa liste želja' : '♡  Dodaj u listu želja'}
              </button>
            )}

            {/* Seller card */}
            <div className="seller-card card mt">
              <div className="seller-header">
                <div className="seller-avatar">{watch.brand?.name?.[0]}</div>
                <div>
                  <p className="seller-name">{watch.brand?.name} Official</p>
                  <p className="seller-type muted">Profesionalni prodavac</p>
                </div>
                <div className="seller-rating-wrap">
                  <StarRating score={sellerRating} />
                </div>
              </div>
              <div className="seller-info-row">
                <span className="muted">Dostupno:</span>
                <strong>{watch.stockQuantity} kom</strong>
              </div>
              <div className="seller-info-row">
                <span className="muted">Isporuka:</span>
                <strong>2–5 radnih dana</strong>
              </div>
            </div>

          </div>
        </div>

        {/* ── Detailed specs table ── */}
        <div className="detail-specs-section card mt">
          <h2 className="specs-title">Detaljne informacije</h2>

          <div className="specs-columns">

            <div className="spec-group">
              <h3 className="spec-group-title">Osnovne informacije</h3>
              <table className="spec-table">
                <tbody>
                  <tr><td className="spec-k">Brend</td><td>{watch.brand?.name}</td></tr>
                  <tr><td className="spec-k">Model</td><td>{watch.name}</td></tr>
                  <tr><td className="spec-k">Referenca</td><td>{watch.referenceNumber}</td></tr>
                  <tr><td className="spec-k">Kategorija</td><td>{watch.category?.name}</td></tr>
                  <tr><td className="spec-k">Pol</td><td>{GENDER_LABELS[watch.gender] || '-'}</td></tr>
                </tbody>
              </table>
            </div>

            <div className="spec-group">
              <h3 className="spec-group-title">Mehanizam</h3>
              <table className="spec-table">
                <tbody>
                  <tr><td className="spec-k">Tip mehanizma</td><td>{MOVEMENT_LABELS[watch.movement] || '-'}</td></tr>
                  <tr><td className="spec-k">Pogon</td><td>{watch.movement === 'QUARTZ' ? 'Baterija' : 'Mehanički'}</td></tr>
                </tbody>
              </table>
            </div>

            <div className="spec-group">
              <h3 className="spec-group-title">Kućište</h3>
              <table className="spec-table">
                <tbody>
                  <tr><td className="spec-k">Prečnik</td><td>{watch.caseDiameterMm ? `${watch.caseDiameterMm} mm` : '-'}</td></tr>
                  <tr><td className="spec-k">Vodootpornost</td><td>{watch.waterResistanceM ? `${watch.waterResistanceM} m` : '-'}</td></tr>
                  {watch.material && <tr><td className="spec-k">Materijal</td><td>{MATERIAL_LABELS[watch.material] || watch.material}</td></tr>}
                </tbody>
              </table>
            </div>

            <div className="spec-group">
              <h3 className="spec-group-title">Stanje i dokumentacija</h3>
              <table className="spec-table">
                <tbody>
                  {watch.condition && (
                    <tr>
                      <td className="spec-k">Stanje</td>
                      <td>
                        <span className={`pill tag-cond-${watch.condition}`}>
                          {CONDITION_LABELS[watch.condition]}
                        </span>
                      </td>
                    </tr>
                  )}
                  {watch.documentation && (
                    <tr><td className="spec-k">Dokumentacija</td><td>{DOCUMENTATION_LABELS[watch.documentation]}</td></tr>
                  )}
                  <tr>
                    <td className="spec-k">Status</td>
                    <td>
                      <span className={`pill ${watch.inStock ? 'pill-PAID' : 'pill-CANCELLED'}`}>
                        {watch.inStock ? 'Na stanju' : 'Nije dostupno'}
                      </span>
                    </td>
                  </tr>
                  <tr><td className="spec-k">Količina</td><td>{watch.stockQuantity} kom</td></tr>
                  <tr><td className="spec-k">Cena</td><td><strong>{formatPrice(watch.price)}</strong></td></tr>
                </tbody>
              </table>
            </div>

          </div>
        </div>

        {/* Back link */}
        <div className="mt">
          <button className="btn btn-ghost" onClick={() => navigate(-1)}>← Nazad</button>
        </div>

      </div>
    </div>
  )
}
