package com.adfonic.adserver.rtb.mapper.mobfox;

import com.adfonic.adserver.rtb.open.v2.BidRequest;

/**
 * 
 */
public class MobFoxBidRequest extends BidRequest<MobFoxImp> {

    private MobFoxBidRequestExt ext;

    public MobFoxBidRequestExt getExt() {
        return ext;
    }

    public void setExt(MobFoxBidRequestExt ext) {
        this.ext = ext;
    }

    public static class MobFoxBidRequestExt {

        private MobFoxUdi udi;

        public MobFoxUdi getUdi() {
            return udi;
        }

        public void setUdi(MobFoxUdi udi) {
            this.udi = udi;
        }

    }

    /**
     * http://www.mobfox.com/dsp-resource-center/
     */
    public static class MobFoxUdi {

        private String idfa;

        private String gaid;

        private String androidid;

        public String getIdfa() {
            return idfa;
        }

        public void setIdfa(String idfa) {
            this.idfa = idfa;
        }

        public String getGaid() {
            return gaid;
        }

        public void setGaid(String gaid) {
            this.gaid = gaid;
        }

        public String getAndroidid() {
            return androidid;
        }

        public void setAndroidid(String androidid) {
            this.androidid = androidid;
        }

    }
}
