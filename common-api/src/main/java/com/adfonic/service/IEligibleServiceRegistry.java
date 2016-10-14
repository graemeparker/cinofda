package com.adfonic.service;

import java.util.List;

import javax.jws.WebService;


@WebService
public interface IEligibleServiceRegistry {

    public void registerECService(boolean include, List<Long> publisherIds, String ecServiceEndpoint);


    public String lookupECService(Long publisherId);
}
