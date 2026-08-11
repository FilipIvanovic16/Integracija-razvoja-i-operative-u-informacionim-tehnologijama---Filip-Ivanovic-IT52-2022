import { NavLink, Outlet } from 'react-router-dom'

export default function AdminLayout() {
  return (
    <div>
      <h1 className="page-title">Administracija</h1>
      <div className="admin-shell">
        <nav className="admin-nav">
          <NavLink to="/admin" end>Pregled</NavLink>
          <NavLink to="/admin/watches">Satovi</NavLink>
          <NavLink to="/admin/transactions">Transakcije</NavLink>
          <NavLink to="/admin/orders">Porudžbine</NavLink>
          <NavLink to="/admin/users">Korisnici</NavLink>
        </nav>
        <section>
          <Outlet />
        </section>
      </div>
    </div>
  )
}
