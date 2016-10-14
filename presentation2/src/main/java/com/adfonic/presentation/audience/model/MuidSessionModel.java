package com.adfonic.presentation.audience.model;

import java.math.BigDecimal;

public class MuidSessionModel {

    private String status;
    private BigDecimal ingested;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getIngested() {
        return ingested;
    }

    public void setIngested(BigDecimal ingested) {
        this.ingested = ingested;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MuidSessionModel [status=").append(status).append(", ingested=").append(ingested).append("]");
        return builder.toString();
    }

}
