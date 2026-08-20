# Reglas de dominio y flujo operativo (MVP)

## Objetivo

Este documento aterriza las reglas de negocio principales del backend para evitar decisiones ambiguas en implementacion.

Se define el flujo desde `accounts -> voters -> candidates` y su relacion con `parties`, `elections`, `party_election_representatives` y `votes`.

---

## 1. Semantica de estados

## 1.1 Accounts

- `accounts.is_active = 0`: cuenta no activada (sin credenciales operativas).
- `accounts.is_active = 1`: cuenta activada para autenticacion.

## 1.2 Voters

- `voters.status = 'I'`: votante inactivo (no elegible para votar ni para candidatura).
- `voters.status = 'A'`: votante activo (elegible, sujeto a reglas de eleccion en curso).
- Regla operativa acordada:
  - Un votante creado por admin inicia en `status = 'I'`.
  - Cuando el ciudadano activa su cuenta correctamente, pasa a `status = 'A'`.

## 1.3 Parties

- `parties.is_active = 1`: partido habilitado para operaciones de eleccion.
- `parties.is_active = 0`: partido inhabilitado logicamente (se conserva historial).

## 1.4 Candidates

- `candidates.is_active = 1`: candidatura habilitada.
- `candidates.is_active = 0`: candidatura inhabilitada logicamente (se conserva historial).

## 1.5 Elections

- Ventana temporal: `start_date` y `end_date`.
- Semantica oficial de estado:
  - `P`: Pendiente.
  - `A`: Activa.
  - `C`: Cerrada.

La evaluacion de "en curso" debe considerar fecha/hora actual respecto al rango temporal.

### 1.5.1 Transiciones permitidas de eleccion

- `P -> A`: permitido por accion de administrador.
- `A -> C`: solo por sistema cuando se cumple `end_date`.

Transiciones no permitidas:

- `A -> P` (rollback manual).
- `A -> C` manual por administrador.
- `C -> A` (reapertura).

Razon funcional: evitar manipulacion manual del cierre electoral y proteger integridad del proceso.

---

## 2. Fuente de verdad del representante

La representacion por partido es temporal (depende de eleccion). Por ello, la fuente de verdad es:

`party_election_representatives -> candidates -> voters/accounts`

No se debe depender de `parties.representative` para logica funcional nueva.

`parties.representative` queda como campo legacy temporal hasta su retiro en migracion posterior.

---

## 3. Matriz de visibilidad y operacion

## 3.1 Modulo Admin Partidos

- Mostrar representante desde tabla puente para eleccion activa/objetivo.
- Si no existe fila en puente: mostrar `SIN REPRESENTANTE`.
- Mostrar datos aunque partido o candidato esten inactivos en vistas administrativas (auditoria y trazabilidad).
- No editar representante desde este modulo.

## 3.2 Modulo Admin Candidatos

- Alta de candidato requiere `voter_id`, `party_id`, `election_id`.
- Al registrar candidato, crear/actualizar vinculacion en `party_election_representatives`.
- No usar dropdown masivo de votantes; usar busqueda por DNI/nombre.

## 3.3 Cedula de votacion

Incluir solo candidaturas que cumplan todo:

- eleccion en curso,
- partido activo,
- candidato activo,
- votante con `status = 'A'` y cuenta activa.

Excluir partidos sin representante para esa eleccion.

---

## 4. Reglas de bloqueo durante eleccion en curso

Cuando una eleccion presidencial este en curso:

- Bloquear inhabilitacion de partido participante.
- Bloquear inhabilitacion de candidato presidencial participante.
- Bloquear cambios que alteren la oferta electoral vigente.

Fuera de eleccion en curso, se permite inhabilitar/habilitar preservando historial.

---

## 5. Campos administrables vs campos de sistema

## 5.1 Accounts

- Administrable: activacion de cuenta segun flujo definido.
- No administrable manualmente en CRUD general: credenciales sensibles sin flujo controlado.
- Regla de consistencia:
  - No debe existir `account` de usuario sin `voter` asociado.
  - Excepcion valida: cuentas con `role = 'admin'`.

## 5.2 Voters

- Administrable: datos de padron permitidos por negocio (nombres, ubigeo, fechas documentarias, estado A/I).
- Regla sobre DNI:
  - El DNI se captura al crear votante.
  - El DNI queda inmutable despues del alta (no editable en update).
- Solo sistema:
  - `has_voted`
  - `voted_at`

Estos dos campos se actualizan solo en emision de voto.

## 5.3 Parties

- Administrable: nombre, siglas, logo, estado, orden de lista (segun reglas).
- No administrable: representante textual legacy en flujos nuevos.

## 5.4 Candidates

