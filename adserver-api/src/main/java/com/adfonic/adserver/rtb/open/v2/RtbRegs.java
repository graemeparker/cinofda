package com.adfonic.adserver.rtb.open.v2;

/**
 * 
 * @author mvanek
 *
 */
public class RtbRegs {

    /**
     * Flag indicating if this request is subject to the COPPA
     * regulations established by the USA FTC, where 0 = no, 1 = yes.
     */
    private Integer coppa;

    public Integer getCoppa() {
        return coppa;
    }

    public void setCoppa(Integer coppa) {
        this.coppa = coppa;
    }

}
