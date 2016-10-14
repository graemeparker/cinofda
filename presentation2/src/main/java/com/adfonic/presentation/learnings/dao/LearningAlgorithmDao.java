package com.adfonic.presentation.learnings.dao;

import java.util.List;

import com.adfonic.domain.AdSpace;
import com.adfonic.domain.Creative;


public interface LearningAlgorithmDao {
    
    // ------------------
    // Campaign learnings
    // ------------------
    void includeCampaignToLearningAlgorithm(Long campaignId, Long adfonicUserId);
    void excludeCampaignFromLearningAlgorithm(Long campaignId, Long adfonicUserId);
    void removeCampaignLearnings(Long campaignId, Long adfonicUserId);
    Boolean isCampaignAddedToLearningAlgorithm(List<Creative> creatives);
    
    // ---------------------
    // Publication learnings
    // ---------------------
    void includeAdSpaceToLearningAlgorithm(Long publicationId, Long adfonicUserId);
    void excludeAdSpaceFromLearningAlgorithm(Long publicationId, Long adfonicUserId);
    void removeAdSpaceLearnings(Long publicationId, Long adfonicUserId);
    Boolean isPublicationAddedToLearningAlgorithm(List<AdSpace> adSpaces);
    
}
