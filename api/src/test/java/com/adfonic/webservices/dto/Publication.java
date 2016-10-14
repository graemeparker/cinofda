package com.adfonic.webservices.dto;

public class Publication extends BaseTO {
    public String id;
    public String languages;
    public String status;
    // inherit the error desc from base class. Deserializer double sets it
    // public String description;
    public String name;
    public boolean autoapprove;
    public boolean transparent;
    public String type;
    public String url;
    public String reference;
    public int requests;
    public int uniques;

}
