package com.adfonic.adserver.rtb.rubicon;

import java.util.Arrays;

import com.adfonic.adserver.rtb.rubicon.RubiconBidReponse.RubiconSeatBid;

/**
 * http://kb.rubiconproject.com/index.php/RTB/OpenRTB#Extensions
 * 
 * @author mvanek
 *
 */
public class RubiconBidReponse extends com.adfonic.adserver.rtb.open.v1.BidResponse<RubiconSeatBid> {

    public RubiconBidReponse() {
        setSeatbid(Arrays.asList(new RubiconSeatBid()));
    }

    public static class RubiconSeatBid extends com.adfonic.adserver.rtb.open.v1.SeatBid<RubiconBid> {

    }

    public static class RubiconBid extends com.adfonic.adserver.rtb.open.v2.Bid {
        private RubiconBidExt ext;

        public RubiconBidExt getExt() {
            return ext;
        }

        public void setExt(RubiconBidExt ext) {
            this.ext = ext;
        }

    }

    public static class RubiconBidExt {

        /**
         * Event notification token. Max length of 512 characters.
         */
        private String nt;

        public String getNt() {
            return nt;
        }

        public void setNt(String nt) {
            this.nt = nt;
        }

    }
}
