# ChronoShop — Frontend

React (Vite) aplikacija za veb prodavnicu satova.

## Preduslovi

- Node.js 18+
- Pokrenut backend na `http://localhost:8080`

## Pokretanje

```bash
npm install
cp .env.example .env   # i unesi svoj Stripe pk_test_ ključ
npm run dev
```

Aplikacija: `http://localhost:5173` (Vite proxy prosleđuje `/api` ka backendu).

## Konfiguracija (.env)

| Promenljiva                   | Opis                                          |
| ----------------------------- | --------------------------------------------- |
| `VITE_STRIPE_PUBLISHABLE_KEY` | Stripe test publishable ključ (`pk_test_...`) |
| `VITE_API_BASE`               | Bazni URL API-ja (podrazumevano `/api`)       |

## Funkcionalnosti

- Katalog sa pretragom, filtrima (brend, kategorija), sortiranjem i **paginacijom na serveru**
- Registracija i prijava (JWT) sa validacijom forme
- Korpa i checkout sa **Stripe** plaćanjem (test kartica `4242 4242 4242 4242`)
- Korisnički nalog: adrese i lista želja
- Admin panel: pregled, CRUD satova, **transakcije (Stripe webhook)**, porudžbine, korisnici

## Demo nalozi

- Admin: `admin@chronoshop.rs` / `Admin123!`
- Kupac: `kupac@chronoshop.rs` / `Kupac123!`
