package com.adfonic.dto.campaign.enums;

import com.adfonic.domain.Campaign.BiddingStrategy;

public enum BiddingStrategyName { 
    // Optimise Media cost strategy
    MEDIA_COST_OPTIMISATION(BiddingStrategy.MEDIA_COST_OPTIMISATION),
    
    // Average Maximum Bid strategy
    AVERAGE_MAXIMUM_BID(BiddingStrategy.AVERAGE_MAXIMUM_BID);
    
    private BiddingStrategy biddingStrategy;

    // Private to stop people making up their own bidding strategies.
    private BiddingStrategyName(BiddingStrategy biddingStrategy) {
        this.biddingStrategy = biddingStrategy;
    }

    public BiddingStrategy getBiddingStrategy() {
        return biddingStrategy;
    }

    /**
     * Helper method. Return enumeration from a String.
     */
    public static BiddingStrategyName fromString(String domainEnumName) {
        if (domainEnumName != null) {
            for (BiddingStrategyName b : BiddingStrategyName.values()) {
                if (domainEnumName.equalsIgnoreCase(b.biddingStrategy.name())) {
                    return b;
                }
            }
        }
        return null;
    }
}
