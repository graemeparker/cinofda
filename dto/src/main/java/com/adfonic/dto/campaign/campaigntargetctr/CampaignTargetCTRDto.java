package com.adfonic.dto.campaign.campaigntargetctr;

import java.math.BigDecimal;

import org.jdto.annotation.Source;

import com.adfonic.dto.BusinessKeyDTO;

public class CampaignTargetCTRDto extends BusinessKeyDTO {

    private static final long serialVersionUID = 1L;

    @Source(value = "id")
    private long bidType;

    @Source(value = "targetCTR")
    private BigDecimal targetCTR;

    public long getBidType() {
        return bidType;
    }

    public void setBidType(long bidType) {
        this.bidType = bidType;
    }

    public BigDecimal getTargetCTR() {
        return targetCTR;
    }

    public void setTargetCTR(BigDecimal targetCTR) {
        this.targetCTR = targetCTR;
    }

}
