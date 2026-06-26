# Plan: Ajustes de navegación admin/participación + módulo de Resultados Presidenciales

## Contexto

El sistema electoral actual muestra módulos y enlaces de navegación que no corresponden al alcance real del proyecto (Senadores, Diputados, Parlamento Andino nunca se implementarán — son "won't have"). Esto genera confusión sobre qué está realmente disponible. Además, el panel admin muestra un resumen estático y específico de "Partidos Políticos" que no resume el sistema en su conjunto, y la navegación pública (`/participation`) tiene anclas deshabilitadas ("Próximamente") para módulos inexistentes.

El tercer objetivo es habilitar la única ancla pendiente que sí tiene sentido para este sistema: **Presidencial**, mostrando los resultados de la elección presidencial (votos por candidato, gráfico de barras con Chart.js, filtro por distrito) una vez que termina el horario de votación — replicando visualmente el estilo de la página de resultados de ONPE Perú (foto de candidato sobre la barra, logo del partido bajo el eje X, barra de estado de "mesas contabilizadas").

---

## Estrategia de ramas

Trabajo por objetivos, una rama por objetivo, partiendo de `developer`:

| Rama | Objetivo | Estado |
|------|----------|--------|
| `feature/admin-main-cards-cleanup` | Objetivo 1 — Cards de `/admin/main` | En curso |
| `feature/navbar-participation-anchors` | Objetivo 2 — Anclas de `/participation` | Pendiente |
| `feature/presidential-results` | Objetivo 3 — Resultados presidenciales | Pendiente |

---

## Objetivo 1 — `templates/admin/main.html`: cards reales y quitar resumen específico

**Archivo:** `src/main/resources/templates/admin/main.html`

- Eliminar las cards "Senadores" (líneas 67-80), "Diputados" (82-95) y "Parlamento Andino" (97-110) — no existen ni existirán en el alcance del proyecto.
- Mantener "Partidos Políticos" (27-45) y "Elecciones" (47-65) tal cual (ya activas y funcionales).
- Añadir 2 cards nuevas, activas, siguiendo exactamente el mismo patrón visual de las cards activas existentes (icono Lucide, badge verde "Activo", botón "Acceder"):
  - **Candidatos** → `th:href="@{/admin/candidates/list}"` (ruta confirmada en `CandidateController`, `@RequestMapping("/admin/candidates")` + `GET /list`). Icono sugerido: `user-check` o `users`.
  - **Votantes** → `th:href="@{/admin/voters/list}"` (ruta confirmada en `VoterController`, `@RequestMapping("/admin/voters")` + `GET /list`). Icono sugerido: `users` o `id-card`.
- Mantener la card "Resultados" (112-125) como "Próximamente" — pasará a "Activo" de forma natural una vez completado el Objetivo 3, pero **no se vincula a `/presidencial` porque esa es una vista pública sin sidebar/layout admin**; queda fuera de alcance enlazarla desde aquí salvo que se indique lo contrario.
- Total final: **5 cards** (Partidos Políticos, Elecciones, Candidatos, Votantes, Resultados) en el mismo grid `sm:grid-cols-2 lg:grid-cols-3` — con 5 elementos el grid se reparte 2/3 en pantallas grandes sin romper responsividad (no se requiere cambiar las clases del grid).
- Eliminar por completo la sección "Resumen del Sistema" (líneas 129-168), incluyendo su comentario `<!-- STATIC: Reemplazar 38, 38, 0 ... -->` — es un detalle específico de partidos políticos, no un resumen real del sistema, tal como señaló el usuario.

---

## Objetivo 2 — `templates/fragments/navbar.html`: anclas públicas reales

**Archivo:** `src/main/resources/templates/fragments/navbar.html` (aplicar el mismo cambio en las 2 secciones: desktop líneas 23-50, mobile líneas 121-140)

- Renombrar el ancla "Inicio" (`th:href="@{/participation}"`, líneas 24-28 y 122-124) a **"Participación Ciudadana"**, conservando la misma ruta y el icono `home`.
- Mantener "Local de Votación" sin cambios (ya funcional).
- Eliminar por completo los `<span>` deshabilitados de "Senadores" (38-41 / 132-133), "Diputados" (42-45 / 134-135) y "Parlamento Andino" (46-49 / 136-138) — no existen en el alcance.
- Convertir el `<span>` deshabilitado "Presidencial" (34-37 / 128-130) en un `<a th:href="@{/presidencial}">` activo, con el mismo patrón de highlighting condicional (`activePage == 'presidencial'`) usado por los otros links.
- El dropdown "Iniciar Sesión" queda sin cambios — ya es el último elemento.
- Resultado final del navbar: **Participación Ciudadana | Local de Votación | Presidencial | Iniciar Sesión** (4 elementos), tal como pidió el usuario.
- Al usar `/presidencial` en una vista, su controlador deberá pasar `activePage='presidencial'` al fragmento navbar para que el highlighting funcione (ver Objetivo 3).

---

## Objetivo 3 — Resultados Electorales Presidenciales (`/presidencial`)

