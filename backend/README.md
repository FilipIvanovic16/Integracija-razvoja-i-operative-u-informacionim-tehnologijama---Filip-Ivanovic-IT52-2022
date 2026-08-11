# ChronoShop — Backend

Spring Boot 3 REST API za veb prodavnicu satova.

## Preduslovi
- JDK 17+
- Microsoft SQL Server (pokrenut, sa kreiranom bazom `chronoshop`)
- Maven nije obavezan — koristi se priloženi `mvnw` / `mvnw.cmd`

## Konfiguracija (env promenljive)

Aplikacija čita konfiguraciju iz env promenljivih (sa razumnim podrazumevanim vrednostima u `application.properties`):

| Promenljiva | Opis | Primer |
|-------------|------|--------|
| `DB_URL` | JDBC URL ka SQL Server bazi | `jdbc:sqlserver://localhost:1433;databaseName=chronoshop;encrypt=true;trustServerCertificate=true` |
| `DB_USER` | Korisnik baze | `sa` |
| `DB_PASSWORD` | Lozinka baze | `YourStrong!Passw0rd` |
| `JWT_SECRET` | Base64 tajni ključ za JWT | (generisan) |
| `STRIPE_SECRET_KEY` | Stripe test secret ključ | `sk_test_...` |
| `STRIPE_WEBHOOK_SECRET` | Stripe webhook secret | `whsec_...` |
| `CORS_ORIGINS` | Dozvoljeni frontend origin | `http://localhost:5173` |

### Windows (PowerShell)
```powershell
$env:DB_USER="sa"; $env:DB_PASSWORD="YourStrong!Passw0rd"
.\mvnw.cmd spring-boot:run
```

### Linux / Mac
```bash
export DB_USER=sa DB_PASSWORD='YourStrong!Passw0rd'
./mvnw spring-boot:run
```

## Pokretanje
```bash
./mvnw spring-boot:run
```
API je dostupan na `http://localhost:8080`.

Pri prvom pokretanju, ako je `SEED_ENABLED=true`, baza se puni demo podacima (admin nalog, brendovi, kategorije, satovi).
