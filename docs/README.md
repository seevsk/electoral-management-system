# Documentacion funcional backend

Esta carpeta centraliza reglas funcionales y de visibilidad para el equipo.

## Documentos

- `domain-rules-and-flow.md`
  - Reglas de dominio completas del MVP.
  - Flujo `accounts -> voters -> candidates`.
  - Reglas de activacion, alta de votante por admin, candidatura, voto y resultados.
  - Politicas de estado (`A/I`, `is_active`) y bloqueos en eleccion en curso.

- `visibility-rules.md`
  - Matriz de visibilidad por modulo (admin partidos, admin candidatos, cedula).
  - Reglas de como mostrar `SIN REPRESENTANTE` y estados activos/inactivos.

## Nota de uso

Si hay conflicto de interpretacion entre implementaciones, tomar `domain-rules-and-flow.md` como referencia principal y actualizar ambos documentos para mantener consistencia.
