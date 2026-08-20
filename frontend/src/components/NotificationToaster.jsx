import { useNotificationStream } from '../hooks/useNotificationStream.js'

const ICONS = {
  ORDER_CREATED: '🛒',
  PAYMENT_COMPLETED: '✅',
  PAYMENT_FAILED: '⚠️',
  INFO: 'ℹ️',
}

export default function NotificationToaster() {
  const { toasts, dismiss } = useNotificationStream()

  if (toasts.length === 0) return null

  return (
    <div className="toast-stack" aria-live="polite">
      {toasts.map((t) => (
        <div key={t.id} className={`toast toast-${t.type?.toLowerCase() || 'info'}`} onClick={() => dismiss(t.id)}>
          <span className="toast-icon">{ICONS[t.type] || ICONS.INFO}</span>
          <div className="toast-body">
            <div className="toast-message">{t.message || 'Nova notifikacija'}</div>
            {t.orderNumber && <div className="toast-meta">{t.orderNumber}</div>}
          </div>
        </div>
      ))}
    </div>
  )
}
