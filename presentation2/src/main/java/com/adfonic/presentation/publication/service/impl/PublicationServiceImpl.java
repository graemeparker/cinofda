package com.adfonic.presentation.publication.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.AdSpace_;
import com.adfonic.domain.AdfonicUser;
import com.adfonic.domain.Category;
import com.adfonic.domain.Platform;
import com.adfonic.domain.Publication;
import com.adfonic.domain.PublicationType;
import com.adfonic.domain.PublicationType_;
import com.adfonic.domain.Publication_;
import com.adfonic.domain.Publisher;
import com.adfonic.dto.publication.PublicationDto;
import com.adfonic.dto.publication.PublicationInfoDto;
import com.adfonic.dto.publication.enums.AdOpsStatus;
import com.adfonic.dto.publication.enums.PublicationStatus;
import com.adfonic.dto.publication.platform.PlatformDto;
import com.adfonic.dto.publication.publicationtype.PublicationtypeDto;
import com.adfonic.dto.publication.search.PublicationSearchDto;
import com.adfonic.dto.publication.typeahead.PublicationTypeAheadDto;
import com.adfonic.dto.user.UserDTO;
import com.adfonic.presentation.NameIdModel;
import com.adfonic.presentation.category.service.CategoryTypeAheadSearchService;
import com.adfonic.presentation.publication.model.PublicationApprovalDetailModel;
import com.adfonic.presentation.publication.model.PublicationApprovalModel;
import com.adfonic.presentation.publication.service.PublicationService;
import com.adfonic.presentation.util.GenericServiceImpl;
import com.adfonic.presentation.util.Utils;
import com.byyd.middleware.account.filter.AdfonicUserFilter;
import com.byyd.middleware.account.service.PublisherManager;
import com.byyd.middleware.account.service.UserManager;
import com.byyd.middleware.common.service.CommonManager;
import com.byyd.middleware.device.service.DeviceManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.LikeSpec;
import com.byyd.middleware.iface.dao.SortOrder;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;
import com.byyd.middleware.publication.filter.PublicationFilter;
import com.byyd.middleware.publication.service.PublicationManager;

@Service("publicationService")
public class PublicationServiceImpl extends GenericServiceImpl implements PublicationService {
    
    private static final transient Logger LOG = Logger.getLogger(PublicationServiceImpl.class.getName());
    
    private static final int MAX_IDS_PER_QUERY = 100;

	private FetchStrategy publicationFs = new FetchStrategyBuilder()
    .addLeft(Publication_.publisher)
    .addLeft(Publication_.adSpaces)
    .addLeft(Publication_.externalID)
    .addLeft(Publication_.publicationType)
    .addLeft(AdSpace_.formats)
    .build();
    
    @Autowired
    private CategoryTypeAheadSearchService categoryService;
	
    @Autowired
    private PublicationManager publicationManager;
    
    @Autowired
    private CommonManager commonManager;
    
    @Autowired
    private PublisherManager publisherManager;
    
    @Autowired
    private DeviceManager deviceManager;
    
    @Autowired
    private UserManager userManager;
    
    @Autowired
    private org.dozer.Mapper mapper;

    public PublicationSearchDto getPublications(final PublicationSearchDto publicationSearchDto) {
        
        
        PublicationSearchDto result = new PublicationSearchDto();
        

        PublicationFilter filter = new PublicationFilter();

        if (!StringUtils.isEmpty(publicationSearchDto.getName())) {
            filter.setNameLike(publicationSearchDto.getName(), LikeSpec.CONTAINS, false);
        }

        if (publicationSearchDto.getPublisher() != null && publicationSearchDto.getPublisher().getId() != null) {
            filter.setPublisher(publisherManager.getPublisherById(publicationSearchDto.getPublisher().getId()));
        }

        Collection<Publication> publications = publicationManager.getAllPublications(filter);
        Collection<PublicationTypeAheadDto> publicationsResult = getList(PublicationTypeAheadDto.class, publications);
        result.setPublications(publicationsResult);

        return result;
    }

    public PublicationTypeAheadDto getPublicationWithNameForPublisher(final PublicationSearchDto dto) {
        List<Publication> publications = publicationManager.getPublicationsWithNameForPublisher(dto.getName(), false, publisherManager.getPublisherById(dto.getPublisher().getId()), null);
        if (!CollectionUtils.isEmpty(publications) && publications.size() == 1) {
            return getObjectDto(PublicationTypeAheadDto.class, publications.get(0));
        }
        else {
            return new PublicationTypeAheadDto();
        }

    }

