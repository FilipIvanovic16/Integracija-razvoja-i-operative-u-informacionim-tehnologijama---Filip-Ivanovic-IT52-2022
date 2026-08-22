import { createContext, useContext, useEffect, useState, useCallback } from 'react'
import api from '../api/client'
import { useAuth } from './AuthContext.jsx'

const WishlistContext = createContext(null)

export function WishlistProvider({ children }) {
  const { user } = useAuth()
  const [wishlistIds, setWishlistIds] = useState(new Set())

  const refresh = useCallback(() => {
    if (!user) {
      setWishlistIds(new Set())
      return
    }
    api
      .get('/account/wishlist')
      .then((r) => setWishlistIds(new Set((r.data || []).map((item) => item.watch?.id))))
      .catch(() => {})
  }, [user])

  useEffect(() => {
    refresh()
  }, [refresh])

  async function addToWishlist(watchId) {
    await api.post('/account/wishlist', { watchId })
    setWishlistIds((prev) => new Set([...prev, watchId]))
  }

  async function removeFromWishlist(watchId) {
    await api.delete(`/account/wishlist/${watchId}`)
    setWishlistIds((prev) => {
      const s = new Set(prev)
      s.delete(watchId)
      return s
    })
  }

  return (
    <WishlistContext.Provider value={{ wishlistIds, addToWishlist, removeFromWishlist, refresh }}>
      {children}
    </WishlistContext.Provider>
  )
}

export function useWishlist() {
  return useContext(WishlistContext)
}
