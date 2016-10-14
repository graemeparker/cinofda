package com.adfonic.presentation.optimisation.service.impl;


import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Service;

import com.adfonic.domain.AdfonicUser;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Company;
import com.adfonic.domain.Creative;
import com.adfonic.domain.OptimisationReportCompanyPreferences;
import com.adfonic.domain.Publication;
import com.adfonic.domain.RemovalInfo.RemovalType;
import com.adfonic.domain.User;
import com.adfonic.domain.User_;
import com.adfonic.dto.NameIdBusinessDto;
import com.adfonic.dto.advertiser.AdvertiserDto;
import com.adfonic.dto.publication.PublicationDto;
import com.adfonic.presentation.optimisation.datamodels.OptimisationUserInterfaceLivePublicationLazyDataModel;
import com.adfonic.presentation.optimisation.datamodels.OptimisationUserInterfaceRemovedPublicationLazyDataModel;
import com.adfonic.presentation.optimisation.service.AdvertiserOptimisationService;
import com.byyd.middleware.account.service.CompanyManager;
import com.byyd.middleware.account.service.UserManager;
import com.byyd.middleware.campaign.service.CampaignManager;
import com.byyd.middleware.creative.filter.CreativeFilter;
import com.byyd.middleware.creative.service.CreativeManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;
import com.byyd.middleware.publication.service.PublicationManager;
import com.byyd.middleware.utils.AdfonicBeanDispatcher;

@Service("advertiserOptimisationService")
public class AdvertiserOptimisationServiceImpl extends JdbcDaoSupport implements AdvertiserOptimisationService {
	
    private static Logger LOGGER = LoggerFactory.getLogger(AdvertiserOptimisationServiceImpl.class);
    

	public OptimisationUserInterfaceLivePublicationLazyDataModel createLivePublicationLazyDataModel(
			NameIdBusinessDto campaign,
			AdvertiserDto advertiser,
			String dateRange,
			boolean breakdownByCreative) {
		
		org.apache.commons.lang.time.StopWatch stopWatch = null;
		if (LOGGER.isDebugEnabled()) {
			stopWatch = new org.apache.commons.lang.time.StopWatch();
			stopWatch.start();
		}
		
		OptimisationUserInterfaceLivePublicationLazyDataModel dataModel = new OptimisationUserInterfaceLivePublicationLazyDataModel(
				getDataSource(),
				campaign,
				advertiser,
				dateRange,
				breakdownByCreative);
		
		if (LOGGER.isDebugEnabled()) {
			stopWatch.stop();
			LOGGER.debug("createLivePublicationLazyDataModel(): " + stopWatch.toString());
		}
		return dataModel;
    }

	public OptimisationUserInterfaceRemovedPublicationLazyDataModel createRemovedPublicationLazyDataModel(
			NameIdBusinessDto campaign,
			AdvertiserDto advertiser,
			String dateRange,
			boolean breakdownByCreative) {
		
		org.apache.commons.lang.time.StopWatch stopWatch = null;
		if (LOGGER.isDebugEnabled()) {
			stopWatch = new org.apache.commons.lang.time.StopWatch();
			stopWatch.start();
		}
		
		OptimisationUserInterfaceRemovedPublicationLazyDataModel dataModel = new OptimisationUserInterfaceRemovedPublicationLazyDataModel(
				getDataSource(),
				campaign,
				advertiser,
				dateRange,
				breakdownByCreative);
		
		if (LOGGER.isDebugEnabled()) {
			stopWatch.stop();
			LOGGER.debug("createRemovedPublicationLazyDataModel(): " + stopWatch.toString());
		}
		return dataModel;
    }
	
	public void removePublicationsFromCreatives(
			Long campaignId, 
			List<PublicationDto> publications,
			RemovalType removalType, 
			Long userId, 
			Long adfonicUserId) {
		PublicationManager publicationManager = AdfonicBeanDispatcher.getBean(PublicationManager.class);
		CampaignManager campaignManager = AdfonicBeanDispatcher.getBean(CampaignManager.class);
		CreativeManager creativeManager = AdfonicBeanDispatcher.getBean(CreativeManager.class);
		UserManager userManager = AdfonicBeanDispatcher.getBean(UserManager.class);
		User user = userManager.getUserById(userId);
		AdfonicUser adfonicUser = userManager.getAdfonicUserById(adfonicUserId);
		Campaign campaign = campaignManager.getCampaignById(campaignId);
		CreativeFilter filter = new CreativeFilter();
		filter.setCampaign(campaign);
		List<Creative.Status> statuses = new ArrayList<>();
		statuses.add(Creative.Status.ACTIVE);
		List<Creative> creatives = creativeManager.getAllCreatives(filter);
		for(Creative creative : creatives) {
			for(PublicationDto publicationDto : publications) {
				Publication publication = publicationManager.getPublicationById(publicationDto.getId());
		        LOGGER.debug("Removing publication external id: " + publication.getExternalID() + " id: " + publication.getId());
		        creativeManager.removePublicationFromCreative(creative, publication, removalType, user, adfonicUser);
			}
		}
	}
	
