package com.adfonic.adserver.rtb.smaato;

import java.util.List;

public class SmaatoBidRequest extends com.adfonic.adserver.rtb.open.v2.BidRequest<SmaatoImp> {

    private List<SmaatoImp> imp;

    private Extension ext;

    @Override
    public List<SmaatoImp> getImp() {
        return imp;
    }

    @Override
    public void setImp(List<SmaatoImp> imp) {
        this.imp = imp;
    }

    public Extension getExt() {
        return ext;
    }

    public void setExt(Extension ext) {
        this.ext = ext;
    }

    public static class Extension {

        private SmaatoUdi udi;

        public SmaatoUdi getUdi() {
            return udi;
        }

        public void setUdi(SmaatoUdi udi) {
            this.udi = udi;
        }
    }
}
