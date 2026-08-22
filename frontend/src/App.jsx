import { Routes, Route, Navigate } from 'react-router-dom'
import { WishlistProvider } from './context/WishlistContext.jsx'
import Navbar from './components/Navbar.jsx'
import NotificationToaster from './components/NotificationToaster.jsx'
import ProtectedRoute from './components/ProtectedRoute.jsx'
import HomePage from './pages/HomePage.jsx'
import CatalogPage from './pages/CatalogPage.jsx'
import WatchDetailPage from './pages/WatchDetailPage.jsx'
import CartPage from './pages/CartPage.jsx'
import CheckoutPage from './pages/CheckoutPage.jsx'
import LoginPage from './pages/LoginPage.jsx'
import RegisterPage from './pages/RegisterPage.jsx'
import OrdersPage from './pages/OrdersPage.jsx'
import AccountPage from './pages/AccountPage.jsx'
import AdminLayout from './pages/admin/AdminLayout.jsx'
import AdminDashboard from './pages/admin/AdminDashboard.jsx'
import AdminWatches from './pages/admin/AdminWatches.jsx'
import AdminTransactions from './pages/admin/AdminTransactions.jsx'
import AdminOrders from './pages/admin/AdminOrders.jsx'
import AdminUsers from './pages/admin/AdminUsers.jsx'

// Wrapper koji dodaje container + padding za standardne stranice
function PageContainer({ children }) {
  return <div className="page-container">{children}</div>
}

export default function App() {
  return (
    <WishlistProvider>
      <NotificationToaster />
      <Navbar />
      <main className="page-main">
        <Routes>
          {/* Puna širina — bez wrapera */}
          <Route path="/" element={<HomePage />} />
          <Route path="/watches/:id" element={<WatchDetailPage />} />

          {/* Standardne stranice sa container wrapperom */}
          <Route
            path="/catalog"
            element={
              <PageContainer>
                <CatalogPage />
              </PageContainer>
            }
          />
          <Route
            path="/cart"
            element={
              <PageContainer>
                <CartPage />
              </PageContainer>
            }
          />
          <Route
            path="/login"
            element={
              <PageContainer>
                <LoginPage />
              </PageContainer>
            }
          />
          <Route
            path="/register"
            element={
              <PageContainer>
                <RegisterPage />
              </PageContainer>
            }
          />

          <Route
            path="/checkout"
            element={
              <ProtectedRoute>
                <PageContainer>
                  <CheckoutPage />
                </PageContainer>
              </ProtectedRoute>
            }
          />
          <Route
            path="/orders"
            element={
              <ProtectedRoute>
                <PageContainer>
                  <OrdersPage />
                </PageContainer>
              </ProtectedRoute>
            }
          />
          <Route
            path="/account"
            element={
              <ProtectedRoute>
                <PageContainer>
                  <AccountPage />
                </PageContainer>
              </ProtectedRoute>
            }
          />

          <Route
            path="/admin"
            element={
              <ProtectedRoute adminOnly>
                <PageContainer>
                  <AdminLayout />
                </PageContainer>
              </ProtectedRoute>
            }
          >
            <Route index element={<AdminDashboard />} />
            <Route path="watches" element={<AdminWatches />} />
            <Route path="transactions" element={<AdminTransactions />} />
            <Route path="orders" element={<AdminOrders />} />
            <Route path="users" element={<AdminUsers />} />
          </Route>

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
      <footer className="footer">
        <div className="footer-inner container">
          <div className="footer-col">
            <span className="footer-brand">⌚ ChronoShop</span>
            <p className="footer-tagline">Vaš destinacija za luksuzne satove.</p>
          </div>
          <div className="footer-col">
            <span className="footer-heading">Katalog</span>
            <a href="/catalog?condition=NEW">Novi satovi</a>
            <a href="/catalog?preOwned=true">Polovni satovi</a>
            <a href="/catalog">Svi satovi</a>
          </div>
          <div className="footer-col">
            <span className="footer-heading">Nalog</span>
            <a href="/login">Prijava</a>
            <a href="/register">Registracija</a>
            <a href="/orders">Moje porudžbine</a>
          </div>
          <div className="footer-col">
            <span className="footer-heading">Informacije</span>
            <span className="footer-muted">Projektni zadatak</span>
            <span className="footer-muted">EONIS — 2024/2025</span>
            <span className="footer-muted">Filip Ivanović IT52/2022</span>
          </div>
        </div>
        <div className="footer-bottom">
          <span>© 2025 ChronoShop. Sva prava zadržana.</span>
        </div>
      </footer>
    </WishlistProvider>
  )
}
