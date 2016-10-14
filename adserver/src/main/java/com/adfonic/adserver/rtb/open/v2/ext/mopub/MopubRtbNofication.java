package com.adfonic.adserver.rtb.open.v2.ext.mopub;

import java.util.List;

/**
 * https://dev.twitter.com/mopub-demand/marketplace/auction-notifications
 * 
 * The auction notification response is structured like a typical bid response. 
 * For more information on individual fields from the bid response structure, please refer to the MoPub OpenRTB 2.1 spec.
 * 
 * {"bidfloor":19.610,"http_status":599,"id":"9d68ae8e-5823-4ca1-a4c1-151a57dd7b88","latency":0.0010,"reason":{"description":"timeout","id":8}}
 * 
 * {"bidfloor":0.20,"bidid":"10b62412-340c-d291-eb21-fd047fae135e","http_status":200,"id":"e0a46785-4e2d-4f40-82cb-eca2135a9a42","latency":0.0430,"seatbid":[{"bid":[{"adid":"10b62412-340c-d291-eb21-fd047fae135e","id":"111673","impid":"1","price":0.270,"reason":{"description":"outbid","id":1,"value":"bidder"}}]}]}
 * 
 * {"bidfloor":0.080,"bidid":"6a7f03ec-23ce-b452-0b22-239f1796da39","http_status":200,"id":"e7a45863-cc86-4270-88ed-d67c590406e5","latency":0.0210,"seatbid":[{"bid":[{"adid":"6a7f03ec-23ce-b452-0b22-239f1796da39","id":"117712","impid":"1","price":5.250,"reason":{"description":"outbid","id":1,"value":"network"}}],"seat":"6dBl4I1ltxY0KJ4IrIB4wLQLEw3caLWgZyB3hBoK"}]}
 * 
 * {"bidfloor":0.40,"bidid":"5e7c5c36-d3a4-31fd-99f8-9d773b643656","http_status":200,"id":"192e6cf9-d6df-43d8-b5ce-bb5acc4a6648","latency":0.0330,"seatbid":[{"bid":[{"adid":"5e7c5c36-d3a4-31fd-99f8-9d773b643656","id":"118885","impid":"1","price":2.10,"reason":{"description":"blocked_category","id":15,"value":"IAB19-35"}}],"seat":"6dBl4I1ltxY0KJ4IrIB4wLQLEw3caLWgZyB3hBoK"}]}
 */
public class MopubRtbNofication {

    /**
     * Top­level ID of the bid request that this is a response for.
     */
    private String id;

    /**
     * Our pairing id. We pass impression external id in this field in our bid responses 
     * 
     * Bid response ID to assist tracking for bidders. This value is chosen by the bidder for cross­reference.
     */
    private String bidid;

    private Integer http_status;

    private Float latency;

    private Float bidfloor;

    /**
     * On errors like malformed bid response or timeout
     */
    private Reason reason;

    private List<Seatbid> seatbid;

    /**
     * Reason can be found on 2 different places
     */
    public Reason getBestReason() {
        if (reason != null) {
            return reason;
        } else {
            return seatbid.get(0).getBid().get(0).getReason();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBidid() {
        return bidid;
    }

    public void setBidid(String bidid) {
        this.bidid = bidid;
    }

    public Integer getHttp_status() {
        return http_status;
    }

    public void setHttp_status(Integer http_status) {
        this.http_status = http_status;
    }

    public Float getLatency() {
        return latency;
    }

    public void setLatency(Float latency) {
        this.latency = latency;
    }

    public Float getBidfloor() {
        return bidfloor;
    }

    public void setBidfloor(Float bidfloor) {
        this.bidfloor = bidfloor;
    }

    public Reason getReason() {
        return reason;
    }

    public void setReason(Reason reason) {
        this.reason = reason;
    }

    public List<Seatbid> getSeatbid() {
        return seatbid;
    }

    public void setSeatbid(List<Seatbid> seatbid) {
        this.seatbid = seatbid;
    }

    public static class Seatbid {

        private List<Bid> bid;

        public List<Bid> getBid() {
            return bid;
        }

        public void setBid(List<Bid> bid) {
            this.bid = bid;
        }

    }

    public static class Bid {

        /**
         * Our pairing id. We pass impression external id in this field in our bid responses 
         * 
         * ID that references the ad to be served if the bid wins. Logged if passed.
         */
        private String adid;

        /**
         * On Marketplace Loss
         */
        private Reason reason;

        private Float price;

        public String getAdid() {
            return adid;
        }

        public void setAdid(String adid) {
            this.adid = adid;
        }

        public Reason getReason() {
            return reason;
        }

        public void setReason(Reason reason) {
            this.reason = reason;
        }

        public Float getPrice() {
            return price;
        }

        public void setPrice(Float price) {
            this.price = price;
        }

    }

    public static class Reason {
        /**
         * string describing the reason for the notification
         */
        private String description;
        /**
         * optional field if theres additional detail on the reason to supply
         */
        private String value;
        /**
         * reason id from reason code mapping table
         */
        private Integer id;

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

    }

}
