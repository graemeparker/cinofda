package com.adfonic.adserver.rtb.smaato;

/**
 * 
 * http://dspportal.smaato.com/documentation
 *
 */
public class SmaatoUdi {

    private String openudid; //ext.udi.openudid
    private String odin; //ext.udi.odin
    private String idfa; //ext.udi.idfa
    private String idfasha1; //ext.udi.idfasha1
    private String idfamd5; //ext.udi.idfamd5
    private int idfatracking = 1; //ext.udi.idfatracking
    private String googleadid; //ext.udi.googleadid
    private int googlednt = 0; //ext.udi.googlednt
    private String atuid; // 

    public String getOpenudid() {
        return openudid;
    }

    public void setOpenudid(String openudid) {
        this.openudid = openudid;
    }

    public String getOdin() {
        return odin;
    }

    public void setOdin(String odin) {
        this.odin = odin;
    }

    public String getIdfa() {
        return idfa;
    }

    public void setIdfa(String idfa) {
        this.idfa = idfa;
    }

    public String getIdfasha1() {
        return idfasha1;
    }

    public void setIdfasha1(String idfasha1) {
        this.idfasha1 = idfasha1;
    }

    public String getIdfamd5() {
        return idfamd5;
    }

    public void setIdfamd5(String idfamd5) {
        this.idfamd5 = idfamd5;
    }

    public int getIdfatracking() {
        return idfatracking;
    }

    public void setIdfatracking(int idfatracking) {
        this.idfatracking = idfatracking;
    }

    public String getGoogleadid() {
        return googleadid;
    }

    public void setGoogleadid(String googleadid) {
        this.googleadid = googleadid;
    }

    public int getGooglednt() {
        return googlednt;
    }

    public void setGooglednt(int googlednt) {
        this.googlednt = googlednt;
    }

    public String getAtuid() {
        return atuid;
    }

    public void setAtuid(String atuid) {
        this.atuid = atuid;
    }

    public boolean trackIdfa() {
        return this.idfatracking == 1 ? true : false;
    }

    public boolean trackAdid() {
        return this.googlednt == 1 ? false : true;
    }
}
