package com.ems.backend.service.impl;

import com.ems.backend.service.ReportDataQueryService;
import com.ems.backend.service.report.DistrictParticipationRow;
import com.ems.backend.service.report.ElectoralGeneralReportData;
import com.ems.backend.service.report.PresidentialResultRow;
import com.ems.backend.service.report.ReportSummaryData;
import com.ems.backend.service.report.VoterAssignmentRow;
import com.ems.backend.service.report.VotingLocationRow;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Service
public class ReportDataQueryServiceImpl implements ReportDataQueryService {

    private final JdbcTemplate jdbcTemplate;

    public ReportDataQueryServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ElectoralGeneralReportData loadElectoralGeneralReportData() {
        TargetElection targetElection = findTargetElection();

        long totalElectors = queryLong("select count(*) from dbo.voters");
        long totalVotesEmitted = queryLong("select count(*) from dbo.voters where has_voted = 1");
        long totalVotesPending = queryLong("select count(*) from dbo.voters where has_voted = 0");
        long totalVotesRegistered = queryLong("select count(*) from dbo.votes where election_id = ?", targetElection.id());
        long totalBlankVotes = queryLong("select count(*) from dbo.votes where election_id = ? and candidate_id is null", targetElection.id());

        BigDecimal participationPercentage = totalElectors > 0
                ? BigDecimal.valueOf(totalVotesEmitted)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalElectors), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        List<DistrictParticipationRow> districtParticipationRows = jdbcTemplate.query(
                """
                select l.location_code, l.district,
                       count(v.id) as total_electors,
                       sum(case when v.has_voted = 1 then 1 else 0 end) as votes_emitted,
                       sum(case when v.has_voted = 0 then 1 else 0 end) as votes_pending
                from dbo.locations l
                left join dbo.voters v on v.location_code = l.location_code
                group by l.location_code, l.district
                order by l.district asc, l.location_code asc
                """,
                (rs, rowNum) -> new DistrictParticipationRow(
                        rs.getString("location_code"),
                        rs.getString("district"),
                        rs.getLong("total_electors"),
                        rs.getLong("votes_emitted"),
                        rs.getLong("votes_pending"),
                        toPercentage(rs.getLong("votes_emitted"), rs.getLong("total_electors"))
                ));

        List<PresidentialResultRow> presidentialResultRows = jdbcTemplate.query(
                """
                select c.id as candidate_id,
                       concat(v.full_name, ' ', v.first_surname, ' ', v.second_surname) as candidate_name,
                       p.name as party_name,
                       count(vt.id) as votes
                from dbo.party_election_representatives per
                inner join dbo.candidates c on c.id = per.candidate_id and c.is_active = 1
                inner join dbo.voters v on v.id = c.voter_id
                inner join dbo.parties p on p.id = c.party_id and p.is_active = 1
                left join dbo.votes vt on vt.candidate_id = c.id and vt.election_id = per.election_id
                where per.election_id = ?
                group by c.id, v.full_name, v.first_surname, v.second_surname, p.name
                order by count(vt.id) desc, c.id asc
                """,
                (rs, rowNum) -> {
                    long votes = rs.getLong("votes");
                    return new PresidentialResultRow(
                            targetElection.name(),
                            rs.getString("candidate_name").trim(),
                            rs.getString("party_name"),
                            votes,
                            toPercentage(votes, totalVotesRegistered),
                            false
                    );
                },
                targetElection.id());

        if (totalBlankVotes > 0) {
            presidentialResultRows = new java.util.ArrayList<>(presidentialResultRows);
            presidentialResultRows.add(new PresidentialResultRow(
                    targetElection.name(),
                    "Voto en blanco",
                    "-",
                    totalBlankVotes,
                    toPercentage(totalBlankVotes, totalVotesRegistered),
                    true
            ));
        }

        List<VotingLocationRow> votingLocationRows = jdbcTemplate.query(
                """
                select vl.name as location_name,
                       vl.address,
                       l.location_code,
                       l.district,
                       count(distinct vt.id) as table_count,
                       count(va.id) as assigned_voters_count
                from dbo.voting_location vl
                inner join dbo.locations l on l.location_code = vl.location_code
                left join dbo.voting_table vt on vt.voting_location_id = vl.id
                left join dbo.voter_assignment va on va.voting_table_id = vt.id
                group by vl.name, vl.address, l.location_code, l.district
                order by l.district asc, vl.name asc
                """,
                (rs, rowNum) -> new VotingLocationRow(
                        rs.getString("location_name"),
                        rs.getString("address"),
                        rs.getString("location_code"),
                        rs.getString("district"),
                        rs.getLong("table_count"),
                        rs.getLong("assigned_voters_count")
                ));

        List<VoterAssignmentRow> voterAssignmentRows = jdbcTemplate.query(
                """
                select a.dni,
                       concat(v.full_name, ' ', v.first_surname, ' ', v.second_surname) as voter_name,
                       vl.name as location_name,
                       vt.table_number,
                       l.location_code,
                       l.district
                from dbo.voter_assignment va
                inner join dbo.voters v on v.id = va.voter_id
                inner join dbo.accounts a on a.id = v.account_id
                inner join dbo.voting_table vt on vt.id = va.voting_table_id
                inner join dbo.voting_location vl on vl.id = vt.voting_location_id
                inner join dbo.locations l on l.location_code = vl.location_code
                order by l.district asc, vl.name asc, vt.table_number asc, v.full_name asc
                """,
                (rs, rowNum) -> new VoterAssignmentRow(
                        rs.getString("dni"),
                        rs.getString("voter_name").trim(),
                        rs.getString("location_name"),
                        rs.getString("table_number"),
                        rs.getString("location_code"),
                        rs.getString("district")
                ));

        ReportSummaryData summary = new ReportSummaryData(
                totalElectors,
                totalVotesEmitted,
                totalVotesPending,
                participationPercentage,
                totalVotesRegistered,
                totalBlankVotes,
                LocalDateTime.now()
        );

        return new ElectoralGeneralReportData(
                targetElection.id(),
                targetElection.name(),
                targetElection.year(),
                summary,
                districtParticipationRows,
                presidentialResultRows,
                votingLocationRows,
                voterAssignmentRows
        );
    }

    private TargetElection findTargetElection() {
        List<TargetElection> elections = jdbcTemplate.query(
                """
                select top 1 e.id, e.[name], e.[year]
                from dbo.elections e
                where e.election_type = 'PRESIDENCIAL'
                order by case
                    when e.status = 'A' then 1
                    when e.status = 'P' then 2
                    when e.status = 'C' then 3
                    else 4
                end, e.[year] desc, e.id desc
                """,
                (rs, rowNum) -> new TargetElection(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("year")
                ));

        if (elections.isEmpty()) {
            throw new IllegalStateException("No se encontró una elección presidencial para generar el reporte.");
        }

        return elections.get(0);
    }

    private long queryLong(String sql, Object... args) {
        Long value = jdbcTemplate.queryForObject(sql, Long.class, args);
        return value == null ? 0L : value;
    }

    private BigDecimal toPercentage(long numerator, long denominator) {
        if (denominator <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(numerator)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }

    private record TargetElection(Integer id, String name, Integer year) {}
}
