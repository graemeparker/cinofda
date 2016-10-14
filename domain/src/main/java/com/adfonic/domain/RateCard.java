package com.adfonic.domain;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.*;

/**
 * A rate card represents bid minimums on a per-country basis.
 * See also TransparentNetwork.rateCardMap
 */
@Entity
@Table(name="RATE_CARD")
public class RateCard extends BusinessKey {
    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue @Column(name="ID")
    private long id;
    
    /**
     * Optional default.  If null, bidding should only be allowed
     * on the countries in the map.
     */ 
    @Column(name="DEFAULT_MINIMUM",nullable=true)
    private BigDecimal defaultMinimum;

    @ElementCollection(fetch=FetchType.LAZY,targetClass=BigDecimal.class)
    @CollectionTable(name="RATE_CARD_MINIMUM_BID_MAP",joinColumns=@JoinColumn(name="RATE_CARD_ID",referencedColumnName="ID"))
    @MapKeyJoinColumn(name="COUNTRY_ID",referencedColumnName="ID")
    @Column(name="AMOUNT",nullable=false)
    private Map<Country,BigDecimal> minimumBidMap;

    public RateCard() {
	minimumBidMap = new HashMap<Country, BigDecimal>();
    }

    public long getId() { return id; };

    public BigDecimal getDefaultMinimum() {
	return defaultMinimum;
    }

    public void setDefaultMinimum(BigDecimal defaultMinimum) {
	this.defaultMinimum = defaultMinimum;
    }

    public BigDecimal getMinimumBid(Country country) {
	if (minimumBidMap.containsKey(country)) {
	    return minimumBidMap.get(country);
	}
	return defaultMinimum;
    }

    public void setMinimumBid(Country country, BigDecimal minimumBid) {
	minimumBidMap.put(country, minimumBid);
    }

    /** Raw access to the minimum bid map. */
    public Map<Country,BigDecimal> getMinimumBidMap() {
	return minimumBidMap;
    }
}
