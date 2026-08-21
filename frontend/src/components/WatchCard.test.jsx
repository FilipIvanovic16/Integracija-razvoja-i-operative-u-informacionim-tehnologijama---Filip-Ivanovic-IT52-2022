import { describe, expect, it } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import WatchCard from './WatchCard'
import { AuthProvider } from '../context/AuthContext.jsx'
import { CartProvider } from '../context/CartContext.jsx'
import { WishlistProvider } from '../context/WishlistContext.jsx'

const WATCH = {
  id: 1,
  name: 'Submariner Date',
  referenceNumber: '126610LN',
  brand: { id: 1, name: 'Rolex' },
  price: 12150,
  inStock: true,
  imageUrls: [],
}

function renderCard(watch = WATCH) {
  return render(
    <MemoryRouter>
      <AuthProvider>
        <CartProvider>
          <WishlistProvider>
            <WatchCard watch={watch} />
          </WishlistProvider>
        </CartProvider>
      </AuthProvider>
    </MemoryRouter>
  )
}

describe('WatchCard', () => {
  it('shows the watch name, brand, reference and price', () => {
    renderCard()
    expect(screen.getByText('Submariner Date')).toBeInTheDocument()
    expect(screen.getByText('Rolex')).toBeInTheDocument()
    expect(screen.getByText('126610LN')).toBeInTheDocument()
    expect(screen.getByText(/12\.150,00/)).toBeInTheDocument()
  })

  it('enables the "add to cart" button when in stock', () => {
    renderCard()
    expect(screen.getByText('U korpu')).not.toBeDisabled()
  })

  it('disables the button and shows "Nedostupno" when out of stock', () => {
    renderCard({ ...WATCH, inStock: false })
    expect(screen.getByText('Nedostupno')).toBeDisabled()
  })

  it('does not show a wishlist button for an anonymous (logged-out) user', () => {
    renderCard()
    expect(screen.queryByTitle('Dodaj u listu želja')).not.toBeInTheDocument()
  })
})
