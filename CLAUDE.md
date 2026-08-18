# ChronoShop Microservices — kontekst projekta

> Ovaj fajl je kontekst za Claude Code. Pročitaj ga u celosti pre bilo kakvog rada.
> Detaljan raspored PR-ova je u `PLAN-7-DANA.md`.

## Šta je ovo

Projektni zadatak iz predmeta **"Integracija razvoja i operative u informacionim tehnologijama"**.
Student: **Filip Ivanović, IT52/2022**. Rok za predaju: **25.08.2026.** Odbrana nosi 30 bodova.

Repozitorijum: `https://github.com/FilipIvanovic16/Integracija-razvoja-i-operative-u-informacionim-tehnologijama---Filip-Ivanovic-IT52-2022`

Aplikacija je **ChronoShop** — veb prodavnica luksuznih satova. Postoji kao gotov Spring Boot monolit
(rađen za predmet EONIS) u folderu `EONIS - Filip Ivanović IT52-2022`. Ovde ga **dekomponujemo na
mikroservise** i oko njega gradimo kompletan DevOps lanac.

Narativ za odbranu: *"Imao sam funkcionalan monolit. Dekomponovao sam ga na mikroservise i izgradio
ceo DevOps lanac — od feature grane do deploy-a sa monitoringom."*

## Jezik i komunikacija

Filip govori **srpski, ekavica** — odgovori u čatu ostaju na srpskom, sažeti, bez dugih uvoda
i rekapitulacija.

**Commit poruke i PR opisi (naslov + Šta/Zašto/Kako testirati) pišu se na engleskom** od
`feat/auth-service` nadalje (promenjeno 18.08. — `feat/shared-module` je poslednja grana sa
srpskim commit porukama). Komentari u kodu i projektna dokumentacija (README, ADR, docs/)
ostaju na srpskom, ekavica. Kod (imena klasa, metoda, promenljivih) je uvek na engleskom.

## Ciljna arhitektura

```
React + nginx :5173
        │ REST + SSE
   API Gateway :8080  (Spring Cloud Gateway — routing, JWT filter, CORS, rate limit)
        ├── auth-service         :8081  → authdb
        ├── catalog-service      :8082  → catalogdb   (+ gRPC server)
        ├── order-service        :8083  → orderdb     (+ gRPC klijent)
        ├── payment-service      :8084  → paymentdb
        └── notification-service :8085  (WebFlux, SSE, bez baze)

   RabbitMQ  exchange: chronoshop.events
   PostgreSQL 16  — jedna instanca, pet odvojenih baza (database-per-service)

   Observability: svi servisi → OTLP → Tempo (trace) | Prometheus (metrike) | Loki (logovi) → Grafana :3000
```

### Struktura repozitorijuma

```
/
├── pom.xml                          parent POM (Maven multi-module)
├── shared/                          DTO-ovi, event modeli, izuzeci, .proto fajlovi
├── services/
│   ├── api-gateway/
│   ├── auth-service/
│   ├── catalog-service/
│   ├── order-service/
│   ├── payment-service/
│   └── notification-service/
├── frontend/                        React + Vite (iz EONIS-a, gađa gateway)
├── infra/
│   ├── postgres/init-databases.sh
│   ├── prometheus/prometheus.yml
│   ├── grafana/provisioning/
│   ├── loki/ , promtail/ , tempo/
│   └── deploy.sh                    CD deploy skripta
├── e2e/                             Playwright testovi
├── docs/                            ADR-ovi, dijagrami, projektna dokumentacija
├── .github/workflows/               ci.yml, e2e.yml, cd.yml
├── docker-compose.yml               aplikativni stack
├── docker-compose.observability.yml monitoring stack (diže se odvojeno)
└── .env.example
```

## Mapiranje EONIS koda na servise

Maksimalno koristimo postojeći kod — **ne pišemo od nule**. Izvor je
`EONIS - Filip Ivanović IT52-2022/backend/src/main/java/com/chronoshop/`.

