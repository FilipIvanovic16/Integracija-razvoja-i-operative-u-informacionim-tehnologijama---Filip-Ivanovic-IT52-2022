import { useEffect, useState } from 'react'
import api, { apiErrorMessage } from '../../api/client'
import Pagination from '../../components/Pagination.jsx'
import { formatPrice } from '../../utils/format'

const EMPTY = {
  name: '', referenceNumber: '', brandId: '', categoryId: '', description: '',
  price: '', stockQuantity: '', movement: 'AUTOMATIC', gender: 'MENS',
  caseDiameterMm: '', waterResistanceM: '', imageUrls: [], active: true,
  condition: '', documentation: '', material: '',
}
const MOVEMENTS = ['AUTOMATIC', 'MANUAL', 'QUARTZ', 'SPRING_DRIVE']
const GENDERS   = ['MENS', 'WOMENS', 'UNISEX']
const CONDITIONS = [
  { value: 'NEW',       label: 'Nov' },
  { value: 'VERY_GOOD', label: 'Veoma dobro' },
  { value: 'GOOD',      label: 'Dobro (OK)' },
  { value: 'DAMAGED',   label: 'Oštećen' },
]
const DOCUMENTATIONS = [
  { value: 'PAPERS_AND_BOX', label: 'Papiri i kutija' },
  { value: 'BOX_ONLY',       label: 'Samo kutija' },
  { value: 'PAPERS_ONLY',    label: 'Samo papiri' },
  { value: 'NONE',           label: 'Nema dokumentaciju' },
]
const MATERIALS = [
  { value: 'STAINLESS_STEEL', label: 'Nerđajući čelik' },
  { value: 'WHITE_GOLD',      label: 'Belo zlato' },
  { value: 'YELLOW_GOLD',     label: 'Žuto zlato' },
  { value: 'ROSE_GOLD',       label: 'Roze zlato' },
]

function ImageDropManager({ imageUrls, onChange }) {
  const [dragOver, setDragOver] = useState(false)
  const [uploading, setUploading] = useState(false)
  const [dragIdx, setDragIdx] = useState(null)
  const [dragOverIdx, setDragOverIdx] = useState(null)

  async function uploadFiles(files) {
    const images = Array.from(files).filter(f => f.type.startsWith('image/'))
    if (!images.length) return
    setUploading(true)
    const newUrls = []
    for (const file of images) {
      const fd = new FormData()
      fd.append('file', file)
      try {
        const r = await api.post('/admin/uploads', fd, { headers: { 'Content-Type': undefined } })
        newUrls.push(r.data.url)
      } catch {}
    }
    onChange([...imageUrls, ...newUrls])
    setUploading(false)
  }

  function onDropZone(e) {
    e.preventDefault()
    setDragOver(false)
    uploadFiles(e.dataTransfer.files)
  }

  function onFileInput(e) {
    uploadFiles(e.target.files)
    e.target.value = ''
  }

  function removeUrl(i) {
    onChange(imageUrls.filter((_, idx) => idx !== i))
  }

  function onThumbDragStart(e, i) {
    setDragIdx(i)
    e.dataTransfer.effectAllowed = 'move'
  }

  function onThumbDragOver(e, i) {
    e.preventDefault()
    e.dataTransfer.dropEffect = 'move'
    setDragOverIdx(i)
  }

  function onThumbDrop(e, i) {
    e.preventDefault()
    if (dragIdx === null || dragIdx === i) { setDragIdx(null); setDragOverIdx(null); return }
    const next = [...imageUrls]
    const [moved] = next.splice(dragIdx, 1)
    next.splice(i, 0, moved)
    onChange(next)
    setDragIdx(null)
    setDragOverIdx(null)
  }

  return (
    <div className="imgdrop-wrapper">
      <label className="img-manager-label">Slike</label>

      {imageUrls.length > 0 && (
        <div className="imgdrop-preview-row">
          <div className="imgdrop-main">
            {imageUrls[0]
              ? <img src={imageUrls[0]} alt="Naslovna" />
              : <span style={{ color: 'var(--text-muted)', fontSize: 32 }}>⌚</span>
            }
            <span className="imgdrop-main-badge">Naslovna</span>
          </div>
          <div className="imgdrop-thumbs">
            {imageUrls.map((url, i) => (
              <div
                key={i}
                className={`imgdrop-thumb${dragIdx === i ? ' dragging' : ''}${dragOverIdx === i && dragIdx !== i ? ' drop-target' : ''}`}
                draggable
                onDragStart={e => onThumbDragStart(e, i)}
                onDragOver={e => onThumbDragOver(e, i)}
                onDrop={e => onThumbDrop(e, i)}
                onDragEnd={() => { setDragIdx(null); setDragOverIdx(null) }}
              >
                <img src={url} alt={`slika ${i + 1}`} onError={e => { e.target.style.display = 'none' }} />
                {i === 0 && <span className="imgdrop-order-badge">★</span>}
                <button type="button" className="imgdrop-remove" onClick={() => removeUrl(i)} title="Ukloni">✕</button>
              </div>
            ))}
          </div>
        </div>
      )}

      <div
        className={`imgdrop-zone${dragOver ? ' drag-over' : ''}`}
        onDragOver={e => { e.preventDefault(); setDragOver(true) }}
        onDragLeave={() => setDragOver(false)}
        onDrop={onDropZone}
      >
        {uploading ? (
          <span>Učitavanje...</span>
        ) : (
          <>
            <span>Prevuci slike ovde ili</span>
            <label className="btn btn-ghost btn-sm imgdrop-file-label">
              Izaberi fajlove
              <input type="file" multiple accept="image/*" style={{ display: 'none' }} onChange={onFileInput} />
            </label>
          </>
        )}
      </div>
    </div>
  )
}

