package com.adfonic.adserver.rtb.open.v2.ext.appnxs;

public class AppNexusBidRequest extends com.adfonic.adserver.rtb.open.v2.BidRequest<AppNexusImp> {

    private RequestExt ext;

    public RequestExt getExt() {
        return ext;
    }

    public void setExt(RequestExt ext) {
        this.ext = ext;
    }

    public static class RequestExt {

        private AppNexusUdi udi;

        private RequestExtAppNexus appnexus;

        public AppNexusUdi getUdi() {
            return udi;
        }

        public void setUdi(AppNexusUdi udi) {
            this.udi = udi;
        }

        public RequestExtAppNexus getAppnexus() {
            return appnexus;
        }

        public void setAppnexus(RequestExtAppNexus appnexus) {
            this.appnexus = appnexus;
        }

    }

    public static class RequestExtAppNexus {

        private Integer seller_member_id;

        public Integer getSeller_member_id() {
            return seller_member_id;
        }

        public void setSeller_member_id(Integer seller_member_id) {
            this.seller_member_id = seller_member_id;
        }

    }

    public static class AppNexusUdi {

        private String idfa;

        public String getIdfa() {
            return idfa;
        }

        public void setIdfa(String idfa) {
            this.idfa = idfa;
        }

    }
}
