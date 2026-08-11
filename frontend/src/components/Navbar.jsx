import { useState } from 'react'
import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import { useCart } from '../context/CartContext.jsx'

export default function Navbar() {
  const { user, isAdmin, logout } = useAuth()
  const { count } = useCart()
  const navigate = useNavigate()
  const [menuOpen, setMenuOpen] = useState(false)

  function handleLogout() {
    logout()
    navigate('/')
    setMenuOpen(false)
  }

  const close = () => setMenuOpen(false)

  return (
    <header className="navbar">
      <div className="navbar-inner container">
        <Link to="/" className="brand" onClick={close}>⌚ ChronoShop</Link>

        {/* Desktop nav */}
        <nav className="nav-links">
          <NavLink to="/catalog">Katalog</NavLink>
          <NavLink to="/catalog?condition=NEW">Novi satovi</NavLink>
          <NavLink to="/catalog?preOwned=true">Polovni satovi</NavLink>
          {user && <NavLink to="/orders">Moje porudžbine</NavLink>}
          {user && <NavLink to="/account">Nalog</NavLink>}
          {isAdmin && <NavLink to="/admin">Admin</NavLink>}
        </nav>

        <div className="nav-actions">
          <Link to="/cart" className="cart-link" onClick={close}>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{verticalAlign:'middle',marginRight:5}}>
              <circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/>
              <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/>
            </svg>
            Korpa{count > 0 && <span className="badge">{count}</span>}
          </Link>
          {user ? (
            <>
              <span className="nav-user nav-user-desktop">{user.fullName}</span>
              <button className="btn btn-ghost" onClick={handleLogout}>Odjava</button>
            </>
          ) : (
            <>
              <Link to="/login" className="btn btn-ghost" onClick={close}>Prijava</Link>
              <Link to="/register" className="btn btn-primary" onClick={close}>Registracija</Link>
            </>
          )}

          {/* Hamburger */}
          <button
            className={`hamburger${menuOpen ? ' open' : ''}`}
            onClick={() => setMenuOpen(v => !v)}
            aria-label="Meni"
          >
            <span /><span /><span />
          </button>
        </div>
      </div>

      {/* Mobile menu */}
      {menuOpen && (
        <div className="mobile-menu">
          <NavLink to="/catalog" onClick={close}>Katalog</NavLink>
          <NavLink to="/catalog?condition=NEW" onClick={close}>Novi satovi</NavLink>
          <NavLink to="/catalog?preOwned=true" onClick={close}>Polovni satovi</NavLink>
          {user && <NavLink to="/orders" onClick={close}>Moje porudžbine</NavLink>}
          {user && <NavLink to="/account" onClick={close}>Nalog</NavLink>}
          {isAdmin && <NavLink to="/admin" onClick={close}>Admin</NavLink>}
          <div className="mobile-menu-actions">
            {user ? (
              <button className="btn btn-ghost btn-block" onClick={handleLogout}>Odjava</button>
            ) : (
              <>
                <Link to="/login"    className="btn btn-ghost btn-block" onClick={close}>Prijava</Link>
                <Link to="/register" className="btn btn-primary btn-block" onClick={close}>Registracija</Link>
              </>
            )}
          </div>
        </div>
      )}
    </header>
  )
}
