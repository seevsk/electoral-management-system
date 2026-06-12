-- =========================================================================
-- MIGRACIÓN FLYWAY: V19__reset_voter_participation.sql
-- Revierte la simulación de votos hecha en V18, limpiando has_voted y
-- voted_at para que el dashboard parta de datos reales (0 asistentes).
-- =========================================================================

UPDATE voters
SET has_voted = 0,
    voted_at = NULL;
