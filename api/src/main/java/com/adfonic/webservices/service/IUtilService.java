package com.adfonic.webservices.service;

import com.adfonic.domain.Company;
import com.adfonic.webservices.util.DspAccess;

public interface IUtilService {

    IUtilService validatePresence(String name, Object value);

    boolean hasNoPresence(Object value);
    
    DspAccess getEffectiveDspAccess(Company company);
    
}