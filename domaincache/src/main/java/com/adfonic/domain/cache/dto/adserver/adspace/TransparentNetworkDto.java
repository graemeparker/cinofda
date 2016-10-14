package com.adfonic.domain.cache.dto.adserver.adspace;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.adfonic.domain.BidType;
import com.adfonic.domain.cache.dto.BusinessKeyDto;

public class TransparentNetworkDto extends BusinessKeyDto {
    private static final long serialVersionUID = 3L;

    private boolean closed;
    private Set<Long> advertiserCompanyIds = new HashSet<Long>();
    private Map<BidType, RateCardDto> rateCardMap = new HashMap<BidType, RateCardDto>();

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public Set<Long> getAdvertiserCompanyIds() {
        return advertiserCompanyIds;
    }

    public Map<BidType, RateCardDto> getRateCardMap() {
        return rateCardMap;
    }

    public RateCardDto getRateCard(BidType bidType) {
        return rateCardMap.get(bidType);
    }

    public boolean isDefaultRateCard() {
        return rateCardMap.isEmpty();
    }

    @Override
    public String toString() {
        return "TransparentNetworkDto {" + getId() + " closed=" + closed + ", advertiserCompanyIds=" + advertiserCompanyIds + ", rateCardMap=" + rateCardMap + "}";
    }

}
