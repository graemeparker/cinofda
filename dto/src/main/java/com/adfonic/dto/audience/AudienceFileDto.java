package com.adfonic.dto.audience;

import java.util.Date;

public class AudienceFileDto {

    private String id;
    private Date date;
    private String fileName;
    private String status;
    private String subType;
    private Long valids;
    private Long totals;
    private String sessionId;
    private Long audienceTotals;
    private String audienceStatus;
    
    public AudienceFileDto(String id, Date date, String fileName, String status, String subType, Long valids, Long totals) {
        this(id, date, fileName, status, subType, valids, totals, "", 0L, "");
    }
    
    public AudienceFileDto(String id, Date date, String fileName, String status, String subType, Long valids, Long totals, String sessionId, Long audienceTotals, String audienceStatus) {
        super();
        this.id = id;
        this.date = date;
        this.fileName = fileName;
        this.status = status;
        this.subType = subType;
        this.valids = valids;
        this.totals = totals;
        this.sessionId = sessionId;
        this.audienceTotals = audienceTotals;
        this.audienceStatus = audienceStatus;
    }

    public String getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }

    public String getFileName() {
        return fileName;
    }

    public String getStatus() {
        return status;
    }

    public String getSubType() {
        return subType;
    }

    public Long getValids() {
        return valids;
    }

    public Long getTotals() {
        return totals;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Long getAudienceTotals() {
        return audienceTotals;
    }

    public String getAudienceStatus() {
        return audienceStatus;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        boolean isEquals = true;
        if (this == obj){
            isEquals = true;
        } else if (obj == null){
            isEquals = false;
        } else if (getClass() != obj.getClass()){
            isEquals = false;
        } else if ((id == null) && (((AudienceFileDto) obj).id != null)){
            isEquals = false;
        } else {
            isEquals = id.equals(((AudienceFileDto) obj).id);
        }   
        return isEquals;
    }
}
