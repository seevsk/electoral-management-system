package com.ems.backend.repository;

import com.ems.backend.entity.Voter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VoterRepository extends JpaRepository<Voter, Integer> {

    Optional<Voter> findByAccount_Id(Integer accountId);

    @Query("select v from Voter v join fetch v.account a where a.dni = :dni")
    Optional<Voter> findByAccountDni(@Param("dni") String dni);

    // Verifica si una cuenta ya está asociada a un votante
    boolean existsByAccount_Id(Integer accountId);

    // Verifica si una cuenta ya está asociada a otro votante (para edición)
    boolean existsByAccount_IdAndIdNot(Integer accountId, Integer id);

    // Listar todos los votantes ordenados por nombre completo
    List<Voter> findAllByOrderByFullNameAsc();

    // Listar votantes por estado ordenados por nombre completo
    List<Voter> findByStatusOrderByFullNameAsc(String status);

    // Total de votantes registrados en un distrito (para el eje Y de los resultados presidenciales)
    long countByLocationCode(String locationCode);

    @Query("""
            select v
            from Voter v
            join fetch v.account a
            where a.role = 'user'
              and a.isActive = true
              and v.status = 'A'
            order by v.fullName asc, v.firstSurname asc, v.secondSurname asc, a.dni asc, v.id asc
            """)
    List<Voter> findEligibleUserVotersForCandidates();

    @Query("""
            select v
            from Voter v
            join fetch v.account a
            where a.role = 'user'
              and a.isActive = true
              and v.status = 'A'
              and a.dni = :dni
            """)
    Optional<Voter> findEligibleUserVoterByDni(@Param("dni") String dni);

    @EntityGraph(attributePaths = {"account"})
    @Query("""
            select v
            from Voter v
            join fetch v.account a
            where a.role = 'user'
              and a.isActive = true
              and v.status = 'A'
              and (
                  lower(v.fullName) like lower(concat('%', :term, '%'))
                  or lower(v.firstSurname) like lower(concat('%', :term, '%'))
                  or lower(v.secondSurname) like lower(concat('%', :term, '%'))
                  or a.dni like concat('%', :term, '%')
              )
            order by v.fullName asc, v.firstSurname asc, v.secondSurname asc, a.dni asc, v.id asc
            """)
    List<Voter> searchEligibleUserVotersForCandidates(@Param("term") String term);

    // =========================================================================
    // PAGINACIÓN SERVIDOR — PÁGINA DE ESTADO DE VOTANTES
    // =========================================================================

    @Query(
        value = """
            select v from Voter v
            join fetch v.account a
            where (:statusFilter is null or v.status = :statusFilter)
              and (:search is null
                   or lower(v.fullName)      like lower(concat('%', :search, '%'))
                   or lower(v.firstSurname)  like lower(concat('%', :search, '%'))
                   or lower(v.secondSurname) like lower(concat('%', :search, '%'))
                   or a.dni                   like concat('%', :search, '%'))
            order by v.firstSurname asc, v.secondSurname asc
            """,
        countQuery = """
            select count(v) from Voter v
            join v.account a
            where (:statusFilter is null or v.status = :statusFilter)
              and (:search is null
                   or lower(v.fullName)      like lower(concat('%', :search, '%'))
                   or lower(v.firstSurname)  like lower(concat('%', :search, '%'))
                   or lower(v.secondSurname) like lower(concat('%', :search, '%'))
                   or a.dni                   like concat('%', :search, '%'))
            """
    )
    Page<Voter> findPaginatedForStatusPage(
        @Param("search") String search,
        @Param("statusFilter") String statusFilter,
        Pageable pageable
    );

    // =========================================================================
    // CONSULTAS PARA PARTICIPACIÓN CIUDADANA
    // =========================================================================

    /**
     * Obtiene la participación agrupada por departamentos usando hasVoted
     * para calcular emitidos y pendientes.
     */
    @Query("""
            select l.department,
                   count(v),
                   sum(case when v.hasVoted = true then 1L else 0L end),
                   sum(case when v.hasVoted = false then 1L else 0L end)
            from Voter v
            join Location l on v.locationCode = l.locationCode
            group by l.department
            """)
    List<Object[]> getParticipationByScope();

    @Query("""
            select l.department, l.province, l.locationCode, l.district,
                   count(v),
                   sum(case when v.hasVoted = true then 1L else 0L end),
                   sum(case when v.hasVoted = false then 1L else 0L end)
            from Voter v
            join Location l on v.locationCode = l.locationCode
            where l.department = 'LIMA'
            group by l.department, l.province, l.locationCode, l.district
            order by l.district asc
            """)
    List<Object[]> getParticipationByDistrict();

    @Query("""
        SELECT l.department, l.province, l.district, l.locationCode,
               COUNT(v.id),
               SUM(CASE WHEN v.hasVoted = true THEN 1L ELSE 0L END),
               SUM(CASE WHEN v.status = 'A' AND v.hasVoted = false THEN 1L ELSE 0L END),
               SUM(CASE WHEN v.status = 'I' THEN 1L ELSE 0L END)
        FROM Location l
        LEFT JOIN Voter v ON v.locationCode = l.locationCode
        WHERE l.department IS NOT NULL
          AND l.province IS NOT NULL
          AND l.district IS NOT NULL
        GROUP BY l.department, l.province, l.district, l.locationCode
        ORDER BY l.department, l.province, l.district
        """)
    List<Object[]> findAllUbigeos();
}

