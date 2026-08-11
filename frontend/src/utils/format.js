export function formatPrice(value) {
  const num = Number(value || 0)
  return new Intl.NumberFormat('sr-RS', {
    style: 'currency',
    currency: 'EUR',
    maximumFractionDigits: 2,
  }).format(num)
}

export function formatDate(value) {
  if (!value) return '-'
  return new Date(value).toLocaleString('sr-RS', {
    dateStyle: 'medium',
    timeStyle: 'short',
  })
}

export const STATUS_LABELS = {
  PENDING: 'Na čekanju',
  PAID: 'Plaćeno',
  SHIPPED: 'Poslato',
  DELIVERED: 'Isporučeno',
  CANCELLED: 'Otkazano',
}

export const PAYMENT_STATUS_LABELS = {
  REQUIRES_PAYMENT: 'Čeka uplatu',
  PROCESSING: 'U obradi',
  SUCCEEDED: 'Uspešno',
  FAILED: 'Neuspešno',
  CANCELLED: 'Otkazano',
}
