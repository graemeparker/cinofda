package com.adfonic.domain.cache.dto.adserver.adspace;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.adfonic.domain.cache.dto.BusinessKeyDto;

public class RateCardDto extends BusinessKeyDto {
    private static final long serialVersionUID = 3L;

    private BigDecimal defaultMinimum;
    private Map<Long, BigDecimal> minimumBidsByCountryId = new HashMap<Long, BigDecimal>();

    public BigDecimal getDefaultMinimum() {
        return defaultMinimum;
    }

    public void setDefaultMinimum(BigDecimal defaultMinimum) {
        this.defaultMinimum = defaultMinimum;
    }

    public Map<Long, BigDecimal> getMinimumBidsByCountryId() {
        return minimumBidsByCountryId;
    }

    public BigDecimal getMinimumBid(Long countryId) {
        BigDecimal minBid = minimumBidsByCountryId.get(countryId);
        return minBid == null ? defaultMinimum : minBid;
    }

    public boolean isRateCardLessThenAmount(BigDecimal payout) {
        if (defaultMinimum != null) {
            if (payout.compareTo(defaultMinimum) >= 0) {
                // The amount is more than the default minimum...yes serve it
                return true;
            }
        }
        //Either Default aret card is null or Default rate card is more then amount but still see 
        //if any country card is less then this amount
        //if at least one country found this campaign is eligible

        if (minimumBidsByCountryId == null || minimumBidsByCountryId.size() == 0) {
            if (defaultMinimum == null) {
                //Default minimum value is null as well as no country specific minimum values
                return true;
            } else {
                //No Country level rate card value and default rate card is more then payout
                // so dont serve
                return false;
            }
        }
        boolean atLeasetOneCountryIsGoodToServe = false;
        for (BigDecimal minimumBid : minimumBidsByCountryId.values()) {
            if (minimumBid != null) {
                if (payout.compareTo(minimumBid) >= 0) {
                    atLeasetOneCountryIsGoodToServe = true;
                    break;
                }
            }
        }
        return atLeasetOneCountryIsGoodToServe;
    }

    @Override
    public String toString() {
        return "RateCardDto {" + getId() + ", defaultMinimum=" + defaultMinimum + ", minimumBidsByCountryId=" + minimumBidsByCountryId + "}";
    }

}
