package com.adfonic.adserver.rtb.open.v2.ext.mopub;

import com.adfonic.util.CodedEnum;

public enum MopubCrtype implements CodedEnum<String, MopubCrtype> {

    VAST_20("VAST 2.0"), VAST_30("VAST 3.0"), MRAID_10("MRAID 1.0"), MRAID_20("MRAID 2.0"), IMAGE_AD("Image Ad"), HTML5("HTML5"), JS("JS"), NATIVE("native");

    private final String code;

    private MopubCrtype(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }

}
