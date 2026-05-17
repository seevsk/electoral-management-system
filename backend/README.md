# Sistema de Gestion Electoral - Backend

Backend del Sistema de Gestion Electoral enfocado en Lima Metropolitana (43 distritos).
Para PA2 se trabaja con Spring Boot MVC + Thymeleaf y flujos CRUD funcionales.

## Alcance

- Este documento cubre solo el modulo `backend/`.
- En PA2 las vistas se renderizan desde backend con Thymeleaf.
- El alcance geografico actual es Lima Metropolitana (43 distritos), con esquema escalable.

## Contexto del Proyecto

Este sistema simula un proceso de eleccion presidencial con dos actores autenticados (`admin`, `user`) y una vista publica (sin autenticacion):

- `admin`: configura y gestiona entidades electorales (votantes, partidos, candidatos, elecciones).
- `user` (votante): activa cuenta con datos DNI, autentica y emite voto.
- publico: accede a informacion de participacion y resultados segun estado de la eleccion.

## Stack Tecnologico Backend

- Java 21
- Spring Boot 3.5.14
- Spring Web
- Spring Data JPA (Hibernate)
- Spring MVC + Thymeleaf
- Flyway
- SQL Server (Microsoft JDBC driver)
- Lombok
- Maven

## Arquitectura

- Modulo monorepo: `backend/`
- Estilo backend PA2: MVC con renderizado server-side (Thymeleaf)
- Integridad de datos: validaciones en servicio/JPA + constraints SQL (PK, FK, UNIQUE)

## Modelo de Base de Datos (Nucleo)

Tablas principales:

- `locations`: datos de ubigeo INEI
- `accounts`: identidad de autenticacion (`dni`, `password_hash`, `role`, `is_active`)
- `voters`: perfil electoral asociado a `accounts`
- `parties`, `candidates`, `elections`, `votes`

Relacion importante:

- `voters.account_id -> accounts.id` (FK)
- `accounts.dni` es unico y se usa como identificador de login para admin y votantes

## Reglas de Negocio (Criticas)

1. Un voto por votante por eleccion (validacion de servicio + UNIQUE en BD).
2. Disponibilidad de eleccion controlada por `start_date` y `end_date`.
3. Resultados completos por candidato visibles al cierre de eleccion.
4. Activacion y recuperacion validan datos de identidad DNI (`birth_date`, `dni_expiry_date`, `location_code`).
5. Admin existe solo en `accounts` (sin fila en `voters`), por lo tanto no vota.
6. El orden de partidos en cedula usa `parties.list_position`, nunca `id`.

## Ajuste Temporal PA2

Para esta entrega PA2 se definio temporalmente:

- No usar JWT en esta fase.
- No usar persistencia de tokens (`tokens` table).
- No aplicar hasheo de contrasena en esta fase.

Estos puntos se reintroducen en una fase posterior de hardening sin perder el avance CRUD.

## Migraciones Flyway

Baseline actual:

- `V1__create_core_schema.sql` - Creacion del esquema base
- `V2__seed_locations_lima_metropolitana.sql` - Seed de 43 distritos de Lima Metropolitana
- `V3__seed_admin_account.sql` - Seed de cuenta admin
- `V4__seed_initial_voter_accounts.sql` - Seed inicial de cuentas de votantes
- `V5__seed_initial_voter_profile.sql` - Seed inicial de perfil de votantes
- `V6__pa2_simplify_auth_schema.sql` - Elimina tabla `tokens` y ajusta `accounts.password_hash` para PA2

Notas:

- No usar `CREATE DATABASE`, `USE` ni `GO` dentro de migraciones Flyway.
- La base (`EMS`) debe existir previamente; Flyway gestiona objetos y seeds.

## Product Backlog Priorizado (PA2)

| ID | Funcionalidad | Prioridad | Estado PA2 |
|---|---|---|---|
| PB-04 | Registrar votantes en el padron (CRUD) | Must have | En progreso |
| PB-05 | Registrar partidos politicos | Must have | En progreso |
| PB-06 | Registrar candidatos presidenciales | Must have | En progreso |
| PB-01 | Login del votante | Must have | Pendiente |
| PB-02 | Activacion de cuenta del votante | Must have | Pendiente |
| PB-03 | Login del administrador | Must have | Pendiente |
| PB-07 | Crear eleccion con fechas | Must have | Pendiente |
| PB-08 | Cedula de sufragio presidencial | Must have | Pendiente |
| PB-09 | Emitir voto y confirmacion | Must have | Pendiente |
| PB-10 | Vista de participacion y resultados publicos | Must have | Pendiente |
| PB-11 | Restricciones del proceso | Must have | Pendiente |
| PB-12 | Redireccion post-voto | Must have | Pendiente |
| PB-13 | Landing publica con estado del proceso | Must have | Pendiente |
| PB-14 | Busqueda de votante por DNI | Should have | Pendiente |
| PB-15 | Restablecimiento de contrasena | Should have | Pendiente |
| PB-16 | Inhabilitar candidato | Should have | Pendiente |
| PB-17 | Editar datos de candidato | Should have | Pendiente |
| PB-18 | Filtro de resultados por distrito | Should have | Pendiente |
| PB-19 | Grafico porcentual de resultados | Should have | Pendiente |
| PB-20 | API REST de resultados electorales | Should have | Pendiente |
| PB-21 | Total de votantes habilitados por distrito | Should have | Pendiente |
| PB-22 | Pantalla de condiciones de uso | Could have | Pendiente |
| PB-23 | Historial de estados de la eleccion | Could have | Pendiente |

## Configuracion Local

### 1) Variables de Entorno

Crear archivo local: `backend/.env` (no versionado)

Valores de ejemplo:

```properties
DB_URL=jdbc:sqlserver://YOUR_SERVER;databaseName=EMS;encrypt=true;trustServerCertificate=true
DB_USER=your_sql_user
DB_PASSWORD=your_sql_password

CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret
```

Plantilla versionada: `backend/.env.example`

### 2) Application Properties

`application.properties` importa `.env` con:

```properties
spring.config.import=optional:file:.env[.properties]
```

Context path actual:

```properties
server.servlet.context-path=/api
```

## Ejecucion

Desde la raiz del repositorio (Windows PowerShell):

```powershell
.\backend\mvnw.cmd spring-boot:run
```

Desde `backend/`:

```powershell
.\mvnw.cmd spring-boot:run
```

## Flujo Git (Backend)

- Usar ramas cortas desde `main`.
- Abrir PR por cada objetivo de negocio.
- Mantener secretos fuera de commits (`.env` debe quedar local).

## Equipo

- Escriba Arango, Cristhian Luis
- Salas Rojas, Sebastian Jose
- Velasquez Cespedes, Salvador Jovany
- Villanueva Guillen, Aarom Josue
