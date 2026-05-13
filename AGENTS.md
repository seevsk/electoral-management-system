# AGENTS.md

## Purpose
This repository currently focuses on backend development for the Electoral Management System.
Work must follow a strict GitHub Flow process to keep `main` stable and delivery-oriented.

## Current Scope
- Active scope: `backend/` only.
- Frontend is out of scope until it is added by its owner.

## Backend Stack
- Java 21
- Spring Boot 3.5.14
- Spring Web, Spring Data JPA, Spring Security
- JWT (jjwt 0.11.5)
- Flyway
- SQL Server

## Branching Strategy (GitHub Flow)
- `main` is protected and always stable.
- Create short-lived branches from `main`.
- Open PRs for every change.
- Do not commit directly to `main`.

### Branch naming
Use lowercase kebab-case:
- `feat/backend-<topic>`
- `fix/backend-<topic>`
- `chore/backend-<topic>`
- `docs/backend-<topic>`
- `hotfix/backend-<topic>`

Examples:
- `feat/backend-auth-activation`
- `feat/backend-flyway-v1-v2-v3`
- `feat/backend-admin-voters`
- `fix/backend-vote-duplicate-guard`
- `chore/backend-env-template`

## PR Rules
- One business objective per PR.
- Keep PRs small or medium.
- PR description must include:
  - What changed
  - Why it changed
  - How it was tested
- Squash merge recommended.

## Commit Convention
Use Conventional Commits:
- `feat:`, `fix:`, `chore:`, `docs:`, `refactor:`, `test:`

Examples:
- `feat(auth): implement account activation with dni validation`
- `fix(votes): enforce single vote per election`
- `chore(config): add environment template properties`

## Security and Credentials
- Never commit real secrets.
- Never commit real DB credentials, JWT secret, or Cloudinary keys.
- Keep only templates in version control:
  - `application-example.properties`
  - optional `.env.example`
- Real values come from local/private environment.

## Mandatory Domain Rules
1. One vote per voter per election (service check + DB unique constraint).
2. Election timing is automatic (`start_date` / `end_date`).
3. Results visibility depends on election state.
4. Activation and recovery require DNI identity data validation.
5. Admin cannot vote.
6. Ballot party order is `list_position`, never `id`.

## Backend Work Segmentation (Recommended)
1. `feat/backend-db-schema-flyway`
2. `feat/backend-security-jwt`
3. `feat/backend-auth-activation-recovery`
4. `feat/backend-auth-login-logout-token`
5. `feat/backend-admin-voters-crud`
6. `feat/backend-admin-parties-crud`
7. `feat/backend-admin-candidates-crud`
8. `feat/backend-election-management`
9. `feat/backend-vote-emission`
10. `feat/backend-public-participation`
11. `feat/backend-public-results`
12. `chore/backend-config-secrets-template`

## Definition of Done
A backend task is done when:
- Acceptance criteria are met.
- Build compiles and relevant tests pass.
- Business/security rules are preserved.
- PR is reviewed and merged into `main`.
