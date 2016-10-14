package com.adfonic.domain.cache.dto.adserver.creative;

import java.util.HashMap;
import java.util.Map;

import com.adfonic.domain.cache.dto.BusinessKeyDto;

public class AdvertiserDto extends BusinessKeyDto {
    private static final long serialVersionUID = 3L;

    private String externalID; // need this for Ad-X URLs
    private CompanyDto company;
    private BidSeatDto pmpBidSeat;
    
    // MAD-3168 - Enable/disable rtb seats id
    private boolean enableRtbBidSeat;
    
    // MAD-3168 - Map whose key is the publisher id and the value the seatId 
    private Map<Long, String> rtbBidSeats = new HashMap<Long, String>();

    public String getExternalID() {
        return externalID;
    }

    public void setExternalID(String externalID) {
        this.externalID = externalID;
    }

    public CompanyDto getCompany() {
        return company;
    }

    public void setCompany(CompanyDto company) {
        this.company = company;
    }

    public BidSeatDto getPmpBidSeat() {
        return pmpBidSeat;
    }

    public void setPmpBidSeat(BidSeatDto pmpBidSeat) {
        this.pmpBidSeat = pmpBidSeat;
    }
    
    public boolean isEnableRtbBidSeat() {
        return enableRtbBidSeat;
    }

    public void setEnableRtbBidSeat(boolean enableRtbBidSeat) {
        this.enableRtbBidSeat = enableRtbBidSeat;
    }
    
    public Map<Long, String> getRtbBidSeats() {
        return rtbBidSeats;
    }

    @Override
    public String toString() {
        return "AdvertiserDto {" + getId() + ", externalID=" + externalID + ", company=" + company + ", pmpBidSeat=" + pmpBidSeat + ", enableRtbBidSeat=" + enableRtbBidSeat + ", #rtbBidSeats=" + rtbBidSeats.size() + "}";
    }

}
