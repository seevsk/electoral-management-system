-- =========================================================================
-- MIGRACIÓN FLYWAY: V18__enable_sample_voters.sql
-- Comentario descriptivo: Activa los votantes sembrados en el entorno de desarrollo
-- y simula la asistencia para que el dashboard de participación ciudadana
-- muestre información interactiva real de inmediato.
-- =========================================================================

-- Activar a todos los votantes de desarrollo pasándolos a estado 'A' (Activo)
UPDATE voters 
SET status = 'A' 
WHERE status = 'I';

-- Simular la asistencia de aproximadamente el 70% de los votantes (IDs no divisibles por 3)
UPDATE voters 
SET has_voted = 1,
    voted_at = DATEADD(minute, - (id * 5), GETDATE())
WHERE id % 3 != 0;