### Decisiones confirmadas con el usuario
- La columna "Para envío al JEE" de la barra de estado de ONPE **se omite** (no aplica a un sistema 100% electrónico sin actas físicas) — la barra de estado solo muestra **Contabilizadas** y **Pendientes**.
- Las fotos de candidatos y logos de partido se ubican en **filas HTML fijas** (CSS Grid) arriba/abajo del `<canvas>` de Chart.js, alineadas por columna con cada barra — no se intenta anclar dinámicamente la foto a la altura exacta de la barra (evita sincronizar píxeles del canvas, más simple de mantener).
- Resultados se muestran siempre para la elección presidencial más reciente (activa, o si no hay activa la cerrada más reciente vía `ElectionRepository.findBestPresidentialElection()`), sin selector de año — no hay histórico relevante en este momento.
- El filtro de distrito es una **lista plana** (no cascada departamento→provincia→distrito), con "TODOS" como primera opción.
- Orden de candidatos: **por votos descendente** (igual que ONPE), no por número de lista.
- "Mesa contabilizada" = mesa con al menos un voto emitido por alguno de sus votantes asignados (no existe el concepto de acta física en este sistema).

### Modelo de datos (ya existente, sin cambios de esquema)
- `Vote` (voter, candidate nullable=voto en blanco, election, votedAt) — tabla vacía hasta que se emitan votos reales vía `/voter/vote/confirm`.
- `Candidate` (voter, party, election, listNumber, photoUrl, isActive) y `Party` (name, acronym, logoUrl) ya tienen las fotos/logos necesarias.
- `VotingTable` → `VotingLocation` → `Location` (relación `@ManyToOne` ya mapeada en ambos saltos) permite filtrar mesas por distrito.
- `Voter.locationCode` (columna compartida con `Location.locationCode`, sin relación JPA mapeada) permite filtrar votos por distrito vía `v.voter.locationCode`.
- `ElectionRepository.findBestPresidentialElection()` ya existe y resuelve la elección relevante con la prioridad correcta (Activa > Pendiente > Cerrada, año desc).

### Archivos nuevos a crear

```
dto/CandidateResultDto.java          — candidateId, candidateFullName, candidatePhotoUrl, listNumber,
                                        partyName, partyAcronym, partyLogoUrl, voteCount, percentage
dto/ElectionStatusSummaryDto.java     — totalTables, tablesCounted, tablesPending, percentageCounted
dto/DistrictOptionDto.java            — districtCode, districtName
dto/PresidentialResultsDto.java       — electionId, electionName, electionYear, selectedDistrictCode,
                                        candidateResults (List<CandidateResultDto>, ordenado por voteCount desc),
                                        statusSummary, blankVoteCount, blankVotePercentage

service/ElectionResultsService.java         (interfaz)
service/impl/ElectionResultsServiceImpl.java

controller/PresidentialResultsController.java

templates/results/presidential-results.html
```

### Repositorios — métodos a agregar

**`VoteRepository.java`** (actualmente solo tiene `existsByVoterIdAndElectionId`):
```java
@Query("""
    select c.id, count(v) from Vote v join v.candidate c
    left join Location l on v.voter.locationCode = l.locationCode
    where v.election.id = :electionId and (:districtCode is null or l.districtCode = :districtCode)
    group by c.id
    """)
List<Object[]> countVotesByCandidate(Integer electionId, String districtCode);

// análogas: countBlankVotes (v.candidate is null) y countTotalVotes (sin filtro de candidate)
```

**`VotingTableRepository.java`**:
```java
// total de mesas y mesas con >=1 voto emitido (join vt.votingLocation loc, join loc.location l),
// filtradas opcionalmente por l.districtCode; "mesa contabilizada" vía EXISTS sobre
// VoterAssignment + Vote del voter asignado a esa mesa para la elección dada.
long countTables(String districtCode);
long countTablesWithAtLeastOneVote(Integer electionId, String districtCode);
```

**`LocationRepository.java`**:
```java
@Query("select distinct l.districtCode, l.district from Location l where l.districtCode is not null order by l.district")
List<Object[]> findDistinctDistricts();
```

**`CandidateRepository.java`**:
```java
List<Candidate> findByElectionIdAndIsActiveTrueOrderByListNumberAsc(Integer electionId);
```

### Service: `ElectionResultsService`

Sigue el patrón interfaz+impl ya usado en el proyecto (`ElectionService`/`ElectionServiceImpl`, etc.):

- `Optional<Election> findRelevantPresidentialElection()` → delega en `ElectionRepository.findBestPresidentialElection()`.
- `boolean areResultsAvailable(Election e)` → `e.getStatus().equals("C") || LocalDateTime.now().isAfter(e.getEndDate())` (cubre el desfase de hasta 60s antes de que `ElectionSchedulerService` actualice el status).
- `PresidentialResultsDto getResults(Integer electionId, String districtCode)` → carga candidatos activos, cruza con conteos de votos (`getOrDefault(id, 0L)` para candidatos sin votos aún), calcula porcentajes sobre el total de votos válidos+blanco, ordena por voteCount desc, construye el `ElectionStatusSummaryDto` (sin columna JEE) vía los conteos de `VotingTableRepository`.
- `List<DistrictOptionDto> getAvailableDistricts()` → delega en `LocationRepository.findDistinctDistricts()`.

