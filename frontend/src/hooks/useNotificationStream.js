import { useEffect, useRef, useState, useCallback } from 'react'

const STREAM_URL = (import.meta.env.VITE_API_BASE || '/api') + '/notifications/stream'

/**
 * Prati SSE tok sa notification-service (preko gateway-a) i drži listu toast poruka.
 * EventSource ne šalje Authorization zaglavlje (ograničenje browser API-ja), ali
 * /api/notifications/stream je javan broadcast tok - ne treba JWT.
 */
export function useNotificationStream() {
  const [toasts, setToasts] = useState([])
  const idRef = useRef(0)

  const dismiss = useCallback((id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id))
  }, [])

  useEffect(() => {
    const source = new EventSource(STREAM_URL)

    function push(type, data) {
      let payload
      try {
        payload = JSON.parse(data)
      } catch {
        return
      }
      const id = ++idRef.current
      setToasts((prev) => [...prev, { id, type, ...payload }])
      setTimeout(() => dismiss(id), 6000)
    }

    source.addEventListener('ORDER_CREATED', (e) => push('ORDER_CREATED', e.data))
    source.addEventListener('PAYMENT_COMPLETED', (e) => push('PAYMENT_COMPLETED', e.data))
    source.addEventListener('PAYMENT_FAILED', (e) => push('PAYMENT_FAILED', e.data))
    // Fallback za poruke bez eksplicitnog event imena
    source.onmessage = (e) => push('INFO', e.data)
    source.onerror = () => {
      // EventSource se sam re-konektuje (built-in retry) - ništa dodatno ne radimo ovde
    }

    return () => source.close()
  }, [dismiss])

  return { toasts, dismiss }
}
