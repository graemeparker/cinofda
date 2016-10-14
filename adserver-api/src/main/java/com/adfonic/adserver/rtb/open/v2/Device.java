package com.adfonic.adserver.rtb.open.v2;

/**
 * 3.2.11 Object: Device
 *
 */
public class Device {

    /**
     * Browser user agent string.
     * 
     * string; recommended
     */
    private String ua;

    /**
     * IPv4 address closest to device.
     * 
     * string; recommended
     */
    private String ip;

    /**
     * Location of the device assumed to be the user’s current
     * location defined by a Geo object (Section 3.2.12).
     * 
     * object; recommended
     */
    private Geo geo;

    /**
     * Standard “Do Not Track” flag as set in the header by the
     * browser, where 0 = tracking is unrestricted, 1 = do not track.
     */
    private Integer dnt;

    /**
     * “Limit Ad Tracking” signal commercially endorsed (e.g., iOS,
     * Android), where 0 = tracking is unrestricted, 1 = tracking must
     * be limited per commercial guidelines.
     */
    private Integer lmt;

    /**
     * Hardware device ID (e.g., IMEI); hashed via SHA1.
     */
    private String dpidsha1;

    /**
     * ID sanctioned for advertiser use in the clear (i.e., not hashed)
     * 
     * Exchanges usually put IDFA or ADID here. Examining "os" is necessary to figure out which type it is
     */
    private String ifa;

    /**
     * Device operating system (e.g., “iOS”).
     */
    private String os;

    // Unmaped: ipv6, devicetype, make, model, osv, hwv, h, w, ppi, pxratio, js, flashver, language, carrier, connectiontype, didmd5, dpidsha1, dpidmd5, macsha1, macmd5

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

    public Geo getGeo() {
        return geo;
    }

    public void setGeo(Geo geo) {
        this.geo = geo;
    }

    public String getDpidsha1() {
        return dpidsha1;
    }

    public void setDpidsha1(String dpidsha1) {
        this.dpidsha1 = dpidsha1;
    }

    public String getIfa() {
        return ifa;
    }

    public void setIfa(String ifa) {
        this.ifa = ifa;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public Integer getDnt() {
        return dnt;
    }

    public void setDnt(Integer dnt) {
        this.dnt = dnt;
    }

    public Integer getLmt() {
        return lmt;
    }

    public void setLmt(Integer lmt) {
        this.lmt = lmt;
    }

}
