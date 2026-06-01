# Fases pendientes de implementacion (despues de Fase 1.1)

## Estado actual

La Fase 1.1 ya esta implementada en backend para Partidos:

- Representante en modulo Partidos es solo lectura.
- El valor se obtiene desde `party_election_representatives` y no desde `parties.representative`.
- Jerarquia para eleccion de contexto en admin list:
  - `A` (Activa) > `P` (Pendiente) > `C` (Cerrada), con fallback a la mas reciente.

Este documento define las fases pendientes para cerrar el MVP sin romper reglas de negocio.

---

## Fase 1.2 - Alta de votantes desde admin (create transaccional)

## Objetivo

Eliminar dependencia de precarga manual para nuevos ciudadanos y formalizar el onboarding desde el sistema.

## Alcance funcional

- En formulario de alta de votante, incluir `dni` obligatorio (8 digitos).
- Crear en una sola transaccion:
  1) `account` (`role='user'`, `is_active=0`, `password_hash=NULL`)
  2) `voter` (`status='I'`, `has_voted=0`, `voted_at=NULL`)
- Asociar `voter.account_id` al `account.id` creado.

## Reglas obligatorias

- DNI unico en `accounts` (respetar `UQ_accounts_dni`).
- DNI inmutable en update de votante.
- No permitir `account` de usuario sin `voter` asociado.
- Excepcion permitida: cuenta `admin` sin `voter`.

## Validaciones backend

- Validar formato DNI.
- Validar `location_code` existente.
- Validar fechas relevantes (nacimiento, caducidad de DNI).

## QA minimo

1. Alta exitosa crea `account` y `voter`.
2. Alta duplicada por DNI se bloquea.
3. Update de votante no permite cambiar DNI.

---

## Fase 1.3 - Create de candidatos y asignacion formal de representante

## Objetivo

Centralizar la asignacion de representante por eleccion dentro del modulo de Candidatos.

## Alcance funcional

- Registro de candidato con `voter_id`, `party_id`, `election_id`.
- Al guardar candidato, crear/actualizar `party_election_representatives`.
- Mantener `photo_url` como fuente de verdad para imagenes.

## Reglas obligatorias

- Candidato debe venir de `voters`.
- Debe cumplir:
  - `accounts.is_active = 1`
  - `voters.status = 'A'`
- Validar unicidades por eleccion y partido.
- No permitir asignaciones ambiguas para la misma eleccion.

## UX recomendada

- Busqueda de votantes por DNI/nombre (no dropdown masivo).

## QA minimo

1. Registrar candidato crea candidatura y puente.
2. En Partidos aparece el representante automaticamente.
3. Si votante esta `I`, bloquear alta de candidato.

---

## Fase 2 - Reglas de bloqueo durante eleccion en curso

## Objetivo

Proteger integridad del proceso electoral cuando una eleccion esta activa y dentro de ventana de votacion.

## Reglas

- Bloquear inhabilitar partido participante en eleccion en curso.
- Bloquear inhabilitar candidato participante en eleccion en curso.
- Bloquear cambios que alteren oferta electoral vigente.

## Consideraciones

- Backend aplica la regla (fuente de autoridad).
- UI solo muestra mensaje de negocio.

## QA minimo

1. Intentar inhabilitar durante eleccion en curso -> bloqueado.
2. Fuera de eleccion en curso -> permitido.

---

## Fase 3 - Activacion, autenticacion y consistencia account-voter

## Objetivo

Consolidar el flujo de activacion y login con consistencia transaccional.

## Reglas

- Activacion valida identidad por datos del padron.
- Activacion exitosa actualiza en una sola transaccion:
  - `accounts.is_active = 1`
  - `voters.status = 'A'`
- Recuperacion de contrasena usa mismo criterio de identidad.

## Riesgo a evitar

- `account` activo y `voter` inactivo por fallo parcial.

## QA minimo

1. Activacion correcta actualiza ambos estados.
2. Login solo habilitado con ambos estados consistentes.

---

## Fase 4 - Proceso de voto y resultados

## Objetivo

Cerrar el flujo operativo de votacion y reporte.

## Reglas de voto

- Cuenta activa.
- Votante activo.
- Eleccion en curso.
- Candidato activo y partido activo.
- Un voto por votante por eleccion.

## Efectos de voto

- `has_voted = 1`
- `voted_at = NOW`

Estos campos no deben ser editables manualmente por admin.

## Resultados

- Agregacion por partido/candidato/eleccion.
- Participacion por `location_code` de votantes.

---

## Fase 5 - Cierre automatico de eleccion

## Objetivo

Asegurar transicion de estado electoral sin intervencion manual de cierre.

## Regla de transicion

- `P -> A`: admin.
- `A -> C`: sistema al superar `end_date`.

## Restricciones

- Admin no debe cerrar manualmente una eleccion activa.
- No se permite reapertura `C -> A`.

---

## Fase 6 - Limpieza de legacy

## Objetivo

Eliminar deuda tecnica cuando el flujo ya dependa totalmente del modelo nuevo.

## Acciones

- Retirar uso de `parties.representative` en codigo.
- Crear migracion para dropear columna legacy en `parties`.

## Precondicion

- Confirmar que ningun modulo usa esa columna como fuente de verdad.

---

## Orden recomendado de ejecucion por equipo

1. Fase 1.2 (Votantes Create transaccional)
2. Fase 1.3 (Candidatos Create + puente)
3. Fase 2 (bloqueos en eleccion en curso)
4. Fase 3 (activacion/login)
5. Fase 4 (voto/resultados)
6. Fase 5 (cierre automatico)
7. Fase 6 (limpieza legacy)

Este orden minimiza conflictos porque primero estabiliza datos base y relaciones FK.
