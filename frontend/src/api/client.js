import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
  headers: { 'Content-Type': 'application/json' },
})

// Dodaje JWT u svaki zahtev ako postoji
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('cs_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Centralizovano izvlačenje poruke greške iz API odgovora
export function apiErrorMessage(error) {
  const data = error?.response?.data
  if (data?.fieldErrors) {
    return Object.values(data.fieldErrors).join(' ')
  }
  return data?.message || 'Došlo je do greške. Pokušajte ponovo.'
}

export default api
