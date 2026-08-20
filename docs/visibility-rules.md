# Matriz de visibilidad y reglas de negocio (Partidos, Candidatos y Cedula)

## Proposito

Este documento define, de forma operativa y funcional, como se debe comportar el sistema cuando existen:

- Partidos activos e inactivos.
- Candidatos activos e inactivos.
- Partidos sin representante asignado para una eleccion.
- Elecciones en estado pendiente, en curso o cerradas.

El objetivo es que **backend, frontend y QA** trabajen con los mismos criterios para evitar inconsistencias.

---

## Contexto del modelo de datos

### Tablas clave

- `parties`: datos base del partido (nombre, siglas, estado, logo, posicion en lista).
- `elections`: proceso electoral (tipo, anio, ventana de tiempo, estado).
- `candidates`: candidatura de un votante en una eleccion y partido.
- `party_election_representatives`: tabla puente que define el representante de un partido para una eleccion.

### Fuente de verdad del representante

El representante visible del partido para una eleccion **no** debe salir de `parties.representative` (columna legacy), sino de:

`party_election_representatives -> candidates -> voters/accounts`

Esto permite mantener historial por eleccion y evita duplicidad de datos.

---

## Definiciones funcionales

- **Representante asignado**: existe fila en `party_election_representatives` para `(party_id, election_id)`.
- **Sin representante**: no existe fila en puente para ese partido y eleccion.
- **Partido inactivo**: `parties.is_active = 0`.
- **Candidato inactivo**: `candidates.is_active = 0`.
- **Eleccion en curso**: fecha/hora actual entre `start_date` y `end_date` y estado operativo habilitado por sistema.

---

## Matriz de visibilidad por modulo

## 1) Modulo Admin Partidos (listado y estado)

### Que se debe mostrar

- Mostrar partidos segun pantalla:
  - Lista principal: normalmente activos (segun diseno actual).
  - Panel de estado: activos e inactivos.
- Mostrar representante por eleccion objetivo (activa o seleccionada), leyendo desde tabla puente.
- Si no existe representante, mostrar texto `SIN REPRESENTANTE`.

### Reglas de estado

- El hecho de que el partido este inactivo **no borra** su mapeo historico de representante.
- En vistas administrativas se puede mostrar representante incluso si partido/candidato estan inactivos.
- Mostrar badges de estado para claridad operativa:
  - Partido: `Activo` / `Inactivo`.
  - Candidato asociado: `Activo` / `Inactivo` (si existe representante).

### Que no debe ocurrir

- No ocultar representante en admin solo por `party.is_active = 0`.
- No editar representante manualmente desde el CRUD de partidos.

---

## 2) Modulo Admin Candidatos

### Alta de candidato

- El candidato se registra vinculando:
  - `voter_id` (origen padron).
  - `party_id`.
  - `election_id`.
- Luego se crea/actualiza la fila correspondiente en `party_election_representatives` para formalizar representacion.

### Seleccion de votante

- No usar dropdown masivo cuando el padron es grande.
- Usar buscador por DNI y nombre (autocomplete o filtro server-side).

### Reglas de negocio

- Debe existir relacion valida de partido y eleccion para representar.
- No permitir asignaciones ambiguas o duplicadas para la misma eleccion.
- Si candidato/partido estan inactivos, su participacion en cedula debe bloquearse aunque se conserve historial admin.

---

## 3) Cedula de votacion (modulo de votacion)

### Criterio estricto de publicacion

La cedula debe mostrar solo candidaturas que cumplan todas las condiciones:

- Eleccion en curso.
- Partido activo.
- Candidato activo.
- Relacion valida partido-eleccion-representante.

### Exclusiones esperadas

- Partido inactivo: no aparece en cedula.
- Candidato inactivo: no aparece en cedula.
- Partido sin representante para esa eleccion: no aparece en cedula.

---

## Reglas para habilitar/inhabilitar (borrado logico)

## Partido

- **Permitir inhabilitar**: fuera de eleccion en curso.
- **Bloquear inhabilitar**: si la eleccion relevante esta en curso y afectaria votacion activa.
- Al inhabilitar:
  - Se conserva historial.
  - Se excluye de cedula.

## Candidato

- **Permitir inhabilitar**: fuera de eleccion en curso.
- **Bloquear inhabilitar**: durante eleccion en curso presidencial activa.
- Al inhabilitar:
  - Se conserva historial.
  - Se excluye de cedula.

---

## Politica de imagenes (Cloudinary)

### Principio

- La UI debe consumir `photo_url` desde BD.
- No construir URLs por formula con `candidate.id` en frontend/backend.

### Casos validos

- Si hay representante y `photo_url` existe: mostrar foto.
- Si hay representante y `photo_url` es null/vacio: mostrar placeholder.
- Si no hay representante: mostrar `SIN REPRESENTANTE` (y placeholder opcional).

### Nota de mapeo actual

En este proyecto, por gaps de representantes (ej. partidos 4 y 14), puede existir diferencia entre:

- `candidates.id` en BD
- ruta de carpeta en Cloudinary

Esto es valido mientras `photo_url` apunte al recurso correcto.

---

## Criterios de aceptacion funcional (QA)

1. Crear partido nuevo sin representante:
   - Se crea correctamente.
   - En partidos se muestra `SIN REPRESENTANTE` para la eleccion objetivo.

2. Registrar candidato para ese partido y eleccion:
   - Se crea candidatura.
   - Se crea/actualiza puente.
   - Al volver al modulo de partidos aparece el nombre del representante.

3. Inhabilitar partido/candidato fuera de eleccion en curso:
   - Operacion permitida.
   - No se pierde historial.
   - Se excluye de cedula.

4. Intentar inhabilitar durante eleccion en curso:
   - Operacion bloqueada con mensaje de negocio claro.

5. Partidos sin representante (caso esperado):
   - Se visualizan en admin como `SIN REPRESENTANTE`.
   - No aparecen en cedula.

---

## Recomendacion de implementacion incremental

1. Ajustar modulo Partidos para lectura de representante desde puente (read-only).
2. Consolidar alta/edicion de representante en modulo Candidatos.
3. Validar flujo completo con una eleccion activa.
4. Cuando todo este estable, retirar dependencia de `parties.representative` y dropear columna legacy en migracion aparte.
