import { createContext, useContext, useEffect, useState } from 'react'

const CartContext = createContext(null)

export function CartProvider({ children }) {
  const [items, setItems] = useState(() => {
    const stored = localStorage.getItem('cs_cart')
    return stored ? JSON.parse(stored) : []
  })

  useEffect(() => {
    localStorage.setItem('cs_cart', JSON.stringify(items))
  }, [items])

  function addItem(watch, quantity = 1) {
    setItems((prev) => {
      const existing = prev.find((i) => i.watchId === watch.id)
      const maxStock = watch.stockQuantity ?? 0
      if (existing) {
        const newQty = Math.min(existing.quantity + quantity, maxStock)
        return prev.map((i) => (i.watchId === watch.id ? { ...i, quantity: newQty } : i))
      }
      return [
        ...prev,
        {
          watchId: watch.id,
          name: watch.name,
          brand: watch.brand?.name,
          price: watch.price,
          imageUrl: watch.imageUrl,
          stockQuantity: maxStock,
          quantity: Math.min(quantity, maxStock),
        },
      ]
    })
  }

  function updateQuantity(watchId, quantity) {
    setItems((prev) =>
      prev.map((i) =>
        i.watchId === watchId
          ? { ...i, quantity: Math.max(1, Math.min(quantity, i.stockQuantity)) }
          : i
      )
    )
  }

  function removeItem(watchId) {
    setItems((prev) => prev.filter((i) => i.watchId !== watchId))
  }

  function clear() {
    setItems([])
  }

  const total = items.reduce((sum, i) => sum + Number(i.price) * i.quantity, 0)
  const count = items.reduce((sum, i) => sum + i.quantity, 0)

  return (
    <CartContext.Provider
      value={{ items, addItem, updateQuantity, removeItem, clear, total, count }}
    >
      {children}
    </CartContext.Provider>
  )
}

export function useCart() {
  return useContext(CartContext)
}
