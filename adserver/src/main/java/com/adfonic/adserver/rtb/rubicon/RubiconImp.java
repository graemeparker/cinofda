package com.adfonic.adserver.rtb.rubicon;

public class RubiconImp extends com.adfonic.adserver.rtb.open.v2.Imp {

    private RubiconBanner banner;

    private RubiconVideo video;

    @Override
    public RubiconBanner getBanner() {
        return banner;
    }

    public void setBanner(RubiconBanner banner) {
        this.banner = banner;
    }

    @Override
    public RubiconVideo getVideo() {
        return video;
    }

    public void setVideo(RubiconVideo video) {
        this.video = video;
    }

}
