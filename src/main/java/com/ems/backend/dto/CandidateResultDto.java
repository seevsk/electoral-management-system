package com.ems.backend.dto;

public class CandidateResultDto {

    private final Integer candidateId;
    private final String candidateFullName;
    private final String candidatePhotoUrl;
    private final Integer listNumber;
    private final String partyName;
    private final String partyAcronym;
    private final String partyLogoUrl;
    private final long voteCount;
    private final double percentage;

    public CandidateResultDto(Integer candidateId, String candidateFullName, String candidatePhotoUrl,
                              Integer listNumber, String partyName, String partyAcronym, String partyLogoUrl,
                              long voteCount, double percentage) {
        this.candidateId = candidateId;
        this.candidateFullName = candidateFullName;
        this.candidatePhotoUrl = candidatePhotoUrl;
        this.listNumber = listNumber;
        this.partyName = partyName;
        this.partyAcronym = partyAcronym;
        this.partyLogoUrl = partyLogoUrl;
        this.voteCount = voteCount;
        this.percentage = percentage;
    }

    public Integer getCandidateId() { return candidateId; }
    public String getCandidateFullName() { return candidateFullName; }
    public String getCandidatePhotoUrl() { return candidatePhotoUrl; }
    public Integer getListNumber() { return listNumber; }
    public String getPartyName() { return partyName; }
    public String getPartyAcronym() { return partyAcronym; }
    public String getPartyLogoUrl() { return partyLogoUrl; }
    public long getVoteCount() { return voteCount; }
    public double getPercentage() { return percentage; }
}
