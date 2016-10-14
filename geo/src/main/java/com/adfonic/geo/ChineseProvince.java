package com.adfonic.geo;

public enum ChineseProvince implements Coordinates {
    BJ("beijing shi"),
    TJ("tianjin shi"),
    HE("hebei"),
    SX("shaanxi"),
    NM("nei mongol"),
    LN("liaoning"),
    JL("jilin"),
    HL("heilongjiang"),
    SH("shanghai shi"),
    JS("jiangsu"),
    ZJ("zhejiang"),
    AH("anhui"),
    FJ("fujian"),
    JX("jiangxi"),
    SD("shandong"),
    HA("henan"),
    HB("hubei"),
    HN("hunan"),
    GD("guangdong"),
    GX("guangxi"),
    HI("hainan"),
    CQ("chongqing"),
    SC("sichuan"),
    GZ("guizhou"),
    YN("yunnan"),
    XZ("xizang"),
    SN("shanxi"),
    GS("gansu"),
    QH("qinghai"),
    NX("ningxia"),
    XJ("xinjiang");

    private final String name;
    private final String capital;
    private final double latitude;
    private final double longitude;

    private ChineseProvince(String name) {
        this.name = name;
        this.capital = "";
        this.latitude = 0;
        this.longitude = 0;
    }

    public String getName() {
        return name;
    }

    @Override
    public double getLatitude() {
        return latitude;
    }

    public String getCapital() {
        return capital;
    }
    
    @Override
    public double getLongitude() {
        return longitude;
    }
}
