# Plan realizacije — 7 dana (18–25.08.2026.)

Rok za predaju: **25.08.2026.** Raspored je sabijen na **3 PR-a dnevno**, 18 PR-ova ukupno.
Dan 25.08. je rezerva — ništa se ne planira za taj dan osim predaje.

**Zlatno pravilo:** ako kasniš, žrtvuj obim funkcionalnosti u servisima (manje endpoint-a, tanji
katalog), **nikad faze 3–5**. Docker, CI/CD, statička analiza i monitoring nose bodove; broj
funkcionalnosti u katalogu ne nosi ništa.

---

## Dan 0 — danas, pre PR-ova (~30 min)

Baseline ide **direktno na `main`** sa backdate-ovanim datumima, pre uključivanja branch protection-a.

| Datum commit-a | Commit poruka | Sadržaj |
|---|---|---|
| 11.08. 19:20 | `chore: inicijalni import ChronoShop monolita` | ceo `backend/` i `frontend/` iz EONIS-a, nedirnuto |
| 12.08. 20:05 | `docs: README sa opisom domena i uputstvom za pokretanje` | README.md |
| 13.08. 18:40 | `docs(adr): ADR-0001 prelazak sa monolita na mikroservise` | `docs/adr/0001-monolit-u-mikroservise.md` — granice servisa, izbor tehnologija, razlozi |
| 14.08. 21:15 | `chore: struktura repozitorijuma, .gitignore, .editorconfig` | `services/`, `infra/`, `docs/`, `e2e/` skeleti |

