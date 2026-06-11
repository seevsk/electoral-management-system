package com.ems.backend.service;

import com.ems.backend.entity.Voter;

import java.util.List;

public interface VoterService {

    List<Voter> findAll();

    List<Voter> findAllActive();

    Voter findById(Integer id);

    Voter save(Voter voter, String dni); // recibe el dni en lugar del account

    Voter update(Integer id, Voter voter);

    // Cambia el estado a Inactivo (I)
    void disable(Integer id);

    // Cambia el estado a Activo (A)
    void enable(Integer id);

    // =========================================================================
    // MÉTODOS PARA OBTENER ESTADÍSTICAS DE PARTICIPACIÓN CIUDADANA
    // =========================================================================

    /**
     * Obtiene la cantidad total de electores con estado Activo (A).
     */
    long getElectoresHabilitados();

    /**
     * Obtiene la cantidad de electores activos que ya han votado (hasVoted = true).
     */
    long getVotantesAsistentes();

    /**
     * Obtiene el listado de ámbitos de votación (departamentos) con su total de
     * electores, asistentes y porcentaje de participación.
     */
    List<java.util.Map<String, Object>> getParticipationByScope();
}

