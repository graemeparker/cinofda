package com.adfonic.presentation.learnings.service;


public interface LearningAlgorithmService {

    // ------------------
    // Campaign learnings
    // ------------------
    void includeCampaignToLearningAlgorithm(Long campaignId, Long adfonicUserId);
    void excludeCampaignFromLearningAlgorithm(Long campaignId, Long adfonicUserId);
    void removeCampaignLearnings(Long campaignId, Long adfonicUserId);
    Boolean isCampaignAddedToLearningAlgorithm(Long campaignId);
    
    // ---------------------
    // Publication learnings
    // ---------------------
    void includePublicationToLearningAlgorithm(Long publicationId, Long adfonicUserId);
    void excludePublicationFromLearningAlgorithm(Long publicationId, Long adfonicUserId);
    void removePublicationLearnings(Long publicationId, Long adfonicUserId);
    Boolean isPublicationAddedToLearningAlgorithm(Long publicationId);
}
