# Architecture

Technical deep-dive into how the Electoral Management System is built. For the project pitch, tech stack summary, and setup instructions, see the [root README](../README.md).

## 1. Overview

The system is a **server-rendered MVC monolith** built with Spring Boot — there is no separate frontend service. Thymeleaf templates are rendered directly by the application, and a small set of JSON REST endpoints exist only to feed client-side widgets (charts, an interactive map, live participation counters).

```
Browser
  │
  ├─ HTML pages ───────► @Controller (Thymeleaf) ─┐
  │                                                │
  └─ fetch()/JS widgets ► @RestController (JSON) ──┤
                                                    ▼
                                        Service layer (business rules)
                                                    │
                                                    ▼
                                    Repository layer (Spring Data JPA)
                                                    │
                                                    ▼
                                          Entities ──► SQL Server
```

## 2. Package layout

| Package | Responsibility |
|---|---|
| `config/` | `SecurityConfig`, `JwtService`, `JwtAuthenticationFilter`, `MvcConfig`, `CloudinaryConfig`, `ReportAsyncConfig` |
| `controller/` | Thymeleaf MVC controllers — one per module (auth, voters, parties, candidates, elections, voting, admin, reports) |
| `restcontroller/` | `@RestController` JSON endpoints under `/api/**`, consumed by the frontend JS (charts, map, live counters) |
| `dto/` | Request/response payloads — controllers and REST endpoints never expose JPA entities directly |
| `entity/` | JPA entities, one per table |
| `repository/` | Spring Data JPA repositories |
| `service/` (+ `impl/`, `report/`) | Business logic; `impl/` holds implementations, `report/` holds the report-row projections used by the Excel export pipeline |

## 3. Authentication & authorization

- **Stateless JWT**, carried in an HttpOnly cookie (not `Authorization` header) — `JwtService` issues/parses/clears the cookie, `JwtAuthenticationFilter` runs once per request ahead of Spring Security's own filter chain.
- **Password hashing**: BCrypt (`PasswordEncoder` bean in `SecurityConfig`).
- **Roles**: `ADMIN`, `OPERATOR`, `USER` (voter). Enforced declaratively in `SecurityConfig`:

  | Path pattern | Access |
  |---|---|
  | `/`, `/participation`, `/presidencial`, `/login/**`, `/activation`, `/recovery/**`, `/auth/**`, `/api/parties/**`, `/api/candidates/**`, `/api/participation/**`, `/api/presidencial/results`, static assets | Public |
  | `/admin/reports/**` | `ADMIN` or `OPERATOR` |
  | `/admin/**` | `ADMIN` |
  | `/voter/**`, `/vote/**` | `USER` |

