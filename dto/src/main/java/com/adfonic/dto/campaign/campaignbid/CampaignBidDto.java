package com.adfonic.dto.campaign.campaignbid;

import java.math.BigDecimal;
import java.util.Date;

import org.jdto.annotation.Source;

import com.adfonic.dto.NameIdBusinessDto;
import com.adfonic.dto.campaign.enums.BidType;

public class CampaignBidDto extends NameIdBusinessDto {
    
    private static final long serialVersionUID = 1L;

    @Source(value = "BidType.name")
    private String bidType = BidType.CPC.getBidType();

    private String bidTypeStr;

    @Source(value = "amount")
    private BigDecimal amount;

    @Source(value = "startDate")
    private Date startDate;

    @Source(value = "endDate")
    private Date endDate;

    @Source(value = "maximum")
    private boolean maximum;

    public String getBidType() {
        return bidType;
    }

    public void setBidType(String bidType) {
        this.bidType = bidType;
    }

    public BigDecimal getAmount() {

        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = (startDate == null ? null : new Date(startDate.getTime()));
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = (endDate == null ? null : new Date(endDate.getTime()));
    }

    public String getBidTypeStr() {
        if (bidType != null && !"".equals(bidType)) {
            bidTypeStr = bidType;
        }
        return bidTypeStr;
    }

    public void setBidTypeStr(String bidTypeStr) {
        this.bidTypeStr = bidTypeStr;
    }

    public boolean isMaximum() {
        return maximum;
    }

    public void setMaximum(boolean maximum) {
        this.maximum = maximum;
    }

}