    public PublicationTypeAheadDto getPublicationById(final PublicationSearchDto dto) {
        Publication publication = publicationManager.getPublicationById(dto.getId(),publicationFs);
        if (publication != null) {
            return mapper.map(publication, PublicationTypeAheadDto.class);
        }
        else {
            return null;
        }
    }
    
    public PublicationDto getPublicationById(final Long id) {
        Publication publication = publicationManager.getPublicationById(id,publicationFs);
        if (publication != null) {
            return mapper.map(publication, PublicationDto.class);
        }
        else {
            return null;
        }
    }
    
    public PublicationDto getPublicationByExternalId(final String id) {
        Publication publication = publicationManager.getPublicationByExternalId(id,publicationFs);
        if (publication != null) {
            return mapper.map(publication, PublicationDto.class);
        }
        else {
            return null;
        }
    }
    
    @Transactional(readOnly=true)
    public Map<Long, PublicationInfoDto> getActivePublicationInfoByExternalIds(final List<String> ids) {
        
        Map<Long, PublicationInfoDto> publicationsInfo = new HashMap<Long, PublicationInfoDto>();
        
        int fromIndex = 0;
        int toIndex = 0;
        List<String> subIdsList = null;
        while(toIndex!=ids.size()){
	        toIndex = fromIndex + MAX_IDS_PER_QUERY; 
	        if (toIndex > ids.size()){
	        	toIndex = ids.size();	
	        }
	        subIdsList = ids.subList(fromIndex, toIndex);
	        
	        List<Publication> publications = publicationManager.getPublicationByExternalIds(subIdsList, null, null);
	        if (CollectionUtils.isNotEmpty(publications)) {
	        	for(Publication publication : publications){
	        		if (publication.getStatus().equals(PublicationStatus.ACTIVE.getStatus())){
	        			publicationsInfo.put(publication.getId(), mapper.map(publication, PublicationInfoDto.class));
	        		}
	        	}
	        }
	        
	        fromIndex = toIndex;
        }
        
        return publicationsInfo;
    }
    
    public List<PublicationTypeAheadDto> getPublicationsById(final String[] publicationsIds) {
        List<PublicationTypeAheadDto> result = new ArrayList<PublicationTypeAheadDto>(0);
        for (String pub : publicationsIds) {
            PublicationSearchDto dto = new PublicationSearchDto();
            dto.setId(Long.parseLong(pub.trim()));
            PublicationTypeAheadDto publicationDto = getPublicationById(dto);
            if (publicationDto != null)
                result.add(publicationDto);
        }
        return result;
    }
    
    public PlatformDto getPlatformById(long id){
        Platform p = publicationManager.getObjectById(Platform.class, id);
        if(p!=null){
            return mapper.map(p, PlatformDto.class);
        }
        else
            return null;
    }
    
    public PlatformDto getPlatformByName(String name){
        Platform p = deviceManager.getPlatformByName(name);
        if(p!=null){
            return mapper.map(p, PlatformDto.class);
        }
        else
            return null;
    }
    
    public List<PlatformDto> getAllPlatforms(final boolean hasAllObject){
        
        SortOrder order = new SortOrder("name");
        Sorting sort = new Sorting(order);
        
        List<Platform> platforms = deviceManager.getAllPlatforms(sort);
        
        List<PlatformDto> platformsDto = new ArrayList<PlatformDto>();
        if(hasAllObject){
            PlatformDto allDto = new PlatformDto();
            allDto.setId((long)-1);
            allDto.setName("All");
            platformsDto.add(allDto);
        }
        
        for(Platform p : platforms){
            platformsDto.add(getObjectDto(PlatformDto.class,p));
        }
        return platformsDto;       
    }
    
    public List<PlatformDto> getAllOrderedPlatforms(final boolean hasAllObject){        
        List<PlatformDto> platforms = getAllPlatforms(hasAllObject);
        Map<String, PlatformDto> map = new HashMap<String, PlatformDto>();
        
        for(PlatformDto p : platforms){
            map.put(p.getSystemName(), p);
        }
        
        List<PlatformDto> res = new ArrayList<PlatformDto>();
        
        if(map.containsKey("ios"))
            res.add(map.get("ios"));
        if(map.containsKey("windows"))
            res.add(map.get("windows"));
        if(map.containsKey("android"))
            res.add(map.get("android"));
        if(map.containsKey("webos"))
            res.add(map.get("webos"));
        if(map.containsKey("symbian"))
            res.add(map.get("symbian"));
        if(map.containsKey("wp7"))
            res.add(map.get("wp7"));
        if(map.containsKey("rim"))
            res.add(map.get("rim"));
        if(map.containsKey("midp"))
            res.add(map.get("midp"));
        if(map.containsKey("other"))
            res.add(map.get("other"));
        
        return res;       
    }
    
