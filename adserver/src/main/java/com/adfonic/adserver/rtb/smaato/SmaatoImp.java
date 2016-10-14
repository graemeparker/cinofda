package com.adfonic.adserver.rtb.smaato;

import com.adfonic.adserver.rtb.open.v2.Imp;
import com.adfonic.adserver.rtb.open.v2.ImpExtension;

public class SmaatoImp extends Imp {

    private SmaatoImpExtension ext;

    public SmaatoImpExtension getExt() {
        return ext;
    }

    public void setExt(SmaatoImpExtension ext) {
        this.ext = ext;
    }

    public static class SmaatoImpExtension implements ImpExtension {

        private boolean strictbannersize;

        public boolean isStrictbannersize() {
            return strictbannersize;
        }

        public void setStrictbannersize(boolean strictbannersize) {
            this.strictbannersize = strictbannersize;
        }
    }
}
