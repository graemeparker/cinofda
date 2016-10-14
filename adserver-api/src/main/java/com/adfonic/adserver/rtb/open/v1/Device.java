package com.adfonic.adserver.rtb.open.v1;

import org.apache.commons.lang.BooleanUtils;

public class Device {

    private String ip;
    
    private String ua;
    
    private String loc;

    private String dpid;

    private String os;
    
    private String adid;
    
    // Exchange specific stuff below
    private String nex_dmac;

    private String nex_ifaclr;

    private boolean nex_ifatrk;
    
    // Mobclix
    private Integer dnt;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUa() {
        return ua;
    }

    public void setUa(String ua) {
        this.ua = ua;
    }

    public String getLoc() {
        return loc;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }

    public String getDpid() {
        return dpid;
    }

    public void setDpid(String dpid) {
        this.dpid = dpid;
    }

    public String getNex_dmac() {
        return nex_dmac;
    }

    public void setNex_dmac(String nex_dmac) {
        this.nex_dmac = nex_dmac;
    }

    public String getNex_ifaclr() {
        return nex_ifaclr;
    }

    public void setNex_ifaclr(String nex_ifaclr) {
        this.nex_ifaclr = nex_ifaclr;
    }

    public boolean getNex_ifatrk() {
        return nex_ifatrk;
    }

    public void setNex_ifatrk(String nex_ifatrk) {
        this.nex_ifatrk = (nex_ifatrk != null && (nex_ifatrk.equals("1") || BooleanUtils.toBoolean(nex_ifatrk)));
    }

    public Integer getDnt() {
        return dnt;
    }

    public void setDnt(Integer dnt) {
        this.dnt = dnt;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

	public String getAdid() {
		return adid;
	}

	public void setAdid(String adid) {
		this.adid = adid;
	}
    
}
