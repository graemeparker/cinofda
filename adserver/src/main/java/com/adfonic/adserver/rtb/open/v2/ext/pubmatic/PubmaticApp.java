package com.adfonic.adserver.rtb.open.v2.ext.pubmatic;

import com.adfonic.adserver.rtb.open.v2.App;

public class PubmaticApp extends App {

    private PubmaticAppExt ext;

    public PubmaticAppExt getExt() {
        return ext;
    }

    public void setExt(PubmaticAppExt ext) {
        this.ext = ext;
    }

    public static class PubmaticAppExt {
        private String pmid;

        public String getPmid() {
            return pmid;
        }

        public void setPmid(String pmid) {
            this.pmid = pmid;
        }

    }
}
