package com.ems.backend.dto;

public class VoterVotingLocationResponse {

    private final String voterFullName;
    private final String tableNumber;
    private final String locationName;
    private final String locationAddress;
    private final String district;

    public VoterVotingLocationResponse(String voterFullName, String tableNumber,
                                       String locationName, String locationAddress,
                                       String district) {
        this.voterFullName = voterFullName;
        this.tableNumber = tableNumber;
        this.locationName = locationName;
        this.locationAddress = locationAddress;
        this.district = district;
    }

    public String getVoterFullName() { return voterFullName; }
    public String getTableNumber() { return tableNumber; }
    public String getLocationName() { return locationName; }
    public String getLocationAddress() { return locationAddress; }
    public String getDistrict() { return district; }
}