    public List<PublicationtypeDto> getPublicationType(PlatformDto platformDto){
        List<PublicationtypeDto> result = new ArrayList<PublicationtypeDto>();
        FetchStrategy pubTypeFs = new FetchStrategyBuilder().addLeft(PublicationType_.platforms).build();
                
        List<PublicationType> pubTypes = publicationManager.getAllPublicationTypes(pubTypeFs);
        
        for(PublicationType pt : pubTypes){
            for(Platform p : pt.getPlatforms()){
                if(p.getSystemName().equals(platformDto.getSystemName())){
                    result.add(mapper.map(pt, PublicationtypeDto.class));
                    break;
                }
            }
        }
        
        return result;
    }
    
    public List<PublicationtypeDto> getPublicationType(String medium){
        List<PublicationtypeDto> result = new ArrayList<PublicationtypeDto>();                
        List<PublicationType> pubTypes = publicationManager.getAllPublicationTypes();
        
        for(PublicationType pt : pubTypes){
            if(pt.getMedium().toString().equals(medium))
                result.add(mapper.map(pt, PublicationtypeDto.class));
        }
        
        return result;
    }
    
    public List<PublicationtypeDto> getPublicationType(final boolean hasAllObject){
        List<PublicationtypeDto> result = new ArrayList<PublicationtypeDto>();                
        List<PublicationType> pubTypes = publicationManager.getAllPublicationTypes();
        
        if(hasAllObject){
            PublicationtypeDto allDto = new PublicationtypeDto();
            allDto.setId((long)-1);
            allDto.setName("All");
            result.add(allDto);
        }
        
        for(PublicationType pt : pubTypes){
            result.add(mapper.map(pt, PublicationtypeDto.class));
        }
        
        return result;
    }
    
    public PublicationDto save(PublicationDto dto) throws Exception{
     // Update publication
        if (dto.getId() != null && dto.getId().longValue() > 0) {
            // already persisted
            Publication publication = publicationManager.getPublicationById(dto.getId(),publicationFs);
            
            publication.setName(dto.getName());
            publication.setURLString(dto.getUrlString());
            publication.setDescription(dto.getDescription());
            publication.setAutoApproval(dto.isAutoApproval());
            publication.setBackfillEnabled(dto.isBackfillEnabled());
            publication.setMinAge(dto.getMinAge());
            publication.setMaxAge(dto.getMaxAge());
            publication.setGenderMix(dto.getGenderMix());
            
            PublicationType type = publicationManager.getPublicationTypeById(dto.getPublicationType().getId(),publicationFs);
            publication.setPublicationType(type);
            
            publicationManager.update(publication);

        } else {
            Publisher pub = publisherManager.getPublisherById(dto.getPublisher().getId());

            Publication entity = new Publication(pub);
            
            entity.setName(dto.getName());
            entity.setURLString(dto.getUrlString());
            entity.setDescription(dto.getDescription());
            entity.setAutoApproval(dto.isAutoApproval());
            entity.setBackfillEnabled(dto.isBackfillEnabled());
            entity.setMinAge(dto.getMinAge());
            entity.setMaxAge(dto.getMaxAge());
            entity.setGenderMix(dto.getGenderMix());
            
            if(dto.getPublicationType()!=null){
                PublicationType type = publicationManager.getPublicationTypeById(dto.getPublicationType().getId());
                entity.setPublicationType(type);
            }
                        
            if (StringUtils.isEmpty(entity.getName())) {
                entity.setName(StringUtils.EMPTY);
            }
            
            entity = publicationManager.create(entity);
            
            entity = publicationManager.getPublicationById(entity.getId(),publicationFs);
            
            dto = mapper.map(entity, PublicationDto.class);
        }
        
        return dto;
    }
    
    public PublicationtypeDto getPublicationTypeByName(final String name){
        PublicationType pt = publicationManager.getPublicationTypeByName(name);
        
        return mapper.map(pt, PublicationtypeDto.class);
    }
    
    public PublicationtypeDto getPublicationTypeById(final Long id){
        PublicationType pt = publicationManager.getPublicationTypeById(id);
        return mapper.map(pt, PublicationtypeDto.class);
    }    
    
    public PublicationtypeDto getPublicationTypeBySystemName(final String name){
        PublicationType pt = publicationManager.getPublicationTypeBySystemName(name);
        
        return mapper.map(pt, PublicationtypeDto.class);
    }
    
