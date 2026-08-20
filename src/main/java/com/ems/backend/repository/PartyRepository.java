package com.ems.backend.repository;

import com.ems.backend.entity.Party;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface PartyRepository extends JpaRepository<Party, Integer> {

    List<Party> findAllByOrderByListPositionAsc();

    List<Party> findByIsActiveTrueOrderByListPositionAsc();

    boolean existsByNameIgnoreCase(String name);

    boolean existsByAcronymIgnoreCase(String acronym);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Integer id);

    boolean existsByAcronymIgnoreCaseAndIdNot(String acronym, Integer id);

    @Query("SELECT COALESCE(MAX(p.listPosition), 0) FROM Party p")
    Integer findMaxListPosition();

    @Query(value = """
            WITH target_election AS (
                SELECT TOP 1 e.id
                FROM elections e
                ORDER BY
                    CASE e.status
                        WHEN 'A' THEN 1
                        WHEN 'P' THEN 2
                        WHEN 'C' THEN 3
                        ELSE 4
                    END,
                    e.[year] DESC,
                    e.id DESC
            )
            SELECT
                per.party_id AS party_id,
                LTRIM(RTRIM(CONCAT(v.first_surname, ' ', v.second_surname, ' ', v.full_name))) AS representative_name
            FROM party_election_representatives per
            INNER JOIN target_election te
                ON te.id = per.election_id
            INNER JOIN candidates c
                ON c.id = per.candidate_id
            INNER JOIN voters v
                ON v.id = c.voter_id
            WHERE per.party_id IN (:partyIds)
            """, nativeQuery = true)
    List<Object[]> findRepresentativeNamesByPartyIdsForPriorityElection(Collection<Integer> partyIds);
}
