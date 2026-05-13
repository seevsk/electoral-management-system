# Electoral Management System - Backend

Backend API for the Electoral Management System focused on Lima Metropolitana (43 districts), designed to support voter identity validation, account activation, ballot casting, and public election reporting.

## Scope

- This document covers only the `backend/` module.
- Frontend is developed as a separate client application and consumes this backend through REST endpoints.
- Current geographic scope: Lima Metropolitana (43 districts), with a scalable schema for broader national growth.

## Project Context

This system simulates a presidential election process with two authenticated actors (`admin`, `user`) and one public view (no authentication):

- `admin`: configures and manages electoral entities (voters, parties, candidates, elections).
- `user` (voter): activates account using DNI identity data, authenticates, and votes.
- public users: can access participation and results endpoints according to election state.

## Backend Tech Stack

- Java 21
- Spring Boot 3.5.14
- Spring Web
- Spring Data JPA (Hibernate)
- Spring Security
- JWT (`jjwt 0.11.5`)
- Flyway
- SQL Server (Microsoft JDBC driver)
- Lombok
- Maven

## Architecture

- Monorepo module: `backend/`
- Backend style: REST API (no server-side rendering)
- Security: stateless JWT with server-side token persistence (`tokens` table) for real logout
- Data integrity: JPA/service validations + SQL constraints (PK, FK, UNIQUE)

## Database Model (Core)

Main tables:

- `locations`: INEI ubigeo reference data
- `accounts`: authentication identity (`dni`, password hash, role, active flag)
- `voters`: electoral citizen profile linked to `accounts`
- `tokens`: active JWT token registry for logout invalidation
- `parties`, `candidates`, `elections`, `votes`

Important relationship:

- `voters.account_id -> accounts.id` (FK)
- `accounts.dni` is unique and used as login identifier for both admin and users

## Business Rules (Critical)

1. One vote per voter per election (service-level check + DB UNIQUE constraint).
2. Election availability is controlled by `start_date` and `end_date`.
3. Full candidate results are visible only after election closure.
4. Account activation uses fixed DNI identity fields (`birth_date`, `dni_expiry_date`, `location_code`).
5. Admin account exists only in `accounts` (no `voters` row), so admin cannot vote.
6. Ballot party display order must use `parties.list_position`.

## Authentication and Identity Flow

### Preloaded Data Pattern

- Admin account is seeded with:
  - `role='admin'`
  - `is_active=1`
  - BCrypt `password_hash`
- Voter accounts are seeded with:
  - `role='user'`
  - `is_active=0`
  - `password_hash=NULL`
- Voter profile rows are seeded in `voters` with `status='I'`.

### Activation

When voter identity data matches the preloaded voter record:

- `accounts.password_hash` is set (BCrypt)
- `accounts.is_active` becomes `1`
- `voters.status` becomes `'A'`

All updates must happen in a single transaction.

### Recovery

Password recovery validates DNI identity data and updates only `accounts.password_hash` (unless business rules explicitly require activation changes).

## Flyway Migrations

Current migration baseline:

- `V1__create_core_schema.sql` - Core schema creation
- `V2__seed_locations_lima_metropolitana.sql` - 43 Lima Metropolitana districts
- `V3__seed_admin_account.sql` - Admin account seed
- `V4__seed_initial_voter_accounts.sql` - Initial voter account seeds
- `V5__seed_initial_voter_profile.sql` - Initial voter profile seed

Notes:

- Do not use `CREATE DATABASE`, `USE`, or `GO` inside Flyway migration files.
- Database must already exist (`EMS`), Flyway manages schema objects and seeded data.

## Local Configuration

### 1) Environment Variables

Create local file: `backend/.env` (not tracked)

Example values:

```properties
DB_URL=jdbc:sqlserver://YOUR_SERVER;databaseName=EMS;encrypt=true;trustServerCertificate=true
DB_USER=your_sql_user
DB_PASSWORD=your_sql_password

JWT_SECRET=your_secret_key_min_256_bits
JWT_EXPIRATION=900000

CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret
```

Reference template (tracked): `backend/.env.example`

### 2) Application Properties

`application.properties` imports `.env` using:

```properties
spring.config.import=optional:file:.env[.properties]
```

Server context path is currently:

```properties
server.servlet.context-path=/api
```

## Run the Backend

From repository root (Windows PowerShell):

```powershell
.\backend\mvnw.cmd spring-boot:run
```

Or from `backend/`:

```powershell
.\mvnw.cmd spring-boot:run
```

## API Status

Backend implementation is being built incrementally.

Current priority sequence:

1. Voter activation
2. Login (admin + voter)
3. Logout (token invalidation)
4. Password recovery

Then admin management modules and voting/result modules.

## Git Workflow (Backend)

- Use short-lived feature branches from `main`.
- Open PRs and merge in GitHub web.
- Use standard PR merge strategy configured in the repository.
- Keep secrets out of commits (`.env` must remain local).

## Team

- Escriba Arango, Cristhian Luis
- Salas Rojas, Sebastian Jose
- Velasquez Cespedes, Salvador Jovany
- Villanueva Guillen, Aarom Josue
