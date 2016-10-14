package com.adfonic.adserver.rtb.rubicon;

import java.math.BigDecimal;
import java.util.List;

/**
 * 
 * http://kb.rubiconproject.com/index.php/RTB/Notifications
 *
 */
public class RubiconRtbNotification {

    private List<NotificationImpression> impressions;

    public List<NotificationImpression> getImpressions() {
        return impressions;
    }

    public void setImpressions(List<NotificationImpression> impressions) {
        this.impressions = impressions;
    }

    public static class NotificationImpression {

        private String id;// Unique auction identifier.

        private Integer time;// Unix timestamp for the impression.

        private String result; // Status code the auction participant. A value of "win" indicates a served impression.

        private String detail; // Detailed status code indicating the cause of a block, loss or error. See Table below.

        private BigDecimal price; // Charge price for the impression (USD CPM).

        private String ip; // Source IP address of the end device.

        private String uid; // Rubicon-assigned user id.

        private String creative; // Unique identifier for the ad creative as provided in a bid response.

        private String token; // Arbitrary token provided in a bid response. This may (optionally) be included in OpenRTB as seatbid.bid.ext.nt, or in the Rubicon protocol as "nt". If provided in a bid response, it is included here but otherwise ignored.

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Integer getTime() {
            return time;
        }

        public void setTime(Integer time) {
            this.time = time;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        public String getDetail() {
            return detail;
        }

        public void setDetail(String detail) {
            this.detail = detail;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getCreative() {
            return creative;
        }

        public void setCreative(String creative) {
            this.creative = creative;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

    }
}
