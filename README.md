# Electoral Management System

Monorepo for the Electoral Management System project focused on Lima Metropolitana (43 districts), simulating a full presidential election flow: voter activation, authentication, ballot casting, and public result visibility.

## Repository Structure

```text
electoral-management-system/
├── backend/   # Spring Boot API (active)
└── frontend/  # React client (in progress)
```

## Modules

### Backend (`/backend`)
- Java 21 + Spring Boot 3.5.14
- Spring Security + JWT
- Spring Data JPA + SQL Server
- Flyway migrations (`V1`...`V5`)
- Detailed setup and architecture: see `backend/README.md`

### Frontend (`/frontend`)
- React + Vite
- Routing/state/forms/UI stack defined by frontend owner
- Detailed setup will live in `frontend/README.md` once module is fully integrated

## Current Status

- Backend schema and initial seed baseline are in place.
- Initial Lima Metropolitana ubigeo seed is available.
- Auth-related backend flows are the current implementation priority:
  1. Account activation
  2. Login (admin + voter)
  3. Logout
  4. Password recovery

## Core Domain Notes

- Login identifier is DNI (`accounts.dni`).
- Admin account exists in `accounts` only.
- Voter identity/profile is stored in `voters`, linked by `account_id`.
- Activation and recovery use DNI identity fields (no email or phone recovery flow).
- One vote per voter per election is enforced in service logic and database constraints.

## Local Development

### Backend
Use the backend module README for complete setup:
- `backend/README.md`

### Frontend
Setup instructions will be maintained in:
- `frontend/README.md`

## Configuration and Secrets

- Never commit real credentials.
- Use local `.env` files and tracked `.env.example` templates.
- Database target: SQL Server (`EMS`).

## Collaboration Workflow

- Use feature branches from `main`.
- Open Pull Requests for review.
- Merge strategy follows repository standard configuration.

## Team

- Escriba Arango, Cristhian Luis
- Salas Rojas, Sebastian Jose
- Velasquez Cespedes, Salvador Jovany
- Villanueva Guillen, Aarom Josue
