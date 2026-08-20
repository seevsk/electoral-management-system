# Plan de integración: Voting Locations, Voting Tables & Voter Assignments — PA4

## Contexto

El profesor Luis Fernando Flores Mejía solicitó explícitamente para PA4 la integración de
locales y mesas de votación. El feedback cita brechas tecnológicas, resistencia al cambio y
limitaciones digitales como razón por la que todo ciudadano debe poder consultar su lugar de
votación — **incluso sin estar autenticado**. El sistema sigue siendo 100% electrónico; el
voto presencial es un "wont have" del backlog (responsabilidad de ONPE/RENIEC). Estos datos
son metadata del censo electoral (padrón simulado), no implican voto presencial.

Decisión de alcance confirmada: migrar los 50,000 votantes completos + los 3 CSV de
locales/mesas. Todo a través de Flyway — el equipo solo ejecuta las migraciones.

---

## Hallazgos clave resueltos

### Voter ID mapping: directo y confirmado
- `V20__seed_voters.sql` inserta los primeros 12,500 DNIs de `votantes.csv` en el mismo
  orden del archivo, sin ningún ORDER BY alternativo.
- SQL Server IDENTITY(1,1) asigna `voters.id = 1, 2, 3 … 12,500` en ese orden.
- V22 continuará con `voters.id = 12,501 … 50,000`.
- `MesaVotante.votante = N` → `voters.id = N`: mapeo 1:1 directo. Sin ambigüedad.
- `votantes.csv` ya contiene los 50,000 registros con datos reales — no hay Excel externo.

### Mapeo de distritos (CSV → locations)
- `LocalVotacion.csv` usa Title Case con tildes: `"Ancón"`, `"Breña"`, `"Rímac"`.
- `locations` usa MAYÚSCULAS sin tildes: `"ANCON"`, `"BRENA"`, `"RIMAC"`.
- Solución: normalización en la generación del SQL (strip de tildes + UPPER). El SQL de
  migración hardcodea el `location_code` ya resuelto — sin JOINs dinámicos en migración.

---

## Tablas nuevas (nombres en inglés)

| CSV fuente       | Tabla DB           | Entidad Java       |
|------------------|--------------------|--------------------|
| LocalVotacion    | `voting_location`  | `VotingLocation`   |
| MesaVotacion     | `voting_table`     | `VotingTable`      |
| MesaVotante      | `voter_assignment` | `VoterAssignment`  |

### Schema

```sql
CREATE TABLE dbo.voting_location (
    id             INT IDENTITY(1,1) NOT NULL,
    name           VARCHAR(150)      NOT NULL,
    address        VARCHAR(200)      NOT NULL,
    location_code  CHAR(6)           NOT NULL,   -- FK → locations.location_code
    capacity       INT               NOT NULL,
    is_active      BIT               NOT NULL,
    created_at     DATETIME          NOT NULL,
    updated_at     DATETIME          NULL,
    CONSTRAINT PK_voting_location PRIMARY KEY (id),
    CONSTRAINT FK_vl_location FOREIGN KEY (location_code)
        REFERENCES locations(location_code)
);

CREATE TABLE dbo.voting_table (
    id                  INT IDENTITY(1,1) NOT NULL,
    table_number        VARCHAR(10)       NOT NULL,  -- ej. "M0001"
    capacity            INT               NOT NULL,
    voting_location_id  INT               NOT NULL,  -- FK → voting_location.id
    created_at          DATETIME          NOT NULL,
    updated_at          DATETIME          NULL,
    CONSTRAINT PK_voting_table PRIMARY KEY (id),
    CONSTRAINT UQ_vt_number UNIQUE (table_number),
    CONSTRAINT FK_vt_location FOREIGN KEY (voting_location_id)
        REFERENCES voting_location(id)
);

CREATE TABLE dbo.voter_assignment (
    id               INT IDENTITY(1,1) NOT NULL,
    voter_id         INT               NOT NULL,  -- FK → voters.id, UNIQUE (1 voter = 1 asignación)
    voting_table_id  INT               NOT NULL,  -- FK → voting_table.id (muchos voters por mesa)
    assigned_at      DATE              NOT NULL,
    created_at       DATETIME          NOT NULL,
    CONSTRAINT PK_voter_assignment PRIMARY KEY (id),
    CONSTRAINT UQ_va_voter UNIQUE (voter_id),     -- un voter, una sola asignación
    CONSTRAINT FK_va_voter FOREIGN KEY (voter_id)
        REFERENCES voters(id),
    CONSTRAINT FK_va_table FOREIGN KEY (voting_table_id)
        REFERENCES voting_table(id)
);
```

