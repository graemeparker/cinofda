package com.adfonic.sso.beans;

import java.io.Serializable;

public class HearAboutPlace implements Serializable{
    
    private static final long serialVersionUID = 2404200316126725710L;
    
    private String value;
    private String label;
    
    public HearAboutPlace(){
        super();
    }
    
    public HearAboutPlace(String value, String label) {
        super();
        this.value = value;
        this.label = label;
    }
    
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }
}
