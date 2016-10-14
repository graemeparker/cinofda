package com.adfonic.dto.campaign.campaigntargetcvr;

import java.math.BigDecimal;

import org.jdto.annotation.Source;

import com.adfonic.dto.BusinessKeyDTO;

public class CampaignTargetCVRDto extends BusinessKeyDTO {
    
    private static final long serialVersionUID = 1L;

    @Source(value = "id")
    private long bidType;

    @Source(value = "targetCVR")
    private BigDecimal targetCVR;

    public long getBidType() {
        return bidType;
    }

    public void setBidType(long bidType) {
        this.bidType = bidType;
    }

    public BigDecimal getTargetCVR() {
        return targetCVR;
    }

    public void setTargetCVR(BigDecimal targetCVR) {
        this.targetCVR = targetCVR;
    }

}