package com.adfonic.presentation.audienceengine.service;

import java.util.Set;

import com.adfonic.presentation.audienceengine.exception.AudienceEngineApiException;


public interface AudienceEngineAPIInterface {
    
    /**
     * Notify AudienceEngine that audience has new files assigned
     * 
     * @param fileIds the set of new assigned file ids
     * @throws AudienceEngineApiException in case of connection problem
     */
    public void notifyAssignedFiles(Long firstPartyAudienceId, Set<String> fileIds) throws AudienceEngineApiException;

}
