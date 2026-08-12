# ChronoShop - mikroservisna arhitektura

Veb prodavnica luksuznih satova. Projekat za predmet **Integracija razvoja i operative
u informacionim tehnologijama**.

Student: **Filip Ivanovic, IT52/2022**

## Polazno stanje

Aplikacija je razvijena kao monolit (Spring Boot 3 + React + Vite). U okviru ovog predmeta
se dekomponuje na mikroservise i oko nje se gradi kompletan DevOps lanac: feature grane i
pull request-ovi, automatizovano testiranje, CI/CD pipeline, kontejnerizacija, staticka
analiza koda i monitoring.

## Domen

Katalog satova sa brendovima, kategorijama i naprednim filtriranjem; korisnicki nalozi sa
JWT autentifikacijom i ulogama ADMIN/CUSTOMER; korpa i porudzbine sa kontrolom zaliha;
lista zelja; placanje preko Stripe-a; administratorski panel.

## Pokretanje (trenutno stanje - monolit)

```bash
cp .env.example .env
docker compose up -d
```

Aplikacija: http://localhost:5173 - API: http://localhost:8080

Demo nalozi:

| Uloga | Email | Lozinka |
|---|---|---|
| Administrator | admin@chronoshop.rs | Admin123! |
| Kupac | kupac@chronoshop.rs | Kupac123! |
