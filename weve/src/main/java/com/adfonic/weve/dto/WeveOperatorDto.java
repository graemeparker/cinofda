package com.adfonic.weve.dto;

public class WeveOperatorDto {

    private final Integer operatorId;
    private Long ipRangeStart;
    private Long ipRangeEnd;
    private final String requestHeaderName;
    private Long decryptionMethod;
    private Boolean beaconServiceFineLoggingOn;
    private Boolean optOutFineLoggingOn;
    
    public WeveOperatorDto(Integer operatorId, 
                              Long ipRangeStart,
                              Long ipRangeEnd,
                              String requestHeaderName,
                              Long decryptionMethod,
                              Boolean beaconServiceFineLoggingOn,
                              Boolean optOutFineLoggingOn) {
        this.operatorId = operatorId;
        this.ipRangeStart = ipRangeStart;
        this.ipRangeEnd = ipRangeEnd;
        this.requestHeaderName = requestHeaderName;
        this.decryptionMethod = decryptionMethod;
        this.beaconServiceFineLoggingOn = beaconServiceFineLoggingOn;
        this.optOutFineLoggingOn = optOutFineLoggingOn;
        
    }
    
    public WeveOperatorDto(Integer operatorId, String requestHeaderName) {
        this.operatorId = operatorId;
        this.requestHeaderName = requestHeaderName;
    }
    
    public WeveOperatorDto(Integer operatorId, 
                           String requestHeaderName,
                           Boolean beaconServiceFineLoggingOn,
                           Boolean optOutFineLoggingOn) {
        this.operatorId = operatorId;
        this.requestHeaderName = requestHeaderName;
        this.beaconServiceFineLoggingOn = beaconServiceFineLoggingOn;
        this.optOutFineLoggingOn = optOutFineLoggingOn;
    }

    public Integer getOperatorId() {
        return this.operatorId;
    }

    public Long getIpRangeStart() {
        return this.ipRangeStart;
    }
    
    public Long getIpRangeEnd() {
        return this.ipRangeEnd;
    }

    public String getRequestHeaderName() {
        return this.requestHeaderName;
    }

    public Long getDecryptionMethod() {
        return this.decryptionMethod;
    }

    public Boolean getBeaconServiceFineLoggingOn() {
        return this.beaconServiceFineLoggingOn;
    }
    
    public Boolean getOptOutFineLoggingOn() {
        return this.optOutFineLoggingOn;
    }
}
