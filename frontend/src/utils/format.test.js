import { describe, expect, it } from 'vitest'
import { formatPrice, formatDate, STATUS_LABELS } from './format'

describe('formatPrice', () => {
  it('formats a number as EUR currency', () => {
    expect(formatPrice(12150)).toContain('12.150,00')
    expect(formatPrice(12150)).toContain('€')
  })

  it('treats missing/undefined values as zero', () => {
    expect(formatPrice(undefined)).toContain('0,00')
    expect(formatPrice(null)).toContain('0,00')
  })
})

describe('formatDate', () => {
  it('returns a dash for empty input', () => {
    expect(formatDate(null)).toBe('-')
    expect(formatDate(undefined)).toBe('-')
  })

  it('formats a real date string', () => {
    const result = formatDate('2026-08-21T10:00:00')
    expect(result).not.toBe('-')
    expect(typeof result).toBe('string')
  })
})

describe('STATUS_LABELS', () => {
  it('has a label for every OrderStatus value', () => {
    expect(STATUS_LABELS.PENDING).toBe('Na čekanju')
    expect(STATUS_LABELS.CANCELLED).toBe('Otkazano')
  })
})
