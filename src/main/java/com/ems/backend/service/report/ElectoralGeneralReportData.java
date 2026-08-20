package com.ems.backend.service.report;

import java.util.List;

public record ElectoralGeneralReportData(
        Integer electionId,
        String electionName,
        Integer electionYear,
        ReportSummaryData summary,
        List<DistrictParticipationRow> districtParticipationRows,
        List<PresidentialResultRow> presidentialResultRows,
        List<VotingLocationRow> votingLocationRows,
        List<VoterAssignmentRow> voterAssignmentRows
) {
}
