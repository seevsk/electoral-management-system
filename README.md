# Electoral Management System

A full-stack Spring Boot MVC platform that simulates a complete presidential election process for Lima Metropolitana (43 districts, Peru) — from voter onboarding to public results, in a single Java codebase.

Three actors, one system: an **admin** who configures voters/parties/candidates/elections, a **voter** who activates an account, authenticates and casts a ballot, and an unauthenticated **public** view of participation and results.

## Tech Stack

| Layer | Technology |
|---|---|
| Language / Runtime | Java 21 |
| Framework | Spring Boot 3.5.14 (Spring MVC, Spring Data JPA, Spring Security) |
| View layer | Thymeleaf (server-rendered), vanilla JS, Chart.js, Leaflet.js |
| Database | SQL Server, schema/versioning via Flyway |
| Auth | JWT (stateless, HttpOnly cookie) + BCrypt password hashing |
| Media storage | Cloudinary (candidate/party photos) |
| Reporting | Apache POI (async Excel report generation) |
| i18n | Spanish / English / Quechua |
| Build | Maven (wrapper included) |

## Key Features

- **Voter lifecycle**: admin-managed voter registry, DNI-based account activation, password recovery — all identity checks resolve against DNI fields, no email/phone flow.
- **Party & candidate management**: CRUD with Cloudinary photo upload, active/inactive status handling, ballot ordering by `list_position`.
- **Election lifecycle**: time-boxed elections (`start_date` / `end_date`), one vote per voter per election enforced at both service and database level.
- **Authentication & authorization**: JWT-based stateless auth with role-based access (`ADMIN`, `OPERATOR`, `USER`), separate login flows per role.
- **Public dashboards**: live participation metrics with an interactive district map (Leaflet + GeoJSON), and a presidential results view (Chart.js) that unlocks once an election closes.
- **Reporting**: multi-threaded, async Excel report generation with job status tracking (`ReportJob`, `ReportAsyncConfig`).
- **Internationalization**: full UI available in Spanish, English, and Quechua.

## Architecture

This is a **server-rendered MVC monolith**, not a split frontend/backend: Thymeleaf templates are rendered directly by the Spring Boot application, with a small set of REST endpoints (`restcontroller/`) feeding the dynamic widgets (charts, maps, live participation data) via JavaScript.

```
Controller / RestController → Service → Repository → Entity (JPA)
```

Schema evolution is entirely Flyway-managed (28 migrations): baseline schema → identity/location seeds → auth hardening → voting infrastructure (locations, tables, assignments) → reporting.

A deeper technical walkthrough (auth flow, data model, migration history, reporting pipeline) lives in [`docs/architecture.md`](docs/architecture.md).

## Project Structure

```
electoral-management-system/
├── src/
│   ├── main/
│   │   ├── java/com/ems/backend/
│   │   │   ├── config/          # Security, JWT, MVC, Cloudinary, async config
│   │   │   ├── controller/      # Thymeleaf MVC controllers
│   │   │   ├── restcontroller/  # JSON REST endpoints (charts, maps, public data)
│   │   │   ├── dto/
│   │   │   ├── entity/          # JPA entities
│   │   │   ├── repository/      # Spring Data JPA repositories
│   │   │   └── service/         # Business logic (+ impl/, report/)
│   │   └── resources/
│   │       ├── db/migration/    # Flyway migrations (V1..V28)
│   │       ├── templates/       # Thymeleaf views
│   │       ├── static/          # CSS / JS / images
│   │       ├── i18n/            # es / en / qu message bundles
│   │       └── data/            # Seed CSVs and GeoJSON consumed by migrations
│   └── test/
├── docs/                        # Architecture documentation
├── .env.example                 # Local environment variable template
├── pom.xml
└── mvnw / mvnw.cmd               # Maven wrapper
```

## Getting Started

### Prerequisites

- **JDK 21** ([Eclipse Temurin](https://adoptium.net/) or equivalent)
- **SQL Server** instance (local or remote), reachable from your machine
- Maven is **not required** — the project ships with the Maven Wrapper (`mvnw` / `mvnw.cmd`)
- *(Optional)* A [Cloudinary](https://cloudinary.com/) account, if you want candidate/party photo uploads to work

### 1. Clone the repository

```bash
git clone https://github.com/seevsk/electoral-management-system.git
cd electoral-management-system
```

### 2. Create the database

Create an empty database named `EMS` on your SQL Server instance. Flyway takes care of creating and versioning every table on first run — do **not** run any schema SQL manually.

### 3. Configure environment variables

Copy the template and fill in your own values:

```powershell
copy .env.example .env
```

```bash
cp .env.example .env
```

`.env` (gitignored) is loaded automatically by Spring Boot on startup. Required variables:

| Variable | Description |
|---|---|
| `DB_URL` | JDBC URL, e.g. `jdbc:sqlserver://localhost;databaseName=EMS;encrypt=true;trustServerCertificate=true` |
| `DB_USER` / `DB_PASSWORD` | SQL Server credentials |
| `JWT_SECRET` | Base64-encoded, 256-bit minimum. Generate with `openssl rand -base64 32` |
| `JWT_EXPIRATION_ADMIN` / `JWT_EXPIRATION_VOTER` | Token lifetime in milliseconds (defaults provided) |
| `CLOUDINARY_CLOUD_NAME` / `CLOUDINARY_API_KEY` / `CLOUDINARY_API_SECRET` | Only needed if you plan to upload candidate/party photos |

### 4. Run the application

From the repository root:

```powershell
.\mvnw.cmd spring-boot:run
```

```bash
./mvnw spring-boot:run
```

On first run, Flyway applies all 28 migrations automatically — schema creation, Lima Metropolitana location seeds, a demo admin account, and a simulated voter/voting-location dataset (~50,000 records). No manual seeding step is required.

### 5. Open the app

The application runs on **http://localhost:8080/ems** (context path `/ems` is fixed in `application.properties`).

- Public landing / participation dashboard: `/ems/`, `/ems/participation`
- Admin login: `/ems/login/admin`
- Voter login: `/ems/login/voter`

## Business Rules (summary)

- One vote per voter per election, enforced in service logic **and** a database `UNIQUE` constraint.
- Election visibility is time-boxed by `start_date` / `end_date`; full per-candidate results are only shown once an election closes.
- Admin accounts exist only in `accounts` (no `voters` row) — admins cannot vote.
- Account activation and password recovery both validate identity via DNI fields (`birth_date`, `dni_expiry_date`, `location_code`), not email/phone.
- Ballot ordering uses `parties.list_position`, never the raw `id`.

See [`docs/architecture.md`](docs/architecture.md) for the full rule set and role-based visibility matrix.

## Git Workflow

- `developer` is the integration branch for day-to-day work; short-lived branches (`feat/`, `fix/`, `chore/`, `docs/`) branch off it.
- `main` is the protected, stable branch — changes land via reviewed Pull Requests.
- One business objective per PR; squash merge recommended.

## Team

- Escriba Arango, Cristhian Luis
- Salas Rojas, Sebastian Jose
- Velasquez Cespedes, Salvador Jovany
- Villanueva Guillen, Aarom Josue
