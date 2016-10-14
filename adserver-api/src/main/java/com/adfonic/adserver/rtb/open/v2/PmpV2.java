package com.adfonic.adserver.rtb.open.v2;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.adfonic.adserver.Constant;

/**
 * OpenRTB-API-Specification
 * 3.2.17 Object: Pmp
 * This object is the private marketplace container for direct deals between buyers and sellers that may pertain to this impression. 
 * The actual deals are represented as a collection of Deal objects. 
 * Refer to Section 7.2 for more details.
 *
 */
public class PmpV2 {

    /**
     * Indicator of auction eligibility to seats named in the Direct Deals object, 
     * where 0 = all bids are accepted, 1 = bids are restricted to the deals specified and the terms thereof.
     */
    private Integer private_auction;

    private List<DealV2> deals = new ArrayList<PmpV2.DealV2>();

    public List<DealV2> getDeals() {
        return deals;
    }

    public void setDeals(List<DealV2> deals) {
        this.deals = deals;
    }

    /**
     * Defaulting to public (non private) if not specified (OpenRTB does not say)
     */
    public boolean isPrivate() {
        return Constant.ONE.equals(private_auction);
    }

    public Integer getPrivate_auction() {
        return private_auction;
    }

    public void setPrivate_auction(Integer private_auction) {
        this.private_auction = private_auction;
    }

    /**
     * 3.2.18 Object: Deal
     * This object constitutes a specific deal that was struck a priori between a buyer and a seller. 
     * Its presence with the Pmp collection indicates that this impression is available under the terms of that deal.
     *
     */
    public static class DealV2 {

        /**
         * A unique identifier for the direct deal.
         * 
         * string; required
         */
        private String id;

        /**
         * Minimum bid for this impression expressed in CPM.
         * 
         * float; default 0
         */
        private BigDecimal bidfloor;

        /**
         * Currency specified using ISO-4217 alpha codes. 
         * This may be different from bid currency returned by bidder if this is allowed by the exchange.
         * 
         * string; default ”USD”
         */
        private String bidfloorcur;

        /**
         * Optional override of the overall auction type of the bid request, 
         * where 1 = First Price, 2 = Second Price Plus, 3 = the value passed in bidfloor is the agreed upon deal price.
         * Additional auction types can be defined by the exchange.
         */
        private Integer at;

        /**
         * Whitelist of buyer seats allowed to bid on this deal. 
         * Seat IDs must be communicated between bidders and the exchange a priori. Omission implies no seat restrictions.
         */
        private List<String> wseat;

        /**
         * Array of advertiser domains (e.g., advertiser.com) allowed to bid on this deal. Omission implies no advertiser restrictions.
         */
        private List<String> wadomain;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public BigDecimal getBidfloor() {
            return bidfloor;
        }

        public void setBidfloor(BigDecimal bidfloor) {
            this.bidfloor = bidfloor;
        }

        public String getBidfloorcur() {
            return bidfloorcur;
        }

        public void setBidfloorcur(String bidfloorcur) {
            this.bidfloorcur = bidfloorcur;
        }

        public Integer getAt() {
            return at;
        }

        public void setAt(Integer at) {
            this.at = at;
        }

        public List<String> getWseat() {
            return wseat;
        }

        public void setWseat(List<String> wseat) {
            this.wseat = wseat;
        }

        public List<String> getWadomain() {
            return wadomain;
        }

        public void setWadomain(List<String> wadomain) {
            this.wadomain = wadomain;
        }

    }
}