function BrandSelect({ brands, value, onChange, onBrandAdded }) {
  const [adding,  setAdding]  = useState(false)
  const [newName, setNewName] = useState('')
  const [saving,  setSaving]  = useState(false)
  const [err,     setErr]     = useState('')

  async function createBrand(e) {
    e.preventDefault()
    const name = newName.trim()
    if (!name) return
    setSaving(true); setErr('')
    try {
      const r = await api.post('/brands', { name })
      onBrandAdded(r.data)
      onChange(String(r.data.id))
      setAdding(false); setNewName('')
    } catch (ex) {
      setErr(apiErrorMessage(ex))
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="field">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <label>Brend *</label>
        <button
          type="button"
          className="inline-add-toggle"
          onClick={() => { setAdding(v => !v); setNewName(''); setErr('') }}
        >
          {adding ? '← Nazad' : '+ Novi brend'}
        </button>
      </div>

      {adding ? (
        <div className="inline-add-row">
          <input
            className="input"
            placeholder="Naziv brenda (npr. Grand Seiko)"
            value={newName}
            onChange={e => setNewName(e.target.value)}
            autoFocus
            onKeyDown={e => e.key === 'Enter' && createBrand(e)}
          />
          <button
            type="button"
            className="btn btn-primary btn-sm"
            onClick={createBrand}
            disabled={saving || !newName.trim()}
          >
            {saving ? '…' : 'Dodaj'}
          </button>
          {err && <span className="field-error" style={{ gridColumn: '1/-1' }}>{err}</span>}
        </div>
      ) : (
        <select className="select" required value={value} onChange={e => onChange(e.target.value)}>
          <option value="">— izaberi brend —</option>
          {brands.map(b => <option key={b.id} value={b.id}>{b.name}</option>)}
        </select>
      )}
    </div>
  )
}

export default function AdminWatches() {
  const [data, setData]           = useState({ content: [], totalPages: 0, page: 0 })
  const [brands, setBrands]       = useState([])
  const [categories, setCategories] = useState([])
  const [page, setPage]           = useState(0)
  const [search, setSearch]       = useState('')
  const [error, setError]         = useState('')
  const [editing, setEditing]     = useState(null)
  const [form, setForm]           = useState(EMPTY)
  const [formError, setFormError] = useState('')

  function load() {
    const params = { page, size: 10, sort: 'createdAt,desc', activeOnly: false }
    if (search.trim()) params.search = search.trim()
    api.get('/watches', { params }).then(r => setData(r.data)).catch(e => setError(apiErrorMessage(e)))
  }

  useEffect(() => {
    api.get('/brands',     { params: { size: 100 } }).then(r => setBrands(r.data.content)).catch(() => {})
    api.get('/categories', { params: { size: 100 } }).then(r => setCategories(r.data.content)).catch(() => {})
  }, [])

  useEffect(() => {
    const t = setTimeout(load, 300)
    return () => clearTimeout(t)
  }, [page, search])

  function openNew() {
    setForm(EMPTY)
    setEditing('new')
    setFormError('')
  }

  function openEdit(w) {
    setForm({
      name: w.name, referenceNumber: w.referenceNumber,
      brandId: w.brand?.id || '', categoryId: w.category?.id || '',
      description: w.description || '', price: w.price, stockQuantity: w.stockQuantity,
      movement: w.movement || 'AUTOMATIC', gender: w.gender || 'MENS',
      caseDiameterMm: w.caseDiameterMm || '', waterResistanceM: w.waterResistanceM || '',
      imageUrls: w.imageUrls?.length > 0 ? w.imageUrls : (w.imageUrl ? [w.imageUrl] : []),
      active: w.active,
      condition: w.condition || '',
      documentation: w.documentation || '',
      material: w.material || '',
    })
    setEditing(w.id)
    setFormError('')
  }

  function set(field) {
    return e => {
      const v = e.target.type === 'checkbox' ? e.target.checked : e.target.value
      setForm(prev => ({ ...prev, [field]: v }))
    }
  }

  async function save(e) {
    e.preventDefault()
    setFormError('')
    const payload = {
      ...form,
      brandId:        Number(form.brandId),
      categoryId:     Number(form.categoryId),
      price:          Number(form.price),
      stockQuantity:  Number(form.stockQuantity),
      caseDiameterMm: form.caseDiameterMm  ? Number(form.caseDiameterMm)  : null,
      waterResistanceM: form.waterResistanceM ? Number(form.waterResistanceM) : null,
    }
    try {
      if (editing === 'new') await api.post('/watches', payload)
      else                   await api.put(`/watches/${editing}`, payload)
      setEditing(null)
      load()
    } catch (err) {
      setFormError(apiErrorMessage(err))
    }
  }

  async function remove(id) {
    if (!confirm('Obrisati sat?')) return
    try { await api.delete(`/watches/${id}`); load() }
    catch (e) { setError(apiErrorMessage(e)) }
  }

  return (
    <div>
      <div className="row-between">
        <h3 style={{ margin: 0 }}>Satovi</h3>
        <div className="toolbar" style={{ margin: 0 }}>
          <input className="input" placeholder="Pretraga…" value={search}
            onChange={e => { setSearch(e.target.value); setPage(0) }} />
          <button className="btn btn-primary" onClick={openNew}>+ Novi sat</button>
        </div>
      </div>
      {error && <div className="alert alert-error">{error}</div>}

      <table className="table">
        <thead>
          <tr><th>Naziv</th><th>Referenca</th><th>Brend</th><th>Cena</th><th>Stanje</th><th>Aktivan</th><th></th></tr>
        </thead>
        <tbody>
          {data.content.map(w => (
            <tr key={w.id}>
              <td style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                {w.imageUrl && (
                  <img src={w.imageUrl} alt={w.name} style={{ width: 36, height: 36, objectFit: 'cover', borderRadius: 6 }}
                    onError={e => { e.target.style.display = 'none' }} />
                )}
                {w.name}
              </td>
              <td className="muted">{w.referenceNumber}</td>
              <td>{w.brand?.name}</td>
              <td>{formatPrice(w.price)}</td>
              <td>{w.stockQuantity}</td>
              <td>{w.active ? '✓' : '—'}</td>
              <td style={{ textAlign: 'right', whiteSpace: 'nowrap' }}>
                <button className="btn btn-ghost btn-sm" onClick={() => openEdit(w)}>Izmeni</button>{' '}
                <button className="btn btn-danger btn-sm" onClick={() => remove(w.id)}>Obriši</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      <Pagination page={data.page} totalPages={data.totalPages} onChange={setPage} />

      {editing !== null && (
        <div className="modal-backdrop" onClick={() => setEditing(null)}>
          <div className="modal modal-wide" onClick={e => e.stopPropagation()}>
            <h3 style={{ marginTop: 0 }}>{editing === 'new' ? 'Novi sat' : 'Izmena sata'}</h3>
            {formError && <div className="alert alert-error">{formError}</div>}
            <form onSubmit={save}>
              <div className="form-row">
                <div className="field"><label>Naziv *</label>
                  <input className="input" required value={form.name} onChange={set('name')} /></div>
                <div className="field"><label>Referenca *</label>
                  <input className="input" required value={form.referenceNumber} onChange={set('referenceNumber')} /></div>
              </div>
              <div className="form-row">
                <BrandSelect
                  brands={brands}
                  value={form.brandId}
                  onChange={id => setForm({ ...form, brandId: id })}
                  onBrandAdded={b => setBrands(prev => [...prev, b].sort((a, z) => a.name.localeCompare(z.name)))}
                />
                <div className="field">
                  <label>Kategorija *</label>
                  <select className="select" required value={form.categoryId} onChange={set('categoryId')}>
                    <option value="">— izaberi —</option>
                    {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                  </select>
                </div>
              </div>
              <div className="form-row">
                <div className="field"><label>Cena (EUR) *</label>
                  <input className="input" type="number" step="0.01" required value={form.price} onChange={set('price')} /></div>
                <div className="field"><label>Količina *</label>
                  <input className="input" type="number" required value={form.stockQuantity} onChange={set('stockQuantity')} /></div>
              </div>
              <div className="form-row">
                <div className="field">
                  <label>Mehanizam</label>
                  <select className="select" value={form.movement} onChange={set('movement')}>
                    {MOVEMENTS.map(m => <option key={m} value={m}>{m}</option>)}
                  </select>
                </div>
                <div className="field">
                  <label>Pol</label>
                  <select className="select" value={form.gender} onChange={set('gender')}>
                    {GENDERS.map(g => <option key={g} value={g}>{g}</option>)}
                  </select>
                </div>
              </div>
              <div className="form-row">
                <div className="field">
                  <label>Nov ili polovan</label>
                  <div className="condition-toggle">
                    <button
                      type="button"
                      className={`cond-toggle-btn${form.condition === 'NEW' ? ' active' : ''}`}
                      onClick={() => setForm(prev => ({ ...prev, condition: 'NEW' }))}
                    >Nov</button>
                    <button
                      type="button"
                      className={`cond-toggle-btn${form.condition !== 'NEW' && form.condition !== '' ? ' active' : ''}`}
                      onClick={() => setForm(prev => ({ ...prev, condition: prev.condition === 'NEW' || prev.condition === '' ? 'VERY_GOOD' : prev.condition }))}
                    >Polovan</button>
                  </div>
                  {form.condition !== 'NEW' && form.condition !== '' && (
                    <select className="select" style={{ marginTop: 8 }} value={form.condition} onChange={set('condition')}>
                      <option value="VERY_GOOD">Veoma dobro</option>
                      <option value="GOOD">Dobro (OK)</option>
                      <option value="DAMAGED">Oštećen</option>
                    </select>
                  )}
                </div>
                <div className="field">
                  <label>Dokumentacija</label>
                  <select className="select" value={form.documentation} onChange={set('documentation')}>
                    <option value="">— izaberi —</option>
                    {DOCUMENTATIONS.map(d => <option key={d.value} value={d.value}>{d.label}</option>)}
                  </select>
                </div>
              </div>
              <div className="form-row">
                <div className="field">
                  <label>Materijal kućišta</label>
                  <select className="select" value={form.material} onChange={set('material')}>
                    <option value="">— izaberi —</option>
                    {MATERIALS.map(m => <option key={m.value} value={m.value}>{m.label}</option>)}
                  </select>
                </div>
                <div className="field"><label>Prečnik (mm)</label>
                  <input className="input" type="number" value={form.caseDiameterMm} onChange={set('caseDiameterMm')} /></div>
              </div>
              <div className="form-row">
                <div className="field"><label>Vodootpornost (m)</label>
                  <input className="input" type="number" value={form.waterResistanceM} onChange={set('waterResistanceM')} /></div>
              </div>
              <div className="field">
                <label>Opis</label>
                <textarea className="input" rows="2" value={form.description} onChange={set('description')} />
              </div>

              <ImageDropManager
                imageUrls={form.imageUrls}
                onChange={urls => setForm({ ...form, imageUrls: urls })}
              />

              <div className="field" style={{ flexDirection: 'row', alignItems: 'center', gap: 8, marginTop: 8 }}>
                <input type="checkbox" checked={form.active} onChange={set('active')} id="active" />
                <label htmlFor="active" style={{ margin: 0 }}>Aktivan (vidljiv u katalogu)</label>
              </div>
              <div className="row-between mt">
                <button type="button" className="btn btn-ghost" onClick={() => setEditing(null)}>Otkaži</button>
                <button className="btn btn-primary">Sačuvaj</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