### Controller: `PresidentialResultsController`

```java
@GetMapping("/presidencial")
public String presidencial(@RequestParam(required = false) String district, Model model) {
    // 1. findRelevantPresidentialElection() -> si vacío: model.noElectionConfigured=true
    // 2. si existe: areResultsAvailable(election)?
    //    - false -> model.election + model.resultsAvailable=false (vista muestra estado de espera con election.endDate)
    //    - true  -> model.results = getResults(electionId, district), model.selectedDistrict
    // 3. siempre: model.availableDistricts = getAvailableDistricts()
    return "results/presidential-results";
}
```

Ruta pública sin autenticación. Agregar `"/presidencial"` explícitamente a `requestMatchers(...).permitAll()` en `SecurityConfig.java:35` (junto a `"/", "/participation"`) por claridad, aunque ya quedaría cubierta por el `anyRequest().permitAll()` final.

### Vista: `templates/results/presidential-results.html`

Estructura (sigue el patrón de `index.html`: `fragments/head` + `fragments/navbar(activePage='presidencial')` + main):

1. **Caso sin elección configurada**: mensaje genérico.
2. **Caso elección aún no cerrada** (`resultsAvailable == false`): estado de espera con icono reloj y la fecha/hora de `election.endDate` formateada ("Los resultados estarán disponibles al finalizar la votación").
3. **Caso con resultados**:
   - Barra de estado superior: `% counted` grande + "Contabilizadas (N)" / "Pendientes (N)" (2 columnas, sin JEE) + `<form method="get">` con `<select name="district" onchange="this.form.submit()">` (opción "TODOS" = value vacío) — recarga server-side, mismo patrón que `VotingLocationController` (GET con query param, sin Alpine/fetch).
   - Gráfico: `<div class="relative">` con 3 filas en grid (`grid-template-columns: repeat(N, 1fr)` calculado vía `th:style` con el tamaño de la lista de candidatos):
     - Fila superior: foto circular de cada candidato + su `%`.
     - `<canvas id="resultsChart">` con barras verticales (`type: 'bar'`), un solo dataset, sin leyenda, eje X sin ticks (las labels reales van en la fila de logos).
     - Fila inferior: logo del partido + acrónimo.
   - Script Chart.js: mismo patrón ya usado en `index.html` (líneas 420-468) — datos pasados vía atributos `th:data-*` en JSON, leídos con `getAttribute` + `JSON.parse` en un `<script>` plano tras `DOMContentLoaded`.
   - Tabla de detalle (accesible, datos exactos) con candidato, partido, votos, porcentaje — opcional pero recomendable como fallback.

### Archivos existentes a modificar (resumen)

```
templates/fragments/navbar.html         — Objetivo 2
templates/admin/main.html                — Objetivo 1
repository/VoteRepository.java           — 3 queries de agregación
repository/VotingTableRepository.java    — 2 queries de conteo de mesas
repository/LocationRepository.java       — 1 query de distritos distintos
repository/CandidateRepository.java      — 1 query de candidatos activos por elección
config/SecurityConfig.java               — agregar "/presidencial" a permitAll (explícito)
```

---

## Verificación

1. **Objetivo 1**: levantar la app, loguear como admin, visitar `/ems/admin/main` — confirmar 5 cards (Partidos Políticos, Elecciones, Candidatos, Votantes, Resultados), que "Candidatos" y "Votantes" naveguen correctamente a sus listados, que no quede sección de resumen, y que el grid se vea bien en mobile (resize de ventana o devtools).
2. **Objetivo 2**: visitar `/ems/participation` sin login — confirmar navbar con exactamente "Participación Ciudadana | Local de Votación | Presidencial | Iniciar Sesión", que el link a Presidencial funcione, y revisar también el drawer mobile (hamburger).
3. **Objetivo 3**:
   - Sin votos emitidos: visitar `/ems/presidencial` antes de que cierre la elección de prueba → debe mostrar el estado de espera con la fecha de cierre.
   - Forzar/esperar a que la elección cierre (o ajustar `end_date` en BD para pruebas) → revisitar `/ems/presidencial` → debe mostrar el gráfico de barras con candidatos en 0 votos (dataset vacío de `votes`), barra de estado en 0% contabilizado, dropdown de distritos poblado.
   - Emitir 1-2 votos de prueba vía el flujo `/voter/vote/...` y confirmar que el conteo y porcentaje se actualizan al recargar `/ems/presidencial`, y que el filtro por distrito acota correctamente los resultados (probar con un distrito donde se votó vs uno sin votos).
   - Revisar consola del navegador para confirmar que Chart.js renderiza sin errores y que las filas de fotos/logos quedan alineadas con las barras en distintos anchos de pantalla.