**Relaciones de cardinalidad:**
- `voter_assignment.voter_id` → UNIQUE: un voter tiene máximo una asignación.
- `voter_assignment.voting_table_id` → sin UNIQUE: muchos voters pueden estar en la misma mesa (relación N:1 voter_assignment → voting_table).
- No se modifica ninguna tabla existente. La relación es unidireccional: `voter_assignment → voters`.

---

## Migraciones Flyway

Todo generado como SQL puro — el equipo ejecuta Flyway y listo.

| Versión | Contenido                                                                      | Filas aprox.  |
|---------|--------------------------------------------------------------------------------|---------------|
| V22     | accounts + voters filas 12,501–50,000 (batches de 1,000, igual que V20)       | ~37,500 × 2   |
| V23     | CREATE TABLE voting_location, voting_table, voter_assignment                   | —             |
| V24     | INSERT 800 voting_locations con location_code resuelto (tildes normalizadas)   | 800           |
| V25     | INSERT 500 voting_tables (voting_location_id = id del CSV, orden preservado)   | 500           |
| V26     | INSERT 50,000 voter_assignments (voter_id = votante CSV, voting_table_id = mesa CSV) | 50,000   |

V24–V26 insertan en orden del CSV → IDENTITY genera ids 1..N que coinciden con los ids del CSV, sin necesitar IDENTITY_INSERT.

Los 43 locales con `estado = "Inactivo"` se migran con `is_active = 0`.

---

## Reglas de negocio

1. Un voter tiene **una sola asignación** (`UNIQUE` en `voter_assignment.voter_id`).
   Una mesa puede tener **muchos voters** asignados (sin restricción en `voting_table_id`).
2. Solo se asignan locales con `is_active = 1` — los inactivos no aparecen en dropdowns del admin.
3. El local asignado debe pertenecer al mismo distrito del voter:
   `voting_location.location_code = voter.location_code`. El backend valida esto en el PUT.
4. La capacidad de la mesa es informativa — no se bloquea por exceso en esta entrega.
5. Admin no puede crear locales ni mesas — solo asigna desde los datos del profesor.
6. La consulta de local de votación es **pública** — no requiere autenticación. Cualquier
   ciudadano puede buscar por DNI (brecha tecnológica: puede ir a un kiosko sin login).
7. El voto presencial es **wont have** — la ONPE registraría ese voto. No se implementa.

---

## Endpoints nuevos

### Público (sin autenticación)
```
GET /voters/voting-location?dni={dni}
```
Flujo: DNI → `accounts` → `voters` → `voter_assignment` → `voting_table` → `voting_location`.
Si no tiene asignación: 404 con mensaje claro.

Response:
```json
{
  "voterName": "María Gutierrez Ramos",
  "votingTable": "M0010",
  "votingLocation": {
    "name": "I.E. José Carlos Mariátegui",
    "address": "Av. La Marina 9161",
    "district": "San Martín de Porres"
  }
}
```

### Admin — dropdowns en cascada
```
GET /voting-locations?districtCode={code}   → locales activos del distrito
GET /voting-tables?locationId={id}          → mesas del local seleccionado
```

