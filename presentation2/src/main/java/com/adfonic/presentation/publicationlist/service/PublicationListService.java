package com.adfonic.presentation.publicationlist.service;

import java.util.List;

import com.adfonic.dto.campaign.publicationlist.PublicationInfoSearchForListDto;
import com.adfonic.dto.campaign.publicationlist.PublicationListInfoDto;
import com.adfonic.dto.campaign.publicationlist.PublicationSearchForListDto;
import com.adfonic.dto.campaign.typeahead.CampaignTypeAheadDto;
import com.adfonic.dto.publication.PublicationInfoDto;

public interface PublicationListService {
    public PublicationListInfoDto getPublicationListInfoById(Long id);
    public List<PublicationListInfoDto> getSavedListsInfo(long companyId, long advertiserId, boolean isWhiteList);
    public PublicationSearchForListDto search(final PublicationSearchForListDto publicationSearch);
    public PublicationListInfoDto save(PublicationListInfoDto publicationListDto, List<PublicationInfoDto> publications, Long advertiserId,Long companyId);
    public void deletePublicationList(PublicationListInfoDto publicationListInfoDto);
    
    public List<CampaignTypeAheadDto> getCampaigsUsingPublicationList(PublicationListInfoDto publicationListInfoDto);
    
    public Integer countAllPublicationsInfo(PublicationInfoSearchForListDto publicationInfoSearchForListDto);
    public List<PublicationInfoDto> getPublicationsInfo(PublicationInfoSearchForListDto publicationInfoSearchForListDto);
}
