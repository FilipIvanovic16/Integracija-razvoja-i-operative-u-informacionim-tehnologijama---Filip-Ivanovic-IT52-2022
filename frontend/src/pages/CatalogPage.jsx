import { useEffect, useState, useCallback } from 'react'
import { useSearchParams } from 'react-router-dom'
import api, { apiErrorMessage } from '../api/client'
import WatchCard from '../components/WatchCard.jsx'
import Pagination from '../components/Pagination.jsx'

const SORT_OPTIONS = [
  { value: 'createdAt,desc', label: 'Najnovije' },
  { value: 'price,asc', label: 'Cena: rastuće' },
  { value: 'price,desc', label: 'Cena: opadajuće' },
  { value: 'name,asc', label: 'Naziv: A–Š' },
]

const MOVEMENTS = [
  { value: 'AUTOMATIC', label: 'Automatski' },
  { value: 'MANUAL', label: 'Manualni' },
  { value: 'QUARTZ', label: 'Quartz' },
  { value: 'SPRING_DRIVE', label: 'Spring Drive' },
]

const GENDERS = [
  { value: 'MENS', label: 'Muški' },
  { value: 'WOMENS', label: 'Ženski' },
  { value: 'UNISEX', label: 'Unisex' },
]

const CONDITIONS = [
  { value: 'NEW', label: 'Nov' },
  { value: 'VERY_GOOD', label: 'Veoma dobro' },
  { value: 'GOOD', label: 'Dobro (OK)' },
  { value: 'DAMAGED', label: 'Oštećen' },
]

const MATERIALS = [
  { value: 'STAINLESS_STEEL', label: 'Nerđajući čelik' },
  { value: 'WHITE_GOLD', label: 'Belo zlato' },
  { value: 'YELLOW_GOLD', label: 'Žuto zlato' },
  { value: 'ROSE_GOLD', label: 'Roze zlato' },
]

const DOCUMENTATIONS = [
  { value: 'PAPERS_AND_BOX', label: 'Papiri i kutija' },
  { value: 'BOX_ONLY', label: 'Samo kutija' },
  { value: 'PAPERS_ONLY', label: 'Samo papiri' },
  { value: 'NONE', label: 'Nema dokumentaciju' },
]

const PRICE_PRESETS = [
  { label: '< 5.000', min: '', max: '5000' },
  { label: '5.000 – 15.000', min: '5000', max: '15000' },
  { label: '15.000 – 50.000', min: '15000', max: '50000' },
  { label: '> 50.000', min: '50000', max: '' },
]

const EMPTY_FILTERS = {
  brandIds: [],
  categoryIds: [],
  genders: [],
  movements: [],
  conditions: [],
  materials: [],
  documentations: [],
  minPrice: '',
  maxPrice: '',
  preOwned: false,
}

const SIDEBAR_ITEMS = [
  { key: 'Brend', filterKeys: ['brandIds'] },
  { key: 'Kategorija', filterKeys: ['categoryIds'] },
  { key: 'Pol', filterKeys: ['genders'] },
  { key: 'Mehanizam', filterKeys: ['movements'] },
  { key: 'Materijal', filterKeys: ['materials'] },
  { key: 'Stanje', filterKeys: ['conditions'] },
  { key: 'Dokumentacija', filterKeys: ['documentations'] },
  { key: 'Cena', filterKeys: ['minPrice', 'maxPrice'] },
]

// ─── Filter modal ──────────────────────────────────────────────────────────────