- Administrable: alta/baja logica de candidatura, foto, partido y eleccion asociados.
- Validacion obligatoria: el candidato debe provenir de `voters` y cumplir estado activo.

## 5.5 Votes

- No administrable por CRUD manual.
- Se registra solo via flujo de votacion.

---

## 6. Reglas de integridad por modulo

## 6.1 Activacion y recuperacion de cuenta

- Identidad basada en DNI y datos del votante registrados en padron.
- Sin validacion de identidad, no se permite activar o recuperar.
- Activacion exitosa actualiza:
  - `accounts.is_active = 1`
  - `voters.status = 'A'`

### 6.1.1 Regla de consistencia account-voter

Para cualquier operacion electoral de usuario (voto, candidatura, participacion), deben cumplirse ambos:

- `accounts.is_active = 1`
- `voters.status = 'A'`

Si solo uno esta activo, se considera inconsistencia operativa y la accion debe bloquearse.

Ejemplo de inconsistencia a evitar:

1. El usuario activa cuenta.
2. Se actualiza `accounts.is_active = 1`.
3. Por error parcial no se actualiza `voters.status` y queda en `I`.
4. Resultado: usuario con login potencial pero no elegible para votar/candidatura.

Mitigacion obligatoria:

- La activacion debe ejecutarse en una transaccion unica que actualice ambas tablas.
- Servicios de negocio deben validar ambos estados, no solo uno.

## 6.2 Registro de candidato

Debe validar:

- Existe `account` asociado al DNI y `role = user`.
- Existe `voter` asociado a esa cuenta.
- `voter.status = 'A'`.
- Partido y eleccion validos.
- No romper unicidades por eleccion.

## 6.2.1 Alta de votante desde modulo admin

Flujo transaccional obligatorio en alta:

1. Capturar DNI en formulario de alta de votante.
2. Validar que no exista otro `account` con el mismo DNI (`UQ_accounts_dni`).
3. Crear `account` de usuario:
   - `role = 'user'`
   - `password_hash = NULL`
   - `is_active = 0`
4. Crear `voter` vinculado a `account_id`:
   - `status = 'I'`
   - `has_voted = 0`
   - `voted_at = NULL`
5. Si falla cualquier paso, hacer rollback completo.

Reglas adicionales:

- El DNI no se edita en update de votante.
- No se permite crear `voter` sin `account` asociado.

## 6.3 Emision de voto

Debe validar:

- Cuenta activa.
- Votante activo.
- Eleccion en curso.
- Candidato activo y partido activo.
- Un voto por votante por eleccion.

## 6.4 Cierre automatico de eleccion

Regla de cierre:

- Si la eleccion esta `A` y la hora actual supera `end_date`, el sistema debe marcarla `C` automaticamente.

Restricciones:

- El admin no cierra manualmente una eleccion activa.
- Una eleccion cerrada no puede volver a activa.

---

## 7. Politica de imagenes (Cloudinary)

- Backend/UI consumen `photo_url` y `logo_url` desde BD.
- No construir URLs por formula con IDs.
- Si `photo_url` es null/vacio: usar placeholder visual.
- Si no hay representante: mostrar `SIN REPRESENTANTE`.

Nota: puede haber desfase entre id interno y carpeta Cloudinary por gaps funcionales (ejemplo: partidos sin representante). Es valido si la URL en BD apunta al recurso correcto.

---

## 8. Criterios de aceptacion operativos

1. Crear partido nuevo:
   - se crea sin representante,
   - en listado aparece `SIN REPRESENTANTE` hasta asignacion desde candidatos.

2. Registrar candidato nuevo para partido/eleccion:
   - se crea candidatura,
   - se actualiza puente,
   - en modulo partidos aparece el representante.

3. Inhabilitar partido/candidato fuera de eleccion en curso:
   - permitido,
   - sin perdida de historial,
   - excluido de cedula.

4. Intentar inhabilitar durante eleccion en curso:
   - bloqueado con mensaje de negocio.

5. Voto emitido:
   - marca `has_voted` y `voted_at`,
   - no editable por admin en CRUD.

6. Alta de votante desde admin:
   - crea `account` y `voter` en una sola operacion,
   - `account.is_active = 0`,
   - `voter.status = 'I'`,
   - al activar cuenta, `voter.status` cambia a `A`.

---

## 9. Orden recomendado de implementacion

1. Ajustar modulo Partidos a representante read-only desde puente.
2. Consolidar modulo Candidatos para asignacion formal de representante por eleccion.
3. Aplicar reglas de bloqueo en eleccion en curso.
4. Completar flujo de voto y resultados.
5. Retirar columna legacy `parties.representative` en migracion dedicada cuando el sistema ya no dependa de ella.