	public void removePublicationFromCreative(
			Long campaignId, 
			Long creativeId, 
			Long publicationId, 
			RemovalType removalType, 
			Long userId, 
			Long adfonicUserId) {
		PublicationManager publicationManager = AdfonicBeanDispatcher.getBean(PublicationManager.class);
        CampaignManager campaignManager = AdfonicBeanDispatcher.getBean(CampaignManager.class);
        CreativeManager creativeManager = AdfonicBeanDispatcher.getBean(CreativeManager.class);
		UserManager userManager = AdfonicBeanDispatcher.getBean(UserManager.class);
		Publication publication = publicationManager.getPublicationById(publicationId);
		User user = userManager.getUserById(userId);
		AdfonicUser adfonicUser = userManager.getAdfonicUserById(adfonicUserId);
		if(creativeId == null) {
			// We're not breaking down by Creatives, so load all the creatives linked to the campaign and remove each
			// from the publication
			Campaign campaign = campaignManager.getCampaignById(campaignId);
			CreativeFilter filter = new CreativeFilter();
			filter.setCampaign(campaign);
			List<Creative.Status> statuses = new ArrayList<>();
			statuses.add(Creative.Status.ACTIVE);
			List<Creative> creatives = creativeManager.getAllCreatives(filter);
			for(Creative creative : creatives) {
			    creativeManager.removePublicationFromCreative(creative, publication, removalType, user, adfonicUser);
			}
		} else {
			Creative creative = creativeManager.getCreativeById(creativeId);
			creativeManager.removePublicationFromCreative(creative, publication, removalType, user, adfonicUser);
		}
		
		
	}

	public void unremovePublicationFromCreative(
			Long campaignId, 
			Long creativeId, 
			Long publicationId,
			Long userId, 
			Long adfonicUserId) {
		PublicationManager publicationManager = AdfonicBeanDispatcher.getBean(PublicationManager.class);
        CampaignManager campaignManager = AdfonicBeanDispatcher.getBean(CampaignManager.class);
        CreativeManager creativeManager = AdfonicBeanDispatcher.getBean(CreativeManager.class);
		UserManager userManager = AdfonicBeanDispatcher.getBean(UserManager.class);
		Publication publication = publicationManager.getPublicationById(publicationId);
		User user = userManager.getUserById(userId);
		AdfonicUser adfonicUser = userManager.getAdfonicUserById(adfonicUserId);
		if(creativeId == null) {
			// We're not breaking down by Creatives, so load all the creatives linked to the campaign and reenable each
			// for the publication
			Campaign campaign = campaignManager.getCampaignById(campaignId);
			CreativeFilter filter = new CreativeFilter();
			filter.setCampaign(campaign);
			List<Creative.Status> statuses = new ArrayList<>();
			statuses.add(Creative.Status.ACTIVE);
			//filter.setStatuses(statuses);
			List<Creative> creatives = creativeManager.getAllCreatives(filter);
			for(Creative creative : creatives) {
			    creativeManager.unremovePublicationFromCreative(creative, publication, user, adfonicUser);
			}
		} else {
			Creative creative = creativeManager.getCreativeById(creativeId);
			creativeManager.unremovePublicationFromCreative(creative, publication, user, adfonicUser);
		}
	}
	
	public OptimisationReportCompanyPreferences getOptimisationReportCompanyPreferences(Long userId) {
	    UserManager userManager = AdfonicBeanDispatcher.getBean(UserManager.class);
	    CompanyManager companyManager = AdfonicBeanDispatcher.getBean(CompanyManager.class);
		FetchStrategy fs = new FetchStrategyBuilder()
		                   .addInner(User_.company)
		                   .build();
		User user = userManager.getUserById(userId, fs);
		Company company = user.getCompany();
		OptimisationReportCompanyPreferences prefs = companyManager.getOptimisationReportCompanyPreferencesForCompany(company);
		return prefs;
	}

}