function FilterModal({ open, onClose, brands, categories, applied, onApply }) {
  const [local, setLocal] = useState(applied)
  const [panel, setPanel] = useState('Brend')
  const [brandSearch, setBrandSearch] = useState('')

  useEffect(() => {
    if (open) {
      setLocal(applied)
      setBrandSearch('')
    }
  }, [open])

  function toggle(key, val) {
    setLocal((prev) => {
      const arr = prev[key]
      return { ...prev, [key]: arr.includes(val) ? arr.filter((v) => v !== val) : [...arr, val] }
    })
  }

  const filteredBrands = brandSearch
    ? brands.filter((b) => b.name.toLowerCase().includes(brandSearch.toLowerCase()))
    : brands

  function isActive(item) {
    return item.filterKeys.some((k) => {
      if (k === 'minPrice' || k === 'maxPrice') return local.minPrice || local.maxPrice
      return Array.isArray(local[k]) ? local[k].length > 0 : local[k]
    })
  }

  if (!open) return null

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="filter-modal" onClick={(e) => e.stopPropagation()}>
        <div className="filter-modal-header">
          <h3>Filteri</h3>
          <button className="filter-close-btn" onClick={onClose}>
            ✕
          </button>
        </div>

        <div className="filter-modal-body">
          {/* Left sidebar */}
          <nav className="filter-sidebar">
            {SIDEBAR_ITEMS.map((item) => (
              <button
                key={item.key}
                className={`filter-sidebar-item${panel === item.key ? ' active' : ''}`}
                onClick={() => setPanel(item.key)}
              >
                {item.key}
                {isActive(item) && <span className="filter-dot" />}
              </button>
            ))}
          </nav>

          {/* Right panel */}
          <div className="filter-panel">
            {panel === 'Brend' && (
              <>
                <input
                  className="input"
                  placeholder="Pretraži brend…"
                  value={brandSearch}
                  onChange={(e) => setBrandSearch(e.target.value)}
                  style={{ marginBottom: 12 }}
                />
                <div className="filter-list">
                  {filteredBrands.map((b) => (
                    <button
                      key={b.id}
                      type="button"
                      className={`filter-option${local.brandIds.includes(String(b.id)) ? ' selected' : ''}`}
                      onClick={() => toggle('brandIds', String(b.id))}
                    >
                      <span className="filter-option-box" />
                      {b.name}
                    </button>
                  ))}
                </div>
              </>
            )}

            {panel === 'Kategorija' && (
              <div className="filter-list">
                {categories.map((c) => (
                  <button
                    key={c.id}
                    type="button"
                    className={`filter-option${local.categoryIds.includes(String(c.id)) ? ' selected' : ''}`}
                    onClick={() => toggle('categoryIds', String(c.id))}
                  >
                    <span className="filter-option-box" />
                    {c.name}
                  </button>
                ))}
              </div>
            )}

            {panel === 'Pol' && (
              <div className="filter-list">
                {GENDERS.map((g) => (
                  <button
                    key={g.value}
                    type="button"
                    className={`filter-option${local.genders.includes(g.value) ? ' selected' : ''}`}
                    onClick={() => toggle('genders', g.value)}
                  >
                    <span className="filter-option-box" />
                    {g.label}
                  </button>
                ))}
              </div>
            )}

            {panel === 'Mehanizam' && (
              <div className="filter-list">
                {MOVEMENTS.map((m) => (
                  <button
                    key={m.value}
                    type="button"
                    className={`filter-option${local.movements.includes(m.value) ? ' selected' : ''}`}
                    onClick={() => toggle('movements', m.value)}
                  >
                    <span className="filter-option-box" />
                    {m.label}
                  </button>
                ))}
              </div>
            )}

            {panel === 'Materijal' && (
              <div className="filter-list">
                {MATERIALS.map((m) => (
                  <button
                    key={m.value}
                    type="button"
                    className={`filter-option${local.materials.includes(m.value) ? ' selected' : ''}`}
                    onClick={() => toggle('materials', m.value)}
                  >
                    <span className="filter-option-box" />
                    {m.label}
                  </button>
                ))}
              </div>
            )}

            {panel === 'Stanje' && (
              <div className="filter-list">
                {CONDITIONS.map((c) => (
                  <button
                    key={c.value}
                    type="button"
                    className={`filter-option${local.conditions.includes(c.value) ? ' selected' : ''}`}
                    onClick={() => toggle('conditions', c.value)}
                  >
                    <span className="filter-option-box" />
                    {c.label}
                  </button>
                ))}
              </div>
            )}

            {panel === 'Dokumentacija' && (
              <div className="filter-list">
                {DOCUMENTATIONS.map((d) => (
                  <button
                    key={d.value}
                    type="button"
                    className={`filter-option${local.documentations.includes(d.value) ? ' selected' : ''}`}
                    onClick={() => toggle('documentations', d.value)}
                  >
                    <span className="filter-option-box" />
                    {d.label}
                  </button>
                ))}
              </div>
            )}

            {panel === 'Cena' && (
              <div>
                <div className="filter-price-row">
                  <div className="filter-price-field">
                    <label>Od (EUR)</label>
                    <input
                      className="input"
                      type="number"
                      min="0"
                      placeholder="0"
                      value={local.minPrice}
                      onChange={(e) => setLocal((p) => ({ ...p, minPrice: e.target.value }))}
                    />
                  </div>
                  <span className="filter-price-sep">—</span>
                  <div className="filter-price-field">
                    <label>Do (EUR)</label>
                    <input
                      className="input"
                      type="number"
                      min="0"
                      placeholder="∞"
                      value={local.maxPrice}
                      onChange={(e) => setLocal((p) => ({ ...p, maxPrice: e.target.value }))}
                    />
                  </div>
                </div>
                <div className="filter-presets">
                  {PRICE_PRESETS.map((pr) => (
                    <button
                      key={pr.label}
                      type="button"
                      className={`filter-preset${local.minPrice === pr.min && local.maxPrice === pr.max ? ' active' : ''}`}
                      onClick={() =>
                        setLocal((p) => ({ ...p, minPrice: pr.min, maxPrice: pr.max }))
                      }
                    >
                      {pr.label}
                    </button>
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>

        <div className="filter-modal-footer">
          <button
            className="btn btn-ghost"
            onClick={() => {
              setLocal(EMPTY_FILTERS)
              onApply(EMPTY_FILTERS)
              onClose()
            }}
          >
            Resetuj sve
          </button>
          <button
            className="btn btn-primary"
            onClick={() => {
              onApply(local)
              onClose()
            }}
          >
            Primeni filtere
          </button>
        </div>
      </div>
    </div>
  )
}

// ─── Applied filter chips ───────────────────────────────────────────────────

function buildChips(filters, brands, categories) {
  const chips = []
  filters.brandIds.forEach((id) => {
    const b = brands.find((b) => String(b.id) === id)
    if (b) chips.push({ key: 'brandIds', val: id, label: b.name })
  })
  filters.categoryIds.forEach((id) => {
    const c = categories.find((c) => String(c.id) === id)
    if (c) chips.push({ key: 'categoryIds', val: id, label: c.name })
  })
  filters.genders.forEach((v) => {
    const g = GENDERS.find((g) => g.value === v)
    if (g) chips.push({ key: 'genders', val: v, label: g.label })
  })
  filters.movements.forEach((v) => {
    const m = MOVEMENTS.find((m) => m.value === v)
    if (m) chips.push({ key: 'movements', val: v, label: m.label })
  })
  filters.conditions.forEach((v) => {
    const c = CONDITIONS.find((c) => c.value === v)
    if (c) chips.push({ key: 'conditions', val: v, label: c.label })
  })
  filters.materials.forEach((v) => {
    const m = MATERIALS.find((m) => m.value === v)
    if (m) chips.push({ key: 'materials', val: v, label: m.label })
  })
  filters.documentations.forEach((v) => {
    const d = DOCUMENTATIONS.find((d) => d.value === v)
    if (d) chips.push({ key: 'documentations', val: v, label: d.label })
  })
  if (filters.preOwned) {
    chips.push({ key: 'preOwned', val: 'preOwned', label: 'Polovni satovi' })
  }
  if (filters.minPrice || filters.maxPrice) {
    const fmt = (v) => Number(v).toLocaleString('de-DE')
    const label =
      filters.minPrice && filters.maxPrice
        ? `€${fmt(filters.minPrice)} – €${fmt(filters.maxPrice)}`
        : filters.minPrice
          ? `Od €${fmt(filters.minPrice)}`
          : `Do €${fmt(filters.maxPrice)}`
    chips.push({ key: 'price', val: 'price', label })
  }
  return chips
}

// ─── Catalog page ───────────────────────────────────────────────────────────

export default function CatalogPage() {
  const [searchParams] = useSearchParams()

  const urlCondition = searchParams.get('condition') || ''
  const urlPreOwned = searchParams.get('preOwned') === 'true'
  const urlBrandId = searchParams.get('brandId') || ''
  const urlGender = searchParams.get('gender') || ''

  const [data, setData] = useState({ content: [], totalPages: 0, page: 0 })
  const [brands, setBrands] = useState([])
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [search, setSearch] = useState('')
  const [sort, setSort] = useState(searchParams.get('sort') || 'createdAt,desc')
  const [page, setPage] = useState(0)
  const [filterOpen, setFilterOpen] = useState(false)
  const [filters, setFilters] = useState(EMPTY_FILTERS)

  // Kad se URL promeni (navbar), resetuj filtere
  useEffect(() => {
    setFilters({
      ...EMPTY_FILTERS,
      brandIds: urlBrandId ? [urlBrandId] : [],
      genders: urlGender ? [urlGender] : [],
      conditions: urlCondition ? [urlCondition] : [],
      preOwned: urlPreOwned,
    })
    setPage(0)
    setSearch('')
  }, [urlCondition, urlPreOwned, urlBrandId, urlGender])

  useEffect(() => {
    api
      .get('/brands', { params: { size: 100 } })
      .then((r) => setBrands(r.data.content))
      .catch(() => {})
    api
      .get('/categories', { params: { size: 100 } })
      .then((r) => setCategories(r.data.content))
      .catch(() => {})
  }, [])

  const load = useCallback(() => {
    setLoading(true)
    setError('')
    // Koristimo URLSearchParams da pravilno šaljemo nizove kao ponavljajuće parametre
    const usp = new URLSearchParams()
    usp.set('page', page)
    usp.set('size', '12')
    usp.set('sort', sort)
    if (search.trim()) usp.set('search', search.trim())
    filters.brandIds.forEach((id) => usp.append('brandId', id))
    filters.categoryIds.forEach((id) => usp.append('categoryId', id))
    filters.genders.forEach((g) => usp.append('gender', g))
    filters.movements.forEach((m) => usp.append('movement', m))
    filters.conditions.forEach((c) => usp.append('condition', c))
    filters.materials.forEach((m) => usp.append('material', m))
    filters.documentations.forEach((d) => usp.append('documentation', d))
    if (filters.minPrice) usp.set('minPrice', filters.minPrice)
    if (filters.maxPrice) usp.set('maxPrice', filters.maxPrice)
    if (filters.preOwned && !filters.conditions.length) usp.set('preOwned', 'true')
    api
      .get(`/watches?${usp.toString()}`)
      .then((r) => setData(r.data))
      .catch((e) => setError(apiErrorMessage(e)))
      .finally(() => setLoading(false))
  }, [page, sort, search, filters])

  useEffect(() => {
    const t = setTimeout(load, 300)
    return () => clearTimeout(t)
  }, [load])

  function applyFilters(f) {
    setFilters(f)
    setPage(0)
  }

  function removeChip(key, val) {
    setFilters((prev) => {
      if (key === 'price') return { ...prev, minPrice: '', maxPrice: '' }
      if (key === 'preOwned') return { ...prev, preOwned: false }
      return { ...prev, [key]: prev[key].filter((v) => v !== val) }
    })
    setPage(0)
  }

  const chips = buildChips(filters, brands, categories)

  // Dinamičan naslov baziran na filterima, ne URL-u
  const pageTitle =
    urlCondition === 'NEW' && filters.conditions.includes('NEW')
      ? 'Novi satovi'
      : urlPreOwned && filters.preOwned
        ? 'Polovni satovi'
        : 'Svi satovi'

  return (
    <div>
      <h1 className="page-title">{pageTitle}</h1>

      <div className="toolbar">
        <input
          className="input"
          placeholder="Pretraži po nazivu, referenci ili brendu…"
          value={search}
          onChange={(e) => {
            setSearch(e.target.value)
            setPage(0)
          }}
        />
        <button
          className={`btn${chips.length ? ' btn-primary' : ' btn-ghost'} filter-btn`}
          onClick={() => setFilterOpen(true)}
        >
          ⊞ Filteri{chips.length > 0 && <span className="filter-count-badge">{chips.length}</span>}
        </button>
        <select
          className="select"
          value={sort}
          onChange={(e) => {
            setSort(e.target.value)
            setPage(0)
          }}
        >
          {SORT_OPTIONS.map((o) => (
            <option key={o.value} value={o.value}>
              {o.label}
            </option>
          ))}
        </select>
      </div>

      {chips.length > 0 && (
        <div className="filter-chips-row">
          {chips.map((chip) => (
            <span key={`${chip.key}-${chip.val}`} className="filter-chip">
              {chip.label}
              <button className="filter-chip-remove" onClick={() => removeChip(chip.key, chip.val)}>
                ✕
              </button>
            </span>
          ))}
          <button
            className="filter-clear-all"
            onClick={() => {
              setFilters(EMPTY_FILTERS)
              setPage(0)
            }}
          >
            Ukloni sve
          </button>
        </div>
      )}

      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <div className="loading">Učitavanje…</div>
      ) : data.content.length === 0 ? (
        <div className="alert alert-info">Nije pronađen nijedan sat po zadatim kriterijumima.</div>
      ) : (
        <>
          <div className="grid watch-grid">
            {data.content.map((w) => (
              <WatchCard key={w.id} watch={w} />
            ))}
          </div>
          <Pagination page={data.page} totalPages={data.totalPages} onChange={setPage} />
        </>
      )}

      <FilterModal
        open={filterOpen}
        onClose={() => setFilterOpen(false)}
        brands={brands}
        categories={categories}
        applied={filters}
        onApply={applyFilters}
      />
    </div>
  )
}