    public void changePublicationStatus(List<Long> publicationIds , PublicationStatus pubStatus) {
        if(!CollectionUtils.isEmpty(publicationIds)){
            for( Long id:publicationIds ) {
                Publication pub = publicationManager.getPublicationById(id);
                
                    if(pub.transitionStatus(pubStatus.getStatus())){
                        publicationManager.update(pub);
                    }
            }
        }
    }
    
    /**
     * Update publication status while keep history in sync with comment
     * Similar during savePublicationApprovalDetails while update status only for one publication
     */
    @Override
    public void changePublicationStatusWithHistory(Long publicationId, PublicationStatus publicationStatus, AdfonicUser adfonicUser) {
        Publication currentPublication = publicationManager.getPublicationById(publicationId);
        
        // Update Publication Status and AdOps Status
        boolean statusOrAdOpsStatusChanged = publicationManager.updatePublicationStatus(
                currentPublication, publicationStatus.getStatus(), AdOpsStatus.NONE.getStatus());
                
        // Comment, History
        // Creating a history entry if
        // a) publication.status or publication.adOpsStatus changed
        if (statusOrAdOpsStatusChanged) {
            publicationManager.newPublicationHistory(currentPublication, "Bulk Status Changed", adfonicUser);
        }
    }
    
    public PublicationDto submit(PublicationDto dto,UserDTO userDto) throws Exception{
        Publisher publisher = publisherManager.getPublisherById(userDto.getPublisherDto().getId());
        Publication publication = publicationManager.getPublicationById(dto.getId(),publicationFs);
        publication.setStatus(Publication.Status.PENDING);
        publication.setSubmissionTime(new Date());
        publication.setDisclosed(publisher.isDisclosed());
        publication = publicationManager.update(publication);
        
        publication = publicationManager.getPublicationById(publication.getId(),publicationFs);
        
        return mapper.map(publication, PublicationDto.class);
    }

    @Override
    public boolean savePublicationApprovalDetails(PublicationApprovalDetailModel dto, AdfonicUser adfonicUser, boolean assignedToUserChanged, boolean commentChanged) {

        FetchStrategy publicationFs = new FetchStrategyBuilder()
        .addLeft(Publication_.publicationType) // for publication type
        .addLeft(Publication_.publicationAttributes) // for softFloor
        .addLeft(Publication_.category) // for stated category
        .addLeft(Publication_.excludedCategories) // for excluded categories
        
        .addLeft(Publication_.watchers) // for publication watchers
        .addLeft(Publication_.assignedTo) // for assignedTo user
        .addLeft(Publication_.history) // for publication history
        .build();
        
        // Get the current publication
        Publication currentPublication = publicationManager.getPublicationById(dto.getInternalId(), publicationFs);
        
        // SAVING PUBLICATION APPROVAL fields
        
        // Update Publication Status and AdOps Status
        boolean statusOrAdOpsStatusChanged = publicationManager.updatePublicationStatus(
                currentPublication, PublicationStatus.valueOf(dto.getStatus()).getStatus(), dto.getAdOpsStatus().getStatus());
        
        // Get updated publication (as updatePublicationStatus method might changed publication like its approved date)
        currentPublication = publicationManager.getPublicationById(dto.getInternalId(), publicationFs);
        
        // Assigned To
        Long assignedToAdfonicUserId = Long.valueOf(dto.getAssignedTo());
        AdfonicUser assignedToAdfonicUser = null;
        if (assignedToAdfonicUserId > 0 ) {
            assignedToAdfonicUser = userManager.getAdfonicUserById(assignedToAdfonicUserId);
        }
        currentPublication.setAssignedTo(assignedToAdfonicUser);
        
        // Comment, History
        // Only bother creating a history entry
        // if at least one of the following occurred:
        // a) publication.status or publication.adOpsStatus changed
        // b) publication.assignedTo changed
        // c) the Admin user entered a comment
        if (statusOrAdOpsStatusChanged || assignedToUserChanged || commentChanged) {
            publicationManager.newPublicationHistory(currentPublication, dto.getComment(), adfonicUser);
        }
        
        // Saving Watchers
        Set<AdfonicUser> watchers = new HashSet<AdfonicUser>();
        
        // User automatically added as watcher
        watchers.add(adfonicUser);
        
        // Add further watchers
        if(!dto.getWatchers().isEmpty()) {
            watchers.addAll(userManager.getAllAdfonicUsers(new AdfonicUserFilter().setAdfonicUserIds(dto.getWatchers())));
        }
        
        // Add Assigned To as a watchers as well
        if (assignedToAdfonicUser != null) {
            watchers.add(assignedToAdfonicUser);
        }
        currentPublication.getWatchers().clear();
        currentPublication.getWatchers().addAll(watchers);
        
        // SAVING PUBLICATION DETAILS fields
        
        // Get and set the new publication type
        PublicationType publicationType = publicationManager.getPublicationTypeByName(dto.getType());
        currentPublication.setPublicationType(publicationType);
        
        // Disclose identity
        currentPublication.setDisclosed(dto.getDiscloseIdentity());
        
        // Friendly Name
        currentPublication.setFriendlyName(dto.getFriendlyName());
        
        // Safety level
        currentPublication.setSafetyLevel(dto.getSafetyLevel().getSafetyLevel());
        
        // Soft Floor
        currentPublication.setSoftFloor(dto.getSoftFloor());
        
        // Sampling Rate
        currentPublication.setSamplingRate(NumberUtils.createInteger(dto.getSamplingRate()));
        
        // IAB Category
        if (!StringUtils.isEmpty(dto.getStatedCategory())){
            currentPublication.setCategory(getCategoryByName(dto.getStatedCategory()));
        }
        
        // Excluded Categories
        currentPublication.getExcludedCategories().clear();
        if (dto.getExcludedCategories() != null) {
            Set<Category> categories = new HashSet<Category>();
            for (NameIdModel nameIdModel : dto.getExcludedCategories()) {
                categories.add(getCategoryByName(nameIdModel.getName()));
            }
            currentPublication.getExcludedCategories().addAll(categories);
        }
        
        // UPDATING PUBLICATION
        publicationManager.update(currentPublication);

        return statusOrAdOpsStatusChanged;
    }
    
