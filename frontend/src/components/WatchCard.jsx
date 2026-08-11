import { useState } from 'react'
import { Link } from 'react-router-dom'
import { formatPrice } from '../utils/format'
import { useCart } from '../context/CartContext.jsx'
import { useAuth } from '../context/AuthContext.jsx'
import { useWishlist } from '../context/WishlistContext.jsx'

export default function WatchCard({ watch }) {
  const { addItem }    = useCart()
  const { user }       = useAuth()
  const { wishlistIds, addToWishlist, removeFromWishlist } = useWishlist()
  const outOfStock     = !watch.inStock
  const wishlisted     = wishlistIds.has(watch.id)
  const CONDITION_LABELS = { NEW: 'Nov', VERY_GOOD: 'Veoma dobro', GOOD: 'Dobro', DAMAGED: 'Oštećen' }
  const [wMsg, setWMsg]   = useState('')
  const [imgIdx, setImgIdx] = useState(0)

  const images = watch.imageUrls?.length > 0
    ? watch.imageUrls
    : (watch.imageUrl ? [watch.imageUrl] : [])

  function prevImg(e) {
    e.preventDefault(); e.stopPropagation()
    setImgIdx(i => (i - 1 + images.length) % images.length)
  }

  function nextImg(e) {
    e.preventDefault(); e.stopPropagation()
    setImgIdx(i => (i + 1) % images.length)
  }

  function goToDot(e, i) {
    e.preventDefault(); e.stopPropagation()
    setImgIdx(i)
  }

  async function handleWishlist(e) {
    e.preventDefault()
    e.stopPropagation()
    try {
      if (wishlisted) {
        await removeFromWishlist(watch.id)
        setWMsg('Uklonjeno')
      } else {
        await addToWishlist(watch.id)
        setWMsg('Dodato ✓')
      }
      setTimeout(() => setWMsg(''), 2000)
    } catch {
      // silent
    }
  }

  return (
    <div className="card watch-card">
      <Link to={`/watches/${watch.id}`} className="watch-image">
        {images.length > 0 ? (
          <img src={images[imgIdx]} alt={watch.name} loading="lazy" onError={e => { e.target.style.display='none' }} />
        ) : (
          <div className="image-placeholder">⌚</div>
        )}

        {outOfStock && <span className="tag tag-out">Nema na stanju</span>}
        {watch.condition && (
          <span className={`tag tag-condition tag-cond-${watch.condition}`}>
            {CONDITION_LABELS[watch.condition]}
          </span>
        )}

        {user && (
          <button
            className={`wishlist-btn${wishlisted ? ' wishlisted' : ''}`}
            onClick={handleWishlist}
            title={wishlisted ? 'Ukloni iz liste želja' : 'Dodaj u listu želja'}
          >
            {wishlisted ? '♥' : '♡'}
          </button>
        )}

        {images.length > 1 && (
          <>
            <button className="card-arrow card-arrow-left" onClick={prevImg} title="Prethodna">‹</button>
            <button className="card-arrow card-arrow-right" onClick={nextImg} title="Sledeća">›</button>
            <div className="card-dots">
              {images.map((_, i) => (
                <button
                  key={i}
                  className={`card-dot${i === imgIdx ? ' active' : ''}`}
                  onClick={e => goToDot(e, i)}
                />
              ))}
            </div>
          </>
        )}

        {wMsg && <span className="wish-toast">{wMsg}</span>}
      </Link>
      <div className="watch-body">
        <span className="watch-brand">{watch.brand?.name}</span>
        <Link to={`/watches/${watch.id}`} className="watch-name">{watch.name}</Link>
        <span className="watch-ref">{watch.referenceNumber}</span>
        <div className="watch-footer">
          <span className="price">{formatPrice(watch.price)}</span>
          <button
            className="btn btn-primary btn-sm"
            disabled={outOfStock}
            onClick={() => addItem(watch, 1)}
          >
            {outOfStock ? 'Nedostupno' : 'U korpu'}
          </button>
        </div>
      </div>
    </div>
  )
}
