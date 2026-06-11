package com.ems.backend.dto;

public class BallotEntryDto {

    private final Integer partyId;
    private final String partyName;
    private final String partyAcronym;
    private final String partyLogoUrl;
    private final Integer candidateId;
    private final String candidateFullName;
    private final String candidatePhotoUrl;
    private final Integer listNumber;

    public BallotEntryDto(Integer partyId, String partyName, String partyAcronym,
                          String partyLogoUrl, Integer candidateId, String candidateFullName,
                          String candidatePhotoUrl, Integer listNumber) {
        this.partyId = partyId;
        this.partyName = partyName;
        this.partyAcronym = partyAcronym;
        this.partyLogoUrl = partyLogoUrl;
        this.candidateId = candidateId;
        this.candidateFullName = candidateFullName;
        this.candidatePhotoUrl = candidatePhotoUrl;
        this.listNumber = listNumber;
    }

    public Integer getPartyId() { return partyId; }
    public String getPartyName() { return partyName; }
    public String getPartyAcronym() { return partyAcronym; }
    public String getPartyLogoUrl() { return partyLogoUrl; }
    public Integer getCandidateId() { return candidateId; }
    public String getCandidateFullName() { return candidateFullName; }
    public String getCandidatePhotoUrl() { return candidatePhotoUrl; }
    public Integer getListNumber() { return listNumber; }
}
