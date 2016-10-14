package com.adfonic.presentation.publicationlist.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Company;
import com.adfonic.domain.Publication;
import com.adfonic.domain.PublicationList;
import com.adfonic.domain.PublicationList.PublicationListLevel;
import com.adfonic.dto.campaign.publicationlist.PublicationInfoSearchForListDto;
import com.adfonic.dto.campaign.publicationlist.PublicationListInfoDto;
import com.adfonic.dto.campaign.publicationlist.PublicationSearchForListDto;
import com.adfonic.dto.campaign.typeahead.CampaignTypeAheadDto;
import com.adfonic.dto.publication.PublicationInfoDto;
import com.adfonic.presentation.publicationlist.dao.PublicationSearchDao;
import com.adfonic.presentation.publicationlist.service.PublicationListService;
import com.adfonic.presentation.util.GenericServiceImpl;
import com.byyd.middleware.account.service.AdvertiserManager;
import com.byyd.middleware.account.service.CompanyManager;
import com.byyd.middleware.campaign.service.CampaignManager;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.SortOrder;
import com.byyd.middleware.iface.dao.SortOrder.Direction;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.publication.filter.PublicationFilter;
import com.byyd.middleware.publication.filter.PublicationListFilter;
import com.byyd.middleware.publication.service.PublicationManager;

@Service("listPublicationService")
public class PublicationListServiceImpl extends GenericServiceImpl implements PublicationListService {
    
    private static final int MAX_IDS_PUBLICATIONS = 250;
    
	@Autowired
    private PublicationManager publicationManager;
	@Autowired
	private CampaignManager campaignManager;
    @Autowired
    private CompanyManager companyManager;
    @Autowired
    private AdvertiserManager advertiserManager;
    @Autowired
    private org.dozer.Mapper mapper;
    @Autowired
    private PublicationSearchDao publicationSearchDao;
    
    @Transactional(readOnly=true)
    public PublicationListInfoDto getPublicationListInfoById(Long id){
        PublicationList publicationList = publicationManager.getPublicationListById(id);
        if (publicationList != null) {
            return mapper.map(publicationList,PublicationListInfoDto.class);
        }else {
            return null;
        }
    }
    
    @Transactional(readOnly=true)
    public List<PublicationListInfoDto> getSavedListsInfo(long companyId, long advertiserId, boolean isWhiteList){
    	List<PublicationList> lpl = getAllPublicationLists(companyId, advertiserId, isWhiteList);
        List<PublicationListInfoDto> result = new ArrayList<PublicationListInfoDto>();
        for(PublicationList pl : lpl){
            result.add(mapper.map(pl, PublicationListInfoDto.class));
        }
        
        return result;
    }
    
    @Transactional(readOnly=true)
    public PublicationSearchForListDto search(final PublicationSearchForListDto publicationSearch){
        String searchString = publicationSearch.getSearchName();
        int publicationType = Integer.parseInt(publicationSearch.getSearchType());
        long numRecords = publicationSearch.getNumberOfRecords();
        long start = publicationSearch.getStart();
        List<Long> publicationIds = publicationSearch.getExcludedPublications();
        
        publicationSearch.setPublications(publicationSearchDao.getPublications(searchString, publicationType, numRecords, start, publicationIds));
        publicationSearch.setNumTotalRecords(publicationSearchDao.getNumberOfRecordsForPublications(searchString, publicationType, publicationIds));
        
        return publicationSearch;
    }
    
    @Transactional(readOnly=false)
    public PublicationListInfoDto save(PublicationListInfoDto publicationListInfoDto, List<PublicationInfoDto> publications, Long advertiserId,Long companyId){
        Company company = null;
        if(companyId!=null){
            company = companyManager.getCompanyById(companyId);
        }
        Advertiser advertiser = null;
        if(advertiserId!=null){
            advertiser = advertiserManager.getAdvertiserById(advertiserId);
        }
        
        Set<Publication> publicationSet = getPublications(publications);
        
        if(publicationListInfoDto.getId()!=null){
            PublicationList pl = publicationManager.getPublicationListById(publicationListInfoDto.getId());
            pl.setCompany(company);
            pl.setAdvertiser(advertiser);
            pl.setPublicationListLevel(PublicationListLevel.ADVERTISER_LEVEL);
            pl.setName(publicationListInfoDto.getName());
            pl.setWhiteList(publicationListInfoDto.getWhiteList());
            pl.setSnapshotDateTime(new Date());
            pl.setPublications(publicationSet);
            pl = publicationManager.update(pl);
            pl = publicationManager.getPublicationListById(publicationListInfoDto.getId());
            publicationListInfoDto = mapper.map(pl, PublicationListInfoDto.class);
        }
        else{
            PublicationList pl = new PublicationList();
            pl.setCompany(company);
            pl.setAdvertiser(advertiser);
            pl.setPublicationListLevel(PublicationListLevel.ADVERTISER_LEVEL);
            pl.setName(publicationListInfoDto.getName());
            pl.setWhiteList(publicationListInfoDto.getWhiteList());
            pl.setSnapshotDateTime(new Date());
            pl.setPublications(publicationSet);
            pl = publicationManager.create(pl);
            pl = publicationManager.getPublicationListById(pl.getId());
            publicationListInfoDto = mapper.map(pl, PublicationListInfoDto.class);
        }
        return publicationListInfoDto;
    }
    
