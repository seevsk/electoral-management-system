package com.ems.backend.dto;

import java.util.List;

public class PresidentialResultsDto {

    private final Integer electionId;
    private final String electionName;
    private final Integer electionYear;
    private final String selectedDistrictCode;
    private final List<CandidateResultDto> candidateResults;
    private final long blankVoteCount;
    private final double blankVotePercentage;
    private final long chartMaxVotes;

    public PresidentialResultsDto(Integer electionId, String electionName, Integer electionYear,
                                  String selectedDistrictCode, List<CandidateResultDto> candidateResults,
                                  long blankVoteCount, double blankVotePercentage, long chartMaxVotes) {
        this.electionId = electionId;
        this.electionName = electionName;
        this.electionYear = electionYear;
        this.selectedDistrictCode = selectedDistrictCode;
        this.candidateResults = candidateResults;
        this.blankVoteCount = blankVoteCount;
        this.blankVotePercentage = blankVotePercentage;
        this.chartMaxVotes = chartMaxVotes;
    }

    public Integer getElectionId() { return electionId; }
    public String getElectionName() { return electionName; }
    public Integer getElectionYear() { return electionYear; }
    public String getSelectedDistrictCode() { return selectedDistrictCode; }
    public List<CandidateResultDto> getCandidateResults() { return candidateResults; }
    public long getBlankVoteCount() { return blankVoteCount; }
    public double getBlankVotePercentage() { return blankVotePercentage; }
    public long getChartMaxVotes() { return chartMaxVotes; }
}
