package com.byyd.middleware.device.filter;

import java.util.ArrayList;
import java.util.List;

import com.adfonic.domain.Country;
import com.byyd.middleware.iface.dao.LikeSpec;

public class OperatorFilter {

    private String name = null;
    private LikeSpec likeSpec = null;
    private boolean caseSensitive = false;
    private boolean mandateQuova = false;
    private Boolean mobileOperator = false;
    private List<Country> countries = null; 
    
    public OperatorFilter(String name, LikeSpec likeSpec, boolean caseSensitive, boolean mandateQuova, Boolean mobileOperator, List<Country> countries) {
        this.name = name;
        this.likeSpec = likeSpec;
        this.caseSensitive = caseSensitive;
        this.mandateQuova = mandateQuova;
        this.mobileOperator = mobileOperator;
        if(countries!=null){
            this.countries = new ArrayList<Country>();
            this.countries.addAll(countries);
        }
    }
    
    public OperatorFilter(boolean mobileOperator) {
        this.mobileOperator = mobileOperator;
    }
    
    public String getName() {
        return name;
    }
    public OperatorFilter setName(String name) {
        this.name = name;
        return this;
    }
    public LikeSpec getLikeSpec() {
        return likeSpec;
    }
    public OperatorFilter setLikeSpec(LikeSpec likeSpec) {
        this.likeSpec = likeSpec;
        return this;
    }
    public boolean isCaseSensitive() {
        return caseSensitive;
    }
    public OperatorFilter setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
        return this;
    }
    
    public OperatorFilter setName(String name, LikeSpec likeSpec, boolean caseSensitive) {
        this.name = name;
        this.likeSpec = likeSpec;
        this.caseSensitive = caseSensitive;
        return this;
    }
    
    public boolean isMandateQuova() {
        return mandateQuova;
    }
    public OperatorFilter setMandateqUOVA(boolean mandateQuova) {
        this.mandateQuova = mandateQuova;
        return this;
    }
    
    public Boolean isMobileOperator() {
        return mobileOperator;
    }
    public OperatorFilter setMobileOperator(boolean mobileOperator) {
        this.mobileOperator = mobileOperator;
        return this;
    }
    
    public List<Country> getCountries() {
        return countries;
    }
    public OperatorFilter setCountries(List<Country> countries) {
        if(countries!=null){
            this.countries = new ArrayList<Country>();
            this.countries.addAll(countries);
        }
        return this;
    }
}