    private Set<Publication> getPublications(List<PublicationInfoDto> publicationsInfoDto) {
    	List<Publication> result = new ArrayList<Publication>();
    	
    	int cnt;
		List<Long> ids;
    	Iterator<PublicationInfoDto> iterator = publicationsInfoDto.iterator();
    	while(iterator.hasNext()){
    		cnt = 0;
    		ids = new ArrayList<Long>();
    		while (iterator.hasNext() && cnt<MAX_IDS_PUBLICATIONS){
    			ids.add(iterator.next().getId());
    			cnt++;
    		}
    		if (CollectionUtils.isNotEmpty(ids)){
    			PublicationFilter filter  = new PublicationFilter();
        		filter.setIncludedIds(ids);
    			List<Publication> publications = publicationManager.getAllPublications(filter);
                if(CollectionUtils.isNotEmpty(publications)) {
                	result.addAll(publications);
                }
    		}	
    	}
    	return new HashSet<Publication>(result);
	}

	@Transactional(readOnly=false)
    public void deletePublicationList(PublicationListInfoDto publicationListInfoDto){
        PublicationList publicationList = publicationManager.getPublicationListById(publicationListInfoDto.getId());
        Set<Publication> publicationSet = new HashSet<>();
        publicationList.setPublications(publicationSet);
        publicationList = publicationManager.update(publicationList);
        publicationManager.delete(publicationList);
    }
    
    @Transactional(readOnly=true)
    public List<CampaignTypeAheadDto> getCampaigsUsingPublicationList(PublicationListInfoDto publicationListInfoDto){
        List<Campaign.Status> statuses = new ArrayList<Campaign.Status>();
        statuses.add(Campaign.Status.ACTIVE);
        statuses.add(Campaign.Status.PAUSED);
        statuses.add(Campaign.Status.NEW);
        statuses.add(Campaign.Status.NEW_REVIEW);
        statuses.add(Campaign.Status.PENDING);
        
        PublicationList publicationList = publicationManager.getPublicationListById(publicationListInfoDto.getId());
        
        List<Campaign> lCampaigns = campaignManager.getCampaignsForPublicationList(publicationList, statuses);
        
        List<CampaignTypeAheadDto> result = new ArrayList<CampaignTypeAheadDto>();
        for(Campaign c : lCampaigns){
            result.add(mapper.map(c, CampaignTypeAheadDto.class));
        }
        
        return result;
    }
    
    @Transactional(readOnly=true)
    public Integer countAllPublicationsInfo(PublicationInfoSearchForListDto publicationInfoSearchForListDto){
    	PublicationListInfoDto publicationListInfoDto = publicationInfoSearchForListDto.getPublicationListInfoDto();
    	PublicationList publicationList = publicationManager.getPublicationListById(publicationListInfoDto.getId());
    	Long count = publicationManager.countPublicationsForPublicationList(publicationList);
    	return count.intValue();
    }
    
    @Transactional(readOnly=true)
    public List<PublicationInfoDto> getPublicationsInfo(PublicationInfoSearchForListDto publicationInfoSearchForListDto){
    	PublicationListInfoDto publicationListInfoDto = publicationInfoSearchForListDto.getPublicationListInfoDto();
    	PublicationList publicationList = publicationManager.getPublicationListById(publicationListInfoDto.getId());
    	List<Publication> publications = null;
    	if (isPaginated(publicationInfoSearchForListDto)){
	    	Pagination pagination = new Pagination(publicationInfoSearchForListDto.getFirst(), 
	    										   publicationInfoSearchForListDto.getPageSize(), 
	    										   createSorting(publicationInfoSearchForListDto.getSortField(), publicationInfoSearchForListDto.getAscending())); 
	    	publications = publicationManager.getPublicationsForPublicationList(publicationList, pagination);
    	}else{
    		
    		publications = new ArrayList<Publication>(publicationList.getPublications());
    	}
    	List<PublicationInfoDto> result = new ArrayList<PublicationInfoDto>();
    	for(Publication publication : publications){
    		result.add(mapper.map(publication, PublicationInfoDto.class));
    	}
    	return result;
    }

	private boolean isPaginated(PublicationInfoSearchForListDto publicationInfoSearchForListDto) {
		return (publicationInfoSearchForListDto.getPageSize()>0);
	}

	// Private methods
    
    private List<PublicationList> getAllPublicationLists(long companyId, long advertiserId, boolean isWhiteList){
    	Company company = companyManager.getCompanyById(companyId);
        Advertiser advertiser = advertiserManager.getAdvertiserById(advertiserId);
        
        PublicationListFilter filter = new PublicationListFilter();
        filter.setCompany(company);
        filter.setAdvertiser(advertiser);
        filter.setWhiteList(isWhiteList);
        filter.setPublicationListLevel(PublicationListLevel.ADVERTISER_LEVEL);
        
        List<PublicationList> lpl = publicationManager.getAllPublicationLists(filter);
        
        return lpl;
    }
    
    private Sorting createSorting(String sortField, Boolean ascending) {
    	if (ascending==null){
    		ascending = true;
    	}
    	Direction direction = (ascending? Direction.ASC : Direction.DESC);
    	
    	List<String> fields = new ArrayList<String>();
    	fields.add(sortField);
    	
    	return new Sorting(SortOrder.create(direction, Publication.class, fields));
	}
}
