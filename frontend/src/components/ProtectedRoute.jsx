import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

export default function ProtectedRoute({ children, adminOnly = false }) {
  const { user, ready, isAdmin } = useAuth()
  const location = useLocation()

  if (!ready) return null

  if (!user) {
    return <Navigate to="/login" state={{ from: location.pathname }} replace />
  }
  if (adminOnly && !isAdmin) {
    return <Navigate to="/" replace />
  }
  return children
}
