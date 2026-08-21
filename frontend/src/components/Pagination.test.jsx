import { describe, expect, it, vi } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import Pagination from './Pagination'

describe('Pagination', () => {
  it('renders nothing when there is one page or fewer', () => {
    const { container } = render(<Pagination page={0} totalPages={1} onChange={() => {}} />)
    expect(container).toBeEmptyDOMElement()
  })

  it('shows the current page and total', () => {
    render(<Pagination page={2} totalPages={5} onChange={() => {}} />)
    expect(screen.getByText('Strana 3 / 5')).toBeInTheDocument()
  })

  it('disables "previous" on the first page and "next" on the last page', () => {
    render(<Pagination page={0} totalPages={3} onChange={() => {}} />)
    expect(screen.getByText(/Prethodna/)).toBeDisabled()
    expect(screen.getByText(/Sledeća/)).not.toBeDisabled()
  })

  it('calls onChange with the next/previous page index', () => {
    const onChange = vi.fn()
    render(<Pagination page={1} totalPages={3} onChange={onChange} />)

    fireEvent.click(screen.getByText(/Sledeća/))
    expect(onChange).toHaveBeenCalledWith(2)

    fireEvent.click(screen.getByText(/Prethodna/))
    expect(onChange).toHaveBeenCalledWith(0)
  })
})