- Session creation policy is `STATELESS` — no `HttpSession`-based auth, no `tokens` table (the schema still has one for historical reasons, but it's unused).
- Unauthenticated access to a protected route redirects to the appropriate login page (`/login/admin` vs `/login/voter`) based on the request path, instead of returning a bare 401.
- Two independent JWT expirations: shorter for admin/operator sessions, longer for voter sessions (`JWT_EXPIRATION_ADMIN`, `JWT_EXPIRATION_VOTER`).

## 4. Data model

Core entities and their relationships (`accounts` is the identity table shared by every role; `voters` extends it 1:1 for the voter role only):

```
accounts (dni, password_hash, role, is_active)
    │ 1:1 (voters.account_id → accounts.id)
    ▼
voters (full_name, birth_date, dni_expiry_date, location_code, status, has_voted, voted_at)
    │
    ├──< candidates (voter_id, party_id, election_id, list_number, photo_url, is_active)
    │        │
    │        └──> parties (name, acronym, list_position, logo_url, is_active)
    │        └──> elections (name, election_type, year, start_date, end_date, status)
    │
    ├──< votes (voter_id, candidate_id [nullable], election_id, voted_at)
    │
    └──1:1 voter_assignment (voter_id, voting_table_id, assigned_at)
                 └──> voting_table ──> voting_location (Lima Metropolitana venues)

party_election_representatives (party_id, election_id, candidate_id) — bridge table;
resolves each party's representative *per election* instead of a single static
`parties.representative` column (that column is legacy and being phased out).
```

Key constraints worth calling out:

- `accounts.dni` is `UNIQUE` and doubles as the login identifier for every role.
- Admin accounts intentionally have **no** row in `voters` — an admin cannot vote by construction, not just by business-rule validation.
- `votes` enforces **one vote per voter per election** with both a service-layer check and a database `UNIQUE` constraint on `(voter_id, election_id)` — belt and suspenders.
- `candidates.list_number` and `parties.list_position` drive ballot ordering; `id` is never used for display order.
- `locations` (Lima Metropolitana's 43 districts, INEI ubigeo codes) is the referential backbone for `voters.location_code`, `voting_location`, and district-level participation aggregation.

## 5. Schema evolution (Flyway)

All schema changes go through Flyway (`src/main/resources/db/migration`, `V1`–`V28`); Hibernate runs with `ddl-auto=validate`, meaning it never generates or alters schema — Flyway is the single source of truth. Roughly, in order:

1. **Baseline** (`V1`–`V5`): core schema, Lima Metropolitana location seed, initial admin/voter accounts.
2. **Auth simplification** (`V6`): drops the `tokens` table — session state moved to stateless JWT instead of DB-persisted tokens.
3. **Party/candidate catalog** (`V7`–`V17`): party seeds, candidate pool, photo URLs, representative bridge table, admin test credentials.
4. **Voting infrastructure** (`V18`–`V26`): nullable `candidate_id` on `votes` (to allow blank/null ballots), voter import infrastructure, full 50k-voter seed, voting locations/tables and voter-to-table assignments.
5. **Reporting** (`V27`–`V28`): `report_jobs` table for async report tracking, operator test account.

## 6. Async reporting pipeline

Report generation (`ReportController`, `ReportService`, `ReportExcelGenerator`) runs off the request thread:

1. A report request creates a `ReportJob` row (`ReportJobStatus`: pending → running → completed/failed).
2. `ReportAsyncConfig` provides a dedicated thread pool executor; `ReportJobAsyncProcessor` picks up the job, queries data through `ReportDataQueryService`, and builds the workbook via Apache POI (`ReportExcelGenerator`).
3. The client polls job status (`ReportJobStatusDto`) instead of blocking on a long HTTP request — this keeps large exports (e.g. full voter rosters) from timing out a synchronous call.
4. Generated files are written to `generated-reports/` (gitignored, machine-local — never committed).

## 7. Public dashboards

- **Participation view** (`/participation`): aggregates voter turnout by district, rendered on an interactive Leaflet map using `lima_callao_distritos_simple.geojson`.
- **Presidential results** (`/presidencial`): Chart.js bar chart of votes per candidate, gated by election status — full results are only exposed once an election's `end_date` has passed.

Both are served through `restcontroller/ParticipationRestController` and `PresidentialResultsController`, kept deliberately public and unauthenticated per `SecurityConfig`.

## 8. Internationalization

`spring.messages.basename=i18n/messages` resolves `messages.properties` (Spanish, default), `messages_en.properties`, and `messages_qu.properties` (Quechua) — every user-facing string in the Thymeleaf templates goes through the message source rather than being hardcoded.

## 9. Business rules quick reference

- One vote per voter per election (service + DB `UNIQUE`).
- Election results/visibility are gated by `start_date`/`end_date`, not a manually-toggled flag.
- Admin cannot vote (no `voters` row).
- Account activation and password recovery validate identity via DNI fields (`birth_date`, `dni_expiry_date`, `location_code`) — there is no email/phone-based flow.
- Ballot and party-list ordering always uses `list_number`/`list_position`, never the database `id`.
