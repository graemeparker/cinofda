package com.adfonic.adserver.rtb.open.v2.ext.mopub;

/**
 * https://docs.google.com/document/d/1DkoL7L9GdQQRt4RDUR0wwCp4eSIqIDWqGowJDzhVFVg/view#
 *
 */
public class MopubImp extends com.adfonic.adserver.rtb.open.v2.Imp {

    private MopubImpExt ext;

    public MopubImpExt getExt() {
        return ext;
    }

    public void setExt(MopubImpExt ext) {
        this.ext = ext;
    }

    /**
     * https://dl.dropboxusercontent.com/s/0czvysdlai63lpz/MoPub%20OpenRTB%202.3.html?dl=0
     * 2.6 Click tracking extensions (Native browser clicks & Deep Link+)
     */
    public static class MopubImpExt {

        private Integer brsrclk; // 2.6.1 Native Browser Clicks

        private Integer dlp; // 2.6.2 Deep Link+

        public Integer getBrsrclk() {
            return brsrclk;
        }

        public void setBrsrclk(Integer brsrclk) {
            this.brsrclk = brsrclk;
        }

        public Integer getDlp() {
            return dlp;
        }

        public void setDlp(Integer dlp) {
            this.dlp = dlp;
        }
    }

}