### Admin — asignar o actualizar mesa de un voter
```
PUT /voters/{id}/voting-assignment
Body: { "votingTableId": 42 }
```
Upsert: crea si no existe, actualiza si ya tenía asignación.
Validación backend: `voting_table → voting_location.location_code == voter.location_code`.
Si no coincide distrito: 400 Bad Request.

### Voter detail existente — campo adicional
El response actual del voter detail agrega `votingAssignment` nullable:
```json
{
  "votingAssignment": {
    "tableNumber": "M0010",
    "locationName": "I.E. José Carlos Mariátegui",
    "locationAddress": "Av. La Marina 9161",
    "district": "San Martín de Porres"
  }
}
```
`null` si el voter no tiene asignación aún.

---

## Flujo de asignación en el admin (cascade)

El admin edita/crea un voter. En la sección de asignación:

1. Distrito del voter ya está seleccionado (campo existente del voter).
2. **Voting Location** dropdown: `GET /voting-locations?districtCode={voter.locationCode}`, solo activos.
3. Al seleccionar local → **Voting Table** dropdown: `GET /voting-tables?locationId={id}`.
4. Al confirmar → `PUT /voters/{id}/voting-assignment`.

Si el admin también cambia el distrito del voter: el dropdown de local se resetea (manejo frontend). El backend siempre valida coherencia de distrito en el PUT.

La asignación es **opcional** al crear/editar: si el admin no la completa, el voter queda sin asignación temporalmente hasta que se la asignen.

---

## Archivos a crear / modificar

### Nuevos
```
db/migration/
  V22__seed_voters_remaining.sql
  V23__create_voting_schema.sql
  V24__seed_voting_locations.sql
  V25__seed_voting_tables.sql
  V26__seed_voter_assignments.sql

entity/
  VotingLocation.java
  VotingTable.java
  VoterAssignment.java

repository/
  VotingLocationRepository.java
  VotingTableRepository.java
  VoterAssignmentRepository.java

service/
  VotingAssignmentService.java

controller/
  VotingLocationController.java   ← GET público DNI lookup + GET dropdowns admin

dto/
  VoterVotingLocationResponse.java
  VotingAssignmentDto.java
```

### Modificados
```
dto/VoterDetailDto.java (o equivalente)  ← agregar campo votingAssignment nullable
service/VoterService.java                ← upsert de asignación + lookup por DNI
controller/VoterController.java          ← PUT /{id}/voting-assignment
```

---

## Estrategia de ramas

Trabajo por objetivos, una rama por objetivo:

| Rama | Objetivo |
|------|----------|
| `feature/seed-voters-complete` | V22: completar 50,000 voters |
| `feature/voting-schema-and-seed` | V23–V26: tablas + seed de locales, mesas y asignaciones |
| `feature/voting-location-lookup` | Endpoint público GET por DNI |
| `feature/voting-assignment-admin` | Endpoints admin (dropdowns + PUT asignación) + voter detail update |

---

## Verificación final

```sql
SELECT COUNT(*) FROM voters;                                    -- 50,000
SELECT COUNT(*) FROM voting_location;                           -- 800
SELECT COUNT(*) FROM voting_table;                              -- 500
SELECT COUNT(*) FROM voter_assignment;                          -- 50,000

-- Sin huérfanos:
SELECT COUNT(*) FROM voter_assignment va
LEFT JOIN voters v ON va.voter_id = v.id
WHERE v.id IS NULL;                                             -- 0

-- Coherencia de distrito:
SELECT COUNT(*) FROM voter_assignment va
JOIN voting_table vt ON va.voting_table_id = vt.id
JOIN voting_location vl ON vt.voting_location_id = vl.id
JOIN voters v ON va.voter_id = v.id
WHERE vl.location_code != v.location_code;                      -- 0
```

- App arranca sin errores de Hibernate `ddl-auto=validate`.
- `GET /voters/voting-location?dni=58008824` sin token → 200 con local y mesa.
- `GET /voting-locations?districtCode=140114` → solo locales activos de Magdalena del Mar.
- `PUT /voters/{id}/voting-assignment` con local de otro distrito → 400.
