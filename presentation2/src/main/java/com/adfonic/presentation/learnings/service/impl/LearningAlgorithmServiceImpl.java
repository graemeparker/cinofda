package com.adfonic.presentation.learnings.service.impl;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.AdSpace;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Publication;
import com.adfonic.presentation.learnings.dao.LearningAlgorithmDao;
import com.adfonic.presentation.learnings.service.LearningAlgorithmService;
import com.adfonic.presentation.util.GenericServiceImpl;
import com.byyd.middleware.campaign.service.CampaignManager;
import com.byyd.middleware.publication.service.PublicationManager;

@Service("learningAlgorithmService")
public class LearningAlgorithmServiceImpl extends GenericServiceImpl implements LearningAlgorithmService {
    
    private static final Logger LOG = Logger.getLogger(LearningAlgorithmServiceImpl.class.getName());
    
    @Autowired
    private CampaignManager campaignManager;
    

    @Autowired
    private PublicationManager publicationManager;
    
    @Autowired
    private LearningAlgorithmDao learningAlgorithmDao;
    
    // ------------------
    // Campaign learnings
    // ------------------
    
    @Transactional(readOnly=false)
    public void includeCampaignToLearningAlgorithm(Long campaignId, Long adfonicUserId){
        Campaign campaign = campaignManager.getCampaignById(campaignId);
        if (campaign == null){
            LOG.warning("Campaign not found with id = " + campaignId);
        }else{
            learningAlgorithmDao.includeCampaignToLearningAlgorithm(campaignId, adfonicUserId);
        }
    }
    
    @Transactional(readOnly=false)
    public void excludeCampaignFromLearningAlgorithm(Long campaignId, Long adfonicUserId){
        Campaign campaign = campaignManager.getCampaignById(campaignId);
        if (campaign == null){
            LOG.warning("Campaign not found with id = " + campaignId);
        }else{
            learningAlgorithmDao.excludeCampaignFromLearningAlgorithm(campaignId, adfonicUserId);
        }
    }
    
    @Transactional(readOnly=false)
    public void removeCampaignLearnings(Long campaignId, Long adfonicUserId){
        Campaign campaign = campaignManager.getCampaignById(campaignId);
        if (campaign == null){
            LOG.warning("Campaign not found with id = " + campaignId);
        }else{
            learningAlgorithmDao.removeCampaignLearnings(campaignId, adfonicUserId);
        }
    }
    
    @Transactional(readOnly=true)
    public Boolean isCampaignAddedToLearningAlgorithm(Long campaignId){
        Boolean result = false;
        Campaign campaign = campaignManager.getCampaignById(campaignId);
        if (campaign == null){
            LOG.warning("Campaign not found with id = " + campaignId);
        }else{
            result = learningAlgorithmDao.isCampaignAddedToLearningAlgorithm(campaign.getCreatives());
        }
        
        return result;
    }
    
    // ---------------------
    // Publication learnings
    // ---------------------
    
    @Transactional(readOnly=false)
    public void includePublicationToLearningAlgorithm(Long publicationId, Long adfonicUserId){
        Publication publication = publicationManager.getPublicationById(publicationId);
        if (publication == null){
            LOG.warning("Publication not found with id = " + publicationId);
        }else{
            for (AdSpace adSpace : publication.getAdSpaces()){
                learningAlgorithmDao.includeAdSpaceToLearningAlgorithm(adSpace.getId(), adfonicUserId);
            }
        }
    }
    
    @Transactional(readOnly=false)
    public void excludePublicationFromLearningAlgorithm(Long publicationId, Long adfonicUserId){
        Publication publication = publicationManager.getPublicationById(publicationId);
        if (publication == null){
            LOG.warning("Publication not found with id = " + publicationId);
        }else{
            for (AdSpace adSpace : publication.getAdSpaces()){
                learningAlgorithmDao.excludeAdSpaceFromLearningAlgorithm(adSpace.getId(), adfonicUserId);
            }
        }
    }
    
    @Transactional(readOnly=false)
    public void removePublicationLearnings(Long publicationId, Long adfonicUserId){
        Publication publication = publicationManager.getPublicationById(publicationId);
        if (publication == null){
            LOG.warning("Publication not found with id = " + publicationId);
        }else{
            for (AdSpace adSpace : publication.getAdSpaces()){
                learningAlgorithmDao.removeAdSpaceLearnings(adSpace.getId(), adfonicUserId);
            }
        }
    }
    
    @Transactional(readOnly=true)
    public Boolean isPublicationAddedToLearningAlgorithm(Long publicationId){
        Boolean result = false;
        Publication publication = publicationManager.getPublicationById(publicationId);
        if (publication == null){
            LOG.warning("Publication not found with id = " + publicationId);
        }else{
            result = learningAlgorithmDao.isPublicationAddedToLearningAlgorithm(publication.getAdSpaces());
        }
        
        return result;
    }
}