| Servis | Preuzima |
|---|---|
| **auth-service** | `domain/User`, `domain/Address`, `domain/enums/Role`, ceo `security/*` (JwtService, JwtAuthenticationFilter, CustomUserDetailsService, UserPrincipal, SecurityUtils, RestAuthEntryPoint), `service/AuthService`, `service/UserService`, `service/AddressService`, `controller/AuthController`, `controller/AccountController`, `config/SecurityConfig` |
| **catalog-service** | `domain/Watch`, `domain/WatchImage`, `domain/Brand`, `domain/Category`, `domain/enums/{Gender,MovementType,WatchCondition,Documentation}`, `repository/spec/WatchSpecifications`, `service/{WatchService,BrandService,CategoryService}`, `controller/{WatchController,BrandController,CategoryController,UploadController}`, `config/{UploadConfig,WatchImageMigrator}` |
| **order-service** | `domain/Order`, `domain/OrderItem`, `domain/WishlistItem`, `domain/enums/OrderStatus`, `service/OrderService`, `service/WishlistService`, `controller/OrderController` |
| **payment-service** | `domain/Payment`, `domain/enums/PaymentStatus`, `service/PaymentService`, `config/StripeConfig`, `controller/PaymentController`, `controller/AdminPaymentController` |
| **notification-service** | novo — WebFlux + Reactor, konzumer RabbitMQ događaja, SSE endpoint |
| **shared** | ceo `dto/*`, ceo `exception/*` (GlobalExceptionHandler, ApiError, BadRequestException, ResourceNotFoundException, DuplicateResourceException, InsufficientStockException), `mapper/EntityMapper` (razdeljen po servisima), + novi event modeli i `.proto` |
| **frontend** | ceo React app; menja se samo `src/api/client.js` (baseURL → gateway) + dodaje se SSE hook za notifikacije |

`config/DataSeeder` se cepa na po jedan seeder u auth-service (demo nalozi) i catalog-service (satovi, brendovi, kategorije).

Demo nalozi koji moraju da rade i posle dekompozicije:
`admin@chronoshop.rs` / `Admin123!` i `kupac@chronoshop.rs` / `Kupac123!`

## Četiri tipa komunikacije (obavezno po spec-u)

| Tip | Gde |
|---|---|
| **REST** | frontend → gateway → svi servisi; order-service → auth-service (validacija korisnika) |
| **Message Queue** | RabbitMQ topic exchange `chronoshop.events`: order-service publikuje `order.created` → payment-service; payment-service publikuje `payment.completed`/`payment.failed` → order-service (menja status) i notification-service |
| **Reaktivno** | notification-service: Spring WebFlux + Project Reactor (`Flux`, `Sinks.Many`), RxJava adapter sloj nad tokom događaja, SSE endpoint `/api/notifications/stream` |
| **gRPC** | order-service → catalog-service: `CheckStock` i `ReserveStock` pre kreiranja porudžbine, sa REST fallback-om |

## Tehnički stek — fiksirano, ne menjati

| Sloj | Izbor |
|---|---|
| Jezik/framework | Java 17, Spring Boot 3.2, Maven multi-module |
| Gateway | Spring Cloud Gateway |
| Baza | PostgreSQL 16, **baza po servisu** (jedna instanca, 5 baza preko init skripte) |
| Migracije | **Flyway** — nema `ddl-auto=update`, šema je verzionisana |
| MQ | RabbitMQ 3 management |
| Reaktivno | Spring WebFlux + Reactor (+ RxJava 3 adapter) |
| RPC | gRPC + protobuf |
| Testovi | JUnit 5, Mockito, Testcontainers, Vitest, Playwright |
| CI/CD | GitHub Actions |
| Statička analiza | SonarCloud + Spotless/Checkstyle + ESLint/Prettier |
| Registry | ghcr.io (GitHub Container Registry) |
| Monitoring | Prometheus + Grafana + Loki + Tempo |
| Deploy | lokalno kontejnerizovano + `infra/deploy.sh` |

