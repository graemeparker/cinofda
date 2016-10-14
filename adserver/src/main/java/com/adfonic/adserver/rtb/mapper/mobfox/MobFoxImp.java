package com.adfonic.adserver.rtb.mapper.mobfox;

import com.adfonic.adserver.rtb.open.v2.Imp;

/**
 * 
 */
public class MobFoxImp extends Imp {

    private MobFoxImpExt ext;

    public MobFoxImpExt getExt() {
        return ext;
    }

    public void setExt(MobFoxImpExt ext) {
        this.ext = ext;
    }

    /**
     * http://www.mobfox.com/dsp-resource-center/
     */
    public static class MobFoxImpExt {

        private Integer strictbannersize;

        private Integer mraid;

        public Integer getMraid() {
            return mraid;
        }

        public void setMraid(Integer mraid) {
            this.mraid = mraid;
        }

        public Integer getStrictbannersize() {
            return strictbannersize;
        }

        public void setStrictbannersize(Integer strictbannersize) {
            this.strictbannersize = strictbannersize;
        }

    }
}
