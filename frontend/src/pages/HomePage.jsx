import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../api/client'
import WatchCard from '../components/WatchCard.jsx'

const GENDER_TABS = [
  { label: 'Muški satovi', value: 'MENS', icon: '♂' },
  { label: 'Ženski satovi', value: 'WOMENS', icon: '♀' },
  { label: 'Unisex', value: 'UNISEX', icon: '◎' },
]

const TESTIMONIALS = [
  {
    name: 'Marko Nikolić',
    rating: 5,
    text: 'Pronašao sam savršeni Rolex Submariner po odličnoj ceni. Dostava brza, sve prošlo besprekorno. Topla preporuka!',
    watch: 'Rolex Submariner',
    avatar: 'MN',
  },
  {
    name: 'Ana Jovanović',
    rating: 5,
    text: 'Odlična kolekcija satova i izuzetno profesionalna usluga. Omega Seamaster koji sam kupila izgleda savršeno.',
    watch: 'Omega Seamaster',
    avatar: 'AJ',
  },
  {
    name: 'Stefan Petrović',
    rating: 5,
    text: 'Već treći put kupujem na ChronoShop-u. Cene su fer, opisi su tačni i platforma je izuzetno laka za korišćenje.',
    watch: 'TAG Heuer Carrera',
    avatar: 'SP',
  },
]

export default function HomePage() {
  const navigate = useNavigate()
  const [featured, setFeatured] = useState([])
  const [brands, setBrands] = useState([])

  useEffect(() => {
    api
      .get('/watches', { params: { size: 6, sort: 'price,desc' } })
      .then((r) => setFeatured(r.data.content || []))
      .catch(() => {})
    api
      .get('/brands', { params: { size: 100 } })
      .then((r) => setBrands(r.data.content || []))
      .catch(() => {})
  }, [])

  const goToCatalog = (params = {}) => {
    const qs = new URLSearchParams(params).toString()
    navigate(`/catalog${qs ? `?${qs}` : ''}`)
  }

  return (
    <div className="home-page">
      {/* ══ HERO ══════════════════════════════════════════════ */}
      <section className="hero-section">
        <div className="hero-overlay" />
        <div className="container hero-content">
          <p className="hero-eyebrow">ChronoShop kolekcija</p>
          <h1 className="hero-title">
            Luksuzni satovi
            <br />
            po najboljim cenama
          </h1>
          <p className="hero-desc">
            Otkrijte stotine verifikovanih timepieces od vrhunskih brendova
          </p>
          <button className="btn btn-primary hero-cta" onClick={() => goToCatalog()}>
            Istraži kolekciju →
          </button>
          <div className="hero-trust">
            <div className="hero-trust-item">
              <span className="hero-trust-val">10,000+</span>
              <span className="hero-trust-lbl">Satova u ponudi</span>
            </div>
            <div className="hero-trust-item">
              <span className="hero-trust-val">100%</span>
              <span className="hero-trust-lbl">Verifikovani prodavci</span>
            </div>
            <div className="hero-trust-item">
              <span className="hero-trust-val">24/7</span>
              <span className="hero-trust-lbl">Korisnička podrška</span>
            </div>
          </div>
        </div>
      </section>

      {/* ══ EXPLORE ═══════════════════════════════════════════ */}
      <section className="lp-section explore-section">
        <div className="container">
          <h2 className="section-title">Istraži po kategoriji</h2>
          <div className="gender-tabs">
            {GENDER_TABS.map((g) => (
              <button
                key={g.value}
                className="gender-tab"
                onClick={() => goToCatalog({ gender: g.value })}
              >
                <span className="gender-icon">{g.icon}</span>
                {g.label}
              </button>
            ))}
          </div>
        </div>
      </section>

      {/* ══ POPULAR MODELS ════════════════════════════════════ */}
      <section className="lp-section popular-section">
        <div className="container">
          <div className="section-header">
            <h2 className="section-title">Najpopularniji modeli</h2>
            <button
              className="btn btn-ghost btn-sm"
              onClick={() => goToCatalog({ sort: 'price,desc' })}
            >
              Prikaži sve →
            </button>
          </div>
          <div className="grid featured-grid">
            {featured.map((w) => (
              <WatchCard key={w.id} watch={w} />
            ))}
          </div>
        </div>
      </section>

      {/* ══ BRANDS ════════════════════════════════════════════ */}
      <section className="lp-section brands-section">
        <div className="container">
          <h2 className="section-title">Popularni brendovi</h2>
          <div className="brands-strip">
            {brands.map((b) => (
              <button
                key={b.id}
                className="brand-pill"
                onClick={() => goToCatalog({ brandId: b.id })}
              >
                {b.name}
              </button>
            ))}
          </div>
        </div>
      </section>

      {/* ══ TESTIMONIALS ══════════════════════════════════════ */}
      <section className="lp-section testimonials-section">
        <div className="container">
          <h2 className="section-title center">Šta kažu naši kupci</h2>
          <div className="testimonials-grid">
            {TESTIMONIALS.map((t, i) => (
              <div key={i} className="testimonial-card">
                <div className="stars">
                  {'★'.repeat(t.rating)}
                  {'☆'.repeat(5 - t.rating)}
                </div>
                <p className="testimonial-text">&ldquo;{t.text}&rdquo;</p>
                <div className="testimonial-footer">
                  <div className="testimonial-avatar">{t.avatar}</div>
                  <div>
                    <strong>{t.name}</strong>
                    <p className="muted testimonial-watch">Kupio: {t.watch}</p>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>
    </div>
  )
}
