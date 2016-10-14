package com.adfonic.adserver.rtb.rubicon;

import com.adfonic.adserver.rtb.open.v2.VideoV2;

/**
 * http://kb.rubiconproject.com/index.php/RTB/VideoRTBBestPractices
 * http://kb.rubiconproject.com/index.php/RTB/OpenRTB#Video_Object
 */
public class RubiconVideo extends VideoV2 {

    private RubiconVideoExt ext;

    public RubiconVideoExt getExt() {
        return ext;
    }

    public void setExt(RubiconVideoExt ext) {
        this.ext = ext;
    }

    public static class RubiconVideoExt {
        /**
         * Indicates whether the user can skip the ad. 0= no 1 = yes, skipping is possible. Default is 0
         */
        private Integer skip;

        /**
         * Duration (in seconds) after which the user has the option to skip the ad. Default is 0
         */
        private Integer skipDelay;

        public Integer getSkip() {
            return skip;
        }

        public void setSkip(Integer skip) {
            this.skip = skip;
        }

        public Integer getSkipDelay() {
            return skipDelay;
        }

        public void setSkipDelay(Integer skipDelay) {
            this.skipDelay = skipDelay;
        }

    }
}
