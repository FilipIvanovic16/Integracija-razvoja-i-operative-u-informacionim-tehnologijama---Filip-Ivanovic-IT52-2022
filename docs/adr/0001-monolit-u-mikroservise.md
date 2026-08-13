# ADR-0001: Prelazak sa monolitne na mikroservisnu arhitekturu

- **Status:** prihvaceno
- **Datum:** 13.08.2026.

## Kontekst

ChronoShop je razvijen kao Spring Boot monolit sa jednom bazom podataka. Ceo domen
(korisnici, katalog, porudzbine, placanja) deli isti proces i istu semu, pa svaka izmena
zahteva ponovni build i deploy cele aplikacije. Katalog i porudzbine imaju bitno razlicite
profile opterecenja, ali se ne mogu nezavisno skalirati. Padom jednog dela aplikacije pada
ceo sistem.

## Odluka

Monolit se dekomponuje na pet poslovnih mikroservisa iza API Gateway-a, po granicama
domenskih agregata:

| Servis | Odgovornost |
|---|---|
| `auth-service` | korisnici, adrese, izdavanje i validacija JWT tokena |
| `catalog-service` | satovi, brendovi, kategorije, slike, pretraga |
| `order-service` | porudzbine, stavke, lista zelja, kontrola zaliha |
| `payment-service` | Stripe integracija, transakcije, webhook |
| `notification-service` | reaktivne notifikacije ka klijentu |

Primenjuje se obrazac **database-per-service** - svaki servis ima svoju bazu i niko ne
pristupa tudjoj semi direktno.

Komunikacija je namerno raznorodna, kako bi svaki obrazac bio primenjen tamo gde mu je mesto:

- **REST** za sinhrone upite iz klijenta kroz gateway
- **RabbitMQ** za asinhroni tok dogadjaja porudzbina -> placanje -> notifikacija
- **gRPC** za cestu i latencijski osetljivu proveru zaliha izmedju order i catalog servisa
- **Reaktivni tok (WebFlux/Reactor)** za guranje notifikacija ka pretrazivacu preko SSE

Sema baze prelazi sa Hibernate `ddl-auto=update` na **Flyway migracije**, jer automatsko
generisanje seme nije prihvatljivo u pipeline-u koji radi automatizovani deploy.

Baza prelazi sa MS SQL Server-a na **PostgreSQL** - znatno laksi kontejner, brze podizanje
i jednostavnija orkestracija vise instanci u razvojnom okruzenju.

## Posledice

**Pozitivne:** nezavisan deploy i skaliranje servisa; izolacija otkaza; jasne granice
odgovornosti; mogucnost demonstracije punog DevOps lanca nad realnim sistemom.

**Negativne:** distribuirana konzistentnost umesto ACID transakcija preko celog domena;
slozenija lokalna razvojna postavka; obavezan monitoring i pracenje trace-ova jer se jedan
korisnicki zahtev sada prostire kroz vise procesa; duplirani DTO i konfiguracioni kod, sto
se ublazava zajednickim `shared` modulom.
