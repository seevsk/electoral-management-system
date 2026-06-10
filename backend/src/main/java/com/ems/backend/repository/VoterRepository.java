package com.ems.backend.repository;

import com.ems.backend.entity.Voter;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VoterRepository extends JpaRepository<Voter, Integer> {

    Optional<Voter> findByAccount_Id(Integer accountId);

    // Verifica si una cuenta ya está asociada a un votante
    boolean existsByAccount_Id(Integer accountId);

    // Verifica si una cuenta ya está asociada a otro votante (para edición)
    boolean existsByAccount_IdAndIdNot(Integer accountId, Integer id);

    // Listar todos los votantes ordenados por nombre completo
    List<Voter> findAllByOrderByFullNameAsc();

    // Listar votantes por estado ordenados por nombre completo
    List<Voter> findByStatusOrderByFullNameAsc(String status);

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
    // CONSULTAS PARA PARTICIPACIÓN CIUDADANA
    // =========================================================================

    /**
     * Cuenta todos los votantes habilitados filtrando por su estado ('A' = Activo).
     */
    long countByStatus(String status);

    /**
     * Cuenta todos los votantes habilitados ('A') que ya han emitido su voto (hasVoted = true).
     */
    long countByStatusAndHasVotedTrue(String status);

    /**
     * Agrupa los votantes activos por departamento y cuenta el total de electores
     * y cuántos de ellos asistieron a votar.
     */
    @Query("""
            select l.department,
                   count(v),
                   sum(case when v.hasVoted = true then 1L else 0L end)
            from Voter v
            join Location l on v.locationCode = l.locationCode
            where v.status = 'A'
            group by l.department
            """)
    List<Object[]> getParticipationByScope();
}

