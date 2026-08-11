import { createContext, useContext, useEffect, useState } from 'react'
import api from '../api/client'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [ready, setReady] = useState(false)

  useEffect(() => {
    const stored = localStorage.getItem('cs_user')
    if (stored) {
      try {
        setUser(JSON.parse(stored))
      } catch {
        localStorage.removeItem('cs_user')
      }
    }
    setReady(true)
  }, [])

  function persist(auth) {
    localStorage.setItem('cs_token', auth.token)
    const u = {
      userId: auth.userId,
      email: auth.email,
      fullName: auth.fullName,
      role: auth.role,
    }
    localStorage.setItem('cs_user', JSON.stringify(u))
    setUser(u)
  }

  async function login(email, password) {
    const { data } = await api.post('/auth/login', { email, password })
    persist(data)
    return data
  }

  async function register(payload) {
    const { data } = await api.post('/auth/register', payload)
    persist(data)
    return data
  }

  function logout() {
    localStorage.removeItem('cs_token')
    localStorage.removeItem('cs_user')
    setUser(null)
  }

  const isAdmin = user?.role === 'ADMIN'

  return (
    <AuthContext.Provider value={{ user, ready, isAdmin, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}
