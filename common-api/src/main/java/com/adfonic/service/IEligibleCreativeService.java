package com.adfonic.service;

import java.util.List;

import javax.jws.WebService;

@WebService
public interface IEligibleCreativeService {

    public List<Long> getEligibleCreativesForAdSpaces(Long publisherId, List<Long> adSpaceIds);


    // Only desired method. But this may not be viable
    public List<Long> getEligibleCreativesForPublication(Long publicationId);

}