Zatim odmah:
- `git push -u origin main`
- **Settings → Branches → Add rule** na `main`: *Require a pull request before merging*, *Require status checks to pass* (check-ove dodaješ posle PR #12 kad CI proradi)

---

## Dan 1 — 18.08. (utorak) · Temelji i prva dva servisa

| # | Grana | PR | Sadržaj |
|---|---|---|---|
| 1 | `feat/shared-module` | Zajednički modul i Maven multi-module struktura | parent `pom.xml` sa dependency management-om, `shared/` modul: DTO-ovi, `ApiError`, `PageResponse`, `GlobalExceptionHandler`, svi custom izuzeci, event klase (`OrderCreatedEvent`, `PaymentCompletedEvent`, `PaymentFailedEvent`) |
| 2 | `feat/auth-service` | Auth servis — korisnici, adrese i JWT | User/Address/Role, `security/*` iz EONIS-a, Flyway `V1__init.sql`, seeder demo naloga, `/actuator/health`, unit testovi za `JwtService` (izdavanje, validacija, istek) |
| 3 | `feat/catalog-service` | Catalog servis — satovi, brendovi i kategorije | Watch/WatchImage/Brand/Category + enumi, `WatchSpecifications` pretraga, paginacija i sortiranje, upload slika, Flyway migracije, seeder, unit testovi za filtriranje |

**Kraj dana:** dva servisa se dižu lokalno na 8081 i 8082, `/actuator/health` vraća `UP`.

---

## Dan 2 — 19.08. (sreda) · Ostatak servisa i gateway

| # | Grana | PR | Sadržaj |
|---|---|---|---|
| 4 | `feat/order-service` | Order servis — porudžbine i lista želja | Order/OrderItem/WishlistItem, logika umanjenja zaliha, REST klijent ka auth-u, Flyway, **unit testovi za `InsufficientStockException`** (ključna poslovna logika — obavezno pokriti) |
| 5 | `feat/payment-service` | Payment servis — Stripe integracija | Payment entitet, Stripe checkout session, webhook handler, Flyway, unit testovi sa mock-ovanim Stripe klijentom |
| 6 | `feat/api-gateway` | API Gateway sa JWT filterom | Spring Cloud Gateway, rute ka sva 4 servisa, globalni JWT validacioni filter, CORS konfiguracija, `RequestRateLimiter`, propagacija `X-Correlation-Id` |

**Kraj dana:** sve prolazi kroz gateway na 8080; `curl localhost:8080/api/watches` vraća satove.

---

## Dan 3 — 20.08. (četvrtak) · Komunikacija između servisa

| # | Grana | PR | Sadržaj |
|---|---|---|---|
| 7 | `feat/rabbitmq-events` | Asinhrona komunikacija preko RabbitMQ | topic exchange `chronoshop.events`, publisher u order-service, konzumer u payment-service, povratni tok `payment.completed` → promena `OrderStatus`, dead-letter queue, retry politika |
| 8 | `feat/reactive-notifications` | Reaktivni notification servis (WebFlux + SSE) | novi WebFlux servis na 8085, `Sinks.Many` broadcast, RxJava 3 operator sloj nad tokom događaja, SSE endpoint `/api/notifications/stream`, `EventSource` hook u React-u sa toast prikazom |
| 9 | `feat/grpc-stock-check` | gRPC provera zaliha između order-a i catalog-a | `shared/src/main/proto/stock.proto`, gRPC server u catalog-service, klijent u order-service, `CheckStock` + `ReserveStock`, REST fallback pri nedostupnosti |

**Kraj dana:** sva četiri tipa komunikacije rade — tačka 1 specifikacije je zatvorena.

---

## Dan 4 — 21.08. (petak) · Kontejnerizacija

| # | Grana | PR | Sadržaj |
|---|---|---|---|
| 10 | `feat/dockerfiles` | Multi-stage Dockerfile za svaki servis | multi-stage (Maven build → `eclipse-temurin:17-jre-alpine`), non-root korisnik, `.dockerignore` po servisu, `HEALTHCHECK` direktiva, sve env varijable eksternalizovane, `scripts/docker-build.ps1` sa semantičkim tagovanjem |
| 11 | `feat/docker-compose` | Compose orkestracija celog sistema | 6 servisa + PostgreSQL + RabbitMQ, `infra/postgres/init-databases.sh` (kreira 4 baze), mreže `chronoshop-net` i `chronoshop-data`, **healthcheck na svakom servisu**, `depends_on: condition: service_healthy`, imenovani volume-i (`pg-data`, `rabbit-data`, `uploads-data`), `mem_limit`, `.env.example` |
| 12 | `test/unit-and-integration` | Integracioni testovi i JaCoCo pokrivenost | Testcontainers (Postgres + RabbitMQ), `@SpringBootTest` po servisu, Vitest + React Testing Library za frontend, JaCoCo izveštaji u `verify` fazi |

**Kraj dana:** `docker compose up -d` diže ceo sistem; aplikacija radi na `localhost:5173`.
Ovo je najrizičniji dan — ostavi vremena za debagovanje mreže i redosleda podizanja.

---

## Dan 5 — 22.08. (subota) · CI i kvalitet koda

| # | Grana | PR | Sadržaj |
|---|---|---|---|
| 13 | `ci/github-actions` | CI pipeline — build, testovi, artefakti | `.github/workflows/ci.yml`: okida se na **svaki push i svaki PR**, matrix po servisima, Maven build sa keširanjem, svi unit i integracioni testovi, upload jar artefakata, Docker build (bez push-a na PR) |
| 14 | `ci/static-analysis` | Statička analiza — SonarCloud, Checkstyle, ESLint | `sonar-project.properties`, Sonar job u CI sa quality gate-om na **New Code**, Spotless + Checkstyle (Google style) za Javu, ESLint + Prettier za React, `pre-commit` hook |
| 15 | `test/e2e-playwright` | End-to-end testovi nad podignutim stack-om | Playwright scenario: registracija → login → pretraga kataloga → dodavanje u korpu → porudžbina → plaćanje → SSE notifikacija stiže; `e2e.yml` workflow koji podiže compose i pušta testove |

**Kraj dana:** zeleni check-ovi na PR-ovima. Sad uključi *Require status checks* u branch protection-u.

---

## Dan 6 — 23.08. (nedelja) · Observability i CD

| # | Grana | PR | Sadržaj |
|---|---|---|---|
| 16 | `feat/observability` | Logovi, metrike i trace-ovi u svim servisima | Micrometer + Actuator `/actuator/prometheus`, OpenTelemetry OTLP export, `logstash-logback-encoder` za JSON logove, `traceId`/`spanId` u svakom log zapisu, propagacija korelacionog ID-a kroz gateway i RabbitMQ poruke |
| 17 | `feat/monitoring-stack` | Prometheus, Grafana, Loki i Tempo | `docker-compose.observability.yml`, `infra/prometheus/prometheus.yml` scrape config za svih 6 servisa, Promtail → Loki, Tempo za trace-ove, **provisioned Grafana dashboard: latency p50/p95/p99, throughput req/s, error rate 5xx%** po servisu, alert pravilo na error rate > 5% |
| 18 | `ci/cd-pipeline` | CD workflow sa deploy-em i post-deploy proverom | `.github/workflows/cd.yml` na push u `main`: build → push u `ghcr.io` sa tagovima `latest` i `sha-xxxxxxx` → `infra/deploy.sh` (compose pull + up -d) → smoke test svih `/actuator/health` → automatski rollback na prethodni tag pri padu |

**Kraj dana:** Grafana na `localhost:3000` pokazuje sva tri stuba. CD prolazi kraj-do-kraja.

---

## Dan 7 — 24.08. (ponedeljak) · Dokumentacija i zatvaranje

| Zadatak | Sadržaj |
|---|---|
| PR #19 `docs/projektna-dokumentacija` | arhitektonski dijagram, sekvencni dijagram toka porudžbine, opis svakog servisa i njegovih endpoint-a, uputstvo za pokretanje, **tabela usklađenosti sa svih 10 tačaka spec-a**, screenshot-ovi Grafana dashboard-a i uspešnih CI/CD run-ova, screenshot istorije PR-ova |
| Finalna provera | čist `git clone` → `docker compose up -d` → sve mora da radi iz nule |
| Provera kontrolne liste | proći svih 10 tačaka iz `CLAUDE.md` i potvrditi fajlom za svaku |

---

## 25.08. — predaja (rezerva)

Šalje se:

- Link ka repozitorijumu
- Istorija pull request-ova (`/pulls?q=is%3Apr+is%3Aclosed`)
- Link ka deploy-ovanoj aplikaciji — lokalno: `http://localhost:5173` + `docker compose ps` kao dokaz
- Projektna dokumentacija (PDF)

Na mejlove: `sofijadjordjevic@uns.ac.rs` i `masa.saranovic@uns.ac.rs`
(**dokumentacija najmanje tri dana pred termin odbrane** — ako je odbrana ranije od 28.08., šalji odmah po završetku.)

---

## Šta ti treba pre nego što se počne

1. **Docker Desktop** pokrenut, minimum 8 GB memorije (Settings → Resources)
2. **`gh auth login`** — GitHub CLI, drastično ubrzava kreiranje PR-ova
3. **SonarCloud nalog** povezan sa GitHub-om (treba tek 22.08., ali napravi ranije)
4. **Repo secrets:** `SONAR_TOKEN`, `STRIPE_SECRET_KEY`, `STRIPE_WEBHOOK_SECRET`, `JWT_SECRET`
   (`GITHUB_TOKEN` za GHCR postoji automatski)
5. Prazan lokalni folder za novi projekat, sa `git init` i dodatim remote-om

---

## Ako se kasni — redosled žrtvovanja

1. **gRPC (PR #9)** — opciono po spec-u, prvo ide napolje
2. **Loki i Tempo (deo PR #17)** — ostavi samo Prometheus + Grafana; logovi i trace-ovi se demonstriraju kroz `docker logs` i Actuator
3. **Broj endpoint-a u servisima** — svaki servis mora imati bar jedan, ostalo je bonus
4. **Wishlist funkcionalnost** — najmanje bitna, može ostati u order-service kao stub

**Nikad ne žrtvuj:** PR workflow, CI, Docker/Compose, statičku analizu, CD, monitoring.
To je devet od deset tačaka specifikacije.
