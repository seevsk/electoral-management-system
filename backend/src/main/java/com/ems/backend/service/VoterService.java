package com.ems.backend.service;

import com.ems.backend.entity.Candidate;
import com.ems.backend.entity.Voter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface VoterService {

    List<Voter> findAll();

    List<Voter> findAllActive();

    Voter findById(Integer id);

    Voter save(Voter voter, String dni, Integer votingTableId);

    Voter update(Integer id, Voter voter, Integer votingTableId);

    Page<Voter> findPaginated(int page, int size, String search, String statusFilter);

    // Cambia el estado a Inactivo (I)
    void disable(Integer id);

    // Cambia el estado a Activo (A)
    void enable(Integer id);


    //probando la paginacion para ver los formatos json
    Page<Voter> findAll(Pageable pageable);

    // =========================================================================
    // MÉTODOS PARA OBTENER ESTADÍSTICAS DE PARTICIPACIÓN CIUDADANA
    // =========================================================================

    /**
     * Obtiene el listado de ámbitos de votación (departamentos) con su total de
     * electores, asistentes y porcentaje de participación.
     */
    List<java.util.Map<String, Object>> getParticipationByScope();

    /**
     * Comentario descriptivo: Obtiene el listado consolidado de participación ciudadana
     * para cada uno de los distritos de Lima Metropolitana.
     */
    List<java.util.Map<String, Object>> getParticipationByDistrict();

    /**
     * NUEVO: Obtiene todos los ubigeos (departamentos, provincias, distritos)
     * para los filtros de ubicación en cascada.
     */
    List<java.util.Map<String, Object>> getAllUbigeos();
}

