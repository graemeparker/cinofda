package com.adfonic.presentation.optimisation.service;

import java.util.List;

import com.adfonic.domain.OptimisationReportCompanyPreferences;
import com.adfonic.domain.RemovalInfo.RemovalType;
import com.adfonic.dto.NameIdBusinessDto;
import com.adfonic.dto.advertiser.AdvertiserDto;
import com.adfonic.dto.publication.PublicationDto;
import com.adfonic.presentation.optimisation.datamodels.OptimisationUserInterfaceLivePublicationLazyDataModel;
import com.adfonic.presentation.optimisation.datamodels.OptimisationUserInterfaceRemovedPublicationLazyDataModel;

/**
 * Service class:
 * 
 * - invoking the proper stored procedures via their mapping classes and data mappers, returning data mode 
 *   instances extending AbstractLazyDataModel
 *   
 * - handling the removal and unremoval of publications from creatives
 * 
 * - exposing OptimisationReportCompanyPreferences based on a User ID
 *  
 * @author pierre
 *
 */
public interface AdvertiserOptimisationService {

	OptimisationUserInterfaceLivePublicationLazyDataModel createLivePublicationLazyDataModel(
			NameIdBusinessDto campaign,
			AdvertiserDto advertiser,
			String dateRange,
			boolean breakdownByCreative);
	
	OptimisationUserInterfaceRemovedPublicationLazyDataModel createRemovedPublicationLazyDataModel(
			NameIdBusinessDto campaign,
			AdvertiserDto advertiser,
			String dateRange,
			boolean breakdownByCreative);
	
	void removePublicationFromCreative(
			Long campaignId, 
			Long creativeId, 
			Long publicationId, 
			RemovalType removalType, 
			Long userId, 
			Long adfonicUserId);
	void unremovePublicationFromCreative(
			Long campaignId, 
			Long creativeId, 
			Long publicationId, 
			Long userId, 
			Long adfonicUserId);
	
	void removePublicationsFromCreatives(
			Long campaignId, 
			List<PublicationDto> publications,
			RemovalType removalType, 
			Long userId, 
			Long adfonicUserId);
	
	OptimisationReportCompanyPreferences getOptimisationReportCompanyPreferences(Long userId);
}