    @Override
    public void assignUserToPublication(PublicationApprovalModel dto, AdfonicUser adfonicUser, Long newAssignedToUserId) {
        FetchStrategy publicationFs = new FetchStrategyBuilder()
        .addLeft(Publication_.watchers) // for publication watchers
        .addLeft(Publication_.assignedTo) // for assignedTo user
        .build();
        
        String logTitle = "Publication ('" + dto.getInternalId() + "') Bulk Assign to User";
        
        // Get the current Publication
        Publication currentPublication = publicationManager.getPublicationById(dto.getInternalId(), publicationFs);
        
        // Assign or Unassign operation was requested
        boolean isAssignOperation = (newAssignedToUserId != 0) ? true : false;

        // Do nothing once the assigned to user id not changed
        Long currentAssignedToUserId = (currentPublication.getAssignedTo() == null) ? 0L : currentPublication.getAssignedTo().getId();
        if (newAssignedToUserId.equals(currentAssignedToUserId)) {
            return;
        }
        
        // Set new Assigned To User
        AdfonicUser newAssignedToUser = null;
        String newAssignedToUserFullName = StringUtils.EMPTY;
        if (isAssignOperation) {
            newAssignedToUser = userManager.getAdfonicUserById(newAssignedToUserId);
            newAssignedToUserFullName = newAssignedToUser.getFullName();
        }
        
        currentPublication.setAssignedTo(newAssignedToUser);
        Utils.logWithTitle(LOG, Level.INFO, logTitle, "Reassigning publication to " + (isAssignOperation ? newAssignedToUserFullName : "nobody"));
        
        // Create a new history entry to track the change
        String assignedToComment;
        if (currentPublication.getAssignedTo() == null) {
            assignedToComment = "Bulk Assigned";
        } else if (isAssignOperation) {
            assignedToComment = "Bulk Reassigned";
        } else {
            assignedToComment = "Bulk Unassigned";
        }
        
        // Create new history with assign details
        publicationManager.newPublicationHistory(currentPublication, assignedToComment, adfonicUser);
        Utils.logWithTitle(LOG, Level.INFO, logTitle, "Adding new history entry with '" + assignedToComment + "' comment by " + adfonicUser.getFullName());

        // Add the new Assigned To User as a watcher (if still not there)
        if (isAssignOperation && !currentPublication.getWatchers().contains(newAssignedToUser)) {
            currentPublication.getWatchers().add(newAssignedToUser);
            Utils.logWithTitle(LOG, Level.INFO, logTitle, "Adding " + newAssignedToUserFullName + " as a new watcher");
        }
        
        // Update publication
        publicationManager.update(currentPublication);
    }
    
    private Category getCategoryByName(String selectedCategoryName) {
        List<NameIdModel> categoryResults = categoryService.searchForCategories(selectedCategoryName);
        if(!categoryResults.isEmpty()) {                    
            return commonManager.getCategoryById(categoryResults.get(0).getId());
        }
        return null;
    }

}