Stripe ostaje u test modu. Pravi ključevi samo u lokalnom `.env`, u testovima mock.

## Git pravila — spec ih eksplicitno ocenjuje

- `main` sadrži **samo stabilan kod**. Branch protection: PR obavezan, CI check-ovi moraju proći.
- Rad **isključivo kroz feature grane**: `feat/*`, `fix/*`, `ci/*`, `test/*`, `docs/*`.
- Svaka značajna izmena ide kroz **pull request**. Squash merge, brisanje grane posle merge-a.
- 2–5 commit-ova po grani (ne jedan džinovski commit).
- PR opis ima tri dela: **Šta**, **Zašto**, **Kako testirati**.
- Commit poruke: Conventional Commits, na engleskom (od `feat/auth-service` nadalje).
  Primer: `feat(auth): extract auth service with JWT issuing`

### Backdate baseline-a

Prva četiri commit-a idu direktno na `main` sa lažiranim datumima (11–14.08), pre nego što se
uključi branch protection. Datumi PR-ova se **ne mogu** lažirati — oni idu realno od 18.08.

```powershell
$env:GIT_AUTHOR_DATE    = "2026-08-11T19:20:00+02:00"
$env:GIT_COMMITTER_DATE = "2026-08-11T19:20:00+02:00"
git commit -m "chore: inicijalni import ChronoShop monolita"
```

## Zahtevi specifikacije — kontrolna lista

Svaka stavka mora biti pokrivena konkretnim fajlom pre predaje.

- [ ] **1.** Min 4 mikroservisa + API Gateway, svaki sa bar jednim endpoint-om → 5 servisa + gateway
- [ ] **1.** Logovi, metrike i trace-ovi iz svakog servisa
- [ ] **1.** REST + MQ + reaktivno + gRPC
- [ ] **2.** Feature grane, PR-ovi, stabilan main, istorija PR-ova
- [ ] **3.** Unit testovi (JUnit) + e2e testovi, svi prolaze
- [ ] **4.** CI pipeline na svaki push i PR: build + testovi + artefakti
- [ ] **5.** Dockerfile po servisu: multi-stage, `.dockerignore`, env varijable, tagovanje; docker build u CI
- [ ] **6.** Docker Compose: mreže, env varijable, **healthcheck-ovi**, **volume-i** za perzistenciju
- [ ] **7.** Statička analiza integrisana u CI (code smells, bugovi, sigurnost)
- [ ] **8.** Deploy: Docker + baza + env konfiguracija + **health endpoint**
- [ ] **9.** CD workflow koji se okida na merge u `main`, sa post-deploy proverom
- [ ] **10.** Monitoring koji prati **latency, throughput i error rate**

## Poznata ograničenja okruženja

- **Docker Desktop** mora biti pokrenut, minimum 8 GB dodeljene memorije.
- Aplikativni i monitoring stack su **odvojeni compose fajlovi** — monitoring se diže samo za
  demonstraciju, da ne guši mašinu.
- JVM u kontejnerima: `-XX:MaxRAMPercentage=60`, `mem_limit` po servisu u compose-u.
- SonarCloud quality gate se podešava na **New Code** — nasleđeni dug iz monolita ne sme da blokira merge.
- e2e job u CI-ju se pokreće samo na PR ka `main` i na `main`, ne na svaki push u feature granu.

## Definicija završenog za svaki PR

1. `./mvnw verify` prolazi lokalno (svi moduli)
2. `docker compose up -d` diže stack, svi healthcheck-ovi zeleni
3. CI check-ovi zeleni na PR-u
4. PR opis popunjen (Šta / Zašto / Kako testirati)
5. Squash merge, grana obrisana
