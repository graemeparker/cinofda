package com.adfonic.presentation.audience.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.adfonic.dto.audience.AudienceFileDto;
import com.adfonic.presentation.audience.dao.MuidNotificationDao;
import com.adfonic.presentation.audience.enums.FileType;
import com.adfonic.presentation.audience.model.MuidSessionModel;
import com.adfonic.presentation.audience.service.AudienceFileService;
import com.adfonic.presentation.location.model.GeoLocationModel;
import com.byyd.elasticsearch.model.BinarySearchExpression.SearchLogicalOperator;
import com.byyd.elasticsearch.model.Hit;
import com.byyd.elasticsearch.model.MultipleSearchExpression;
import com.byyd.elasticsearch.model.SearchExpression;
import com.byyd.elasticsearch.model.SearchResult;
import com.byyd.elasticsearch.model.SimpleSearchExpression;
import com.byyd.elasticsearch.model.SimpleSearchExpression.SearchFieldOperator;
import com.byyd.elasticsearch.model.SortingInfo;
import com.byyd.elasticsearch.model.SortingInfo.SortingOrder;
import com.byyd.elasticsearch.model.audienceengine.AudienceEngineIndexTypes;
import com.byyd.elasticsearch.model.audienceengine.FileLinkFields;
import com.byyd.elasticsearch.model.filemover.FileFields;
import com.byyd.elasticsearch.model.filemover.FileMoverIndexTypes;
import com.byyd.elasticsearch.model.filemover.GeoPoiFields;
import com.byyd.elasticsearch.service.ElasticSearchService;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.SortOrder;
import com.byyd.middleware.iface.dao.SortOrder.Direction;

@Service("audienceFileService")
public class AudienceFileServiceImpl implements AudienceFileService {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(AudienceFileServiceImpl.class);
    
    // Configuration
    @Value("${elasticsearch.cluster:elasticsearch}")
    private String clusterName;

    @Value("${elasticsearch.hosts:localhost}")
    private String[] hosts;

    @Value("${elasticsearch.port:9300}")
    private Integer port;

    @Value("${elasticsearch.user:}")
    private String user;

    @Value("${elasticsearch.pwd:}")
    private String pwd;
    
    @Value("${elasticsearch.index.filemover:filemover}")
    private String fmIndex;
    
    @Value("${elasticsearch.index.audienceengine:audienceengine}")
    private String aeIndex;
    
    private ElasticSearchService elasticSearchService;
    
    @Autowired
    MuidNotificationDao muidNotificationDao;
    
    @PostConstruct
    public void initialize() {
        LOGGER.debug("Inisialiting elasticsearch service for Audience Files management");
        elasticSearchService = new ElasticSearchService(clusterName, hosts, port, user, pwd);
    }
    
    //
    // Audience Files methods
    //
    
    /*
     * (non-Javadoc)
     * @see com.adfonic.presentation.audience.service.AudienceFileService#getAudienceSize(java.lang.Long)
     */
    public Long getAudienceSize(FileType fileType, Long audienceId){
        // Get all files linked to the audience
        SearchResult audienceFileLinks = getAudienceFileLinks(audienceId);
        
        LOGGER.debug("Calculating audience size for audience id " + audienceId + "/type:" + fileType);
        long audienceSize = 0;
        if (audienceFileLinks.getTotalHits()>0){
            for(Hit fileLink : audienceFileLinks.getHits()){
                if (FileType.DEVICES == fileType){
                    String audienceSession = (String) fileLink.getSource().get(FileLinkFields.SESSION_ID);
                    LOGGER.debug("Audience id " + audienceId +" has a MUID session Id " + audienceSession);
                    MuidSessionModel sessionModel = muidNotificationDao.inboundCheckProgress(new BigDecimal(audienceSession));
                    if (sessionModel!=null){
                        Long sessionIngested = sessionModel.getIngested().longValue();
                        LOGGER.debug("MUID session Id " + audienceSession + " has " + sessionIngested + " devices ingested");
                        audienceSize += sessionIngested;
                    }else{
                        LOGGER.debug("MUID session information not found for session id " + audienceSession);
                    }
                }else{
                    String audienceSession = (String) fileLink.getSource().get(FileLinkFields.SESSION_ID);
                    Long sessionIngested = ((Number) fileLink.getSource().get(FileLinkFields.SIZE)).longValue();
                    LOGGER.debug("AudienceEngine session Id " + audienceSession + " has " + sessionIngested + " locations ingested");
                    audienceSize += sessionIngested;
                }
            }
        }else{
            LOGGER.debug("Audience id " + audienceId +" does not have any file linked");
        }
        
        return audienceSize;
    }
    
    /*
     * (non-Javadoc)
     * @see com.adfonic.presentation.audience.service.AudienceFileService#countAssignedFiles(java.lang.Long)
     */
    public Long countAssignedFiles(Long audienceId){
        List<SearchExpression> searchExpressions = new ArrayList<>();
        searchExpressions.add(new SimpleSearchExpression(FileLinkFields.AUDIENCE_ID, audienceId.toString()));
        return elasticSearchService.count(aeIndex, AudienceEngineIndexTypes.FILE_LINK, searchExpressions);
    }
    
    /*
     * (non-Javadoc)
     * @see com.adfonic.presentation.audience.service.AudienceFileService#getFileNamesFromAssignedFiles(java.lang.Long)
     */
    public List<String> getFileNamesFromAssignedFiles(Long audienceId){
        List<String> result = new ArrayList<String>();
        
        // Get all files linked to the audience
        SearchResult audienceFileLinks = getAudienceFileLinks(audienceId);
       
        if (audienceFileLinks.getTotalHits()>0){ 
            // Get all files information for each FileLink
            SearchResult files = getFileFromAudienceFileLinks(audienceFileLinks, null, null);
            
            if (files.getHits()!=null){
                for(Hit file : files.getHits()){
                    result.add((String) file.getSource().get(FileFields.NAME));
                }
            }
        }
        
        return result;
    }
    
    /*
     * (non-Javadoc)
     * @see com.adfonic.presentation.audience.service.AudienceFileService#getAssignedFiles(com.adfonic.presentation.audience.enums.FileType, java.lang.Long, java.util.List, com.byyd.middleware.iface.dao.Pagination)
     */
    public List<AudienceFileDto> getAssignedFiles(FileType fileType, Long audienceId, List<String> filesIdsToAdd, Pagination pagination){
        List<AudienceFileDto> result = new ArrayList<AudienceFileDto>();
        
        // Get all files linked to the audience
        SearchResult audienceFileLinks = getAudienceFileLinks(audienceId);
       
        // Get all files information for each FileLink        
        if ((audienceFileLinks.getTotalHits()>0)||(filesIdsToAdd.size()>0)){
            SearchResult files = getFileFromAudienceFileLinks(audienceFileLinks, filesIdsToAdd, pagination);
            
            // Construct AudienceFileDto list as result
            for(Hit file : files.getHits()){
                // Get filelink
                Hit fileLink = searchFileLink(audienceFileLinks, file.getId());
                
                String audienceSession = "";
                Long audienceTotals = 0L;
                String audienceStatus = "";
                
                if (fileLink!=null){
                    audienceSession = (String) fileLink.getSource().get(FileLinkFields.SESSION_ID);
                    if (fileType == FileType.DEVICES){
                        // Get MUID status and total devices stored
                        MuidSessionModel sessionModel = getMUIDInformation(audienceFileLinks, file.getId());
                        if (sessionModel!=null){
                            audienceTotals = sessionModel.getIngested().longValue(); 
                            audienceStatus = sessionModel.getStatus();
                        }
                    }else{
                        audienceTotals = ((Number) fileLink.getSource().get(FileLinkFields.SIZE)).longValue(); 
                        audienceStatus = (String) fileLink.getSource().get(FileLinkFields.STATUS);
                    }
                }
                
                // Build dto
                AudienceFileDto audienceFileDto = new AudienceFileDto(file.getId(), 
                                                                     new Date(((Number) file.getSource().get(FileFields.STATUS_DATE)).longValue()), 
                                                                     (String) file.getSource().get(FileFields.NAME), 
                                                                     (String) file.getSource().get(FileFields.STATUS),
                                                                     (String) file.getSource().get(FileFields.SUBTYPE),
                                                                     ((Number) file.getSource().get(FileFields.VALIDS)).longValue(), 
                                                                     ((Number) file.getSource().get(FileFields.TOTALS)).longValue(), 
                                                                     audienceSession,
                                                                     audienceTotals,
                                                                     audienceStatus);
                // add dto to result list
                result.add(audienceFileDto);
            }
        }
        
        return result;
    }

    /*
     * (non-Javadoc)
     * @see com.adfonic.presentation.audience.service.AudienceFileService#countUnassignedFiles(com.adfonic.presentation.audience.enums.FileType, java.lang.Long, java.lang.String, java.lang.String, java.util.List)
     */
    public Long countUnassignedFiles(FileType fileType, Long audienceId, String externalCompanyId, String externalAdvertiserId, List<String> removeFilesId){
        // Get current files assigned to the audience
        removeFilesId.addAll(getAssignedFilesIds(audienceId));
        
        // Search expression 
        List<SearchExpression> searchExpression = getUnassignedFilesSearchExpression(fileType, externalCompanyId, externalAdvertiserId, removeFilesId);
        
        // Execute count
        Long count = elasticSearchService.count(fmIndex, FileMoverIndexTypes.FILE, searchExpression);
        
        return count;
    }
    
    /*
     * (non-Javadoc)
     * @see com.adfonic.presentation.audience.service.AudienceFileService#getUnassignedDevicesFiles(java.lang.String, java.lang.String, java.util.List, com.byyd.middleware.iface.dao.Pagination)
     */
    public List<AudienceFileDto> getUnassignedFiles(FileType fileType, Long audienceId, String externalCompanyId, String externalAdvertiserId, List<String> filesIdsToRemove, Pagination pagination){
        // Get current files assigned to the audience
        List<String> removeFilesId = new ArrayList<>(filesIdsToRemove);
        removeFilesId.addAll(getAssignedFilesIds(audienceId));
        
        // Search expression 
        List<SearchExpression> searchExpression = getUnassignedFilesSearchExpression(fileType, externalCompanyId, externalAdvertiserId, removeFilesId);
        
        // Sortings
        List<SortingInfo> sortings = getSortingsInfo(pagination);
        
        // Pagination
        com.byyd.elasticsearch.model.Pagination esPagination = getESPagination(pagination);
        
        // Execute query using pagination
        SearchResult files = elasticSearchService.search(fmIndex, FileMoverIndexTypes.FILE, searchExpression, sortings, esPagination);
        
        List<AudienceFileDto> result = new ArrayList<AudienceFileDto>(files.getHits().size());
        for(Hit file : files.getHits()){
            // Build dto 
            AudienceFileDto audienceFileDto = new AudienceFileDto(file.getId(), 
                                                                  new Date(((Number) file.getSource().get(FileFields.STATUS_DATE)).longValue()), 
                                                                  (String) file.getSource().get(FileFields.NAME), 
                                                                  (String) file.getSource().get(FileFields.STATUS),
                                                                  (String) file.getSource().get(FileFields.SUBTYPE),
                                                                  ((Number) file.getSource().get(FileFields.VALIDS)).longValue(), 
                                                                  ((Number) file.getSource().get(FileFields.TOTALS)).longValue());
            // add dto to result list
            result.add(audienceFileDto);
        }
        
        return result;
    }
    
    //
    //Geopoint methods
    //
    
    /*
     * (non-Javadoc)
     * @see com.adfonic.presentation.audience.service.AudienceFileService#getGeopointsFromAudience(java.lang.Long, java.lang.Integer)
     */
    public List<GeoLocationModel> getGeopointsFromAudience(Long audienceId, Integer limit){
        // Result
        List<GeoLocationModel> locations = new ArrayList<GeoLocationModel>();
        
        // Get all files linked to the audience
        SearchResult audienceFileLinks = getAudienceFileLinks(audienceId);
        
        // Get locations for each filelink
        if (audienceFileLinks.getHits()!=null){
            for (int cnt=0; (cnt<audienceFileLinks.getHits().size() && (locations.size()<limit)); cnt++){
                // File id
                String fileId = (String) audienceFileLinks.getHits().get(cnt).getSource().get(FileLinkFields.FILE_ID);
                
                // Get locations for file id
                locations.addAll(getGeoPoints(fileId, limit - locations.size()));
            }
        }
        
        return locations;
    }
    
    private List<GeoLocationModel> getGeoPoints(String fileId, Integer limit){
        // Search Expression
        List<SearchExpression> searchExpressions = new ArrayList<>();
        searchExpressions.add(new SimpleSearchExpression(GeoPoiFields.FILE, fileId));
        
        com.byyd.elasticsearch.model.Pagination pagination = new com.byyd.elasticsearch.model.Pagination(0, limit);
        
        // Execute query
        SearchResult geopois = elasticSearchService.search(fmIndex, FileMoverIndexTypes.GEOPOI, searchExpressions, null, pagination);
        
        List<GeoLocationModel> geoLocations = new ArrayList<>();
        if (geopois.getHits()!=null){
            for (Hit geopoi : geopois.getHits()){
                geoLocations.add(new GeoLocationModel((String) geopoi.getSource().get(GeoPoiFields.NAME),
                                                      new BigDecimal((String) geopoi.getSource().get(GeoPoiFields.LATITUDE)),
                                                      new BigDecimal((String) geopoi.getSource().get(GeoPoiFields.LONGITUDE)),
                                                      new BigDecimal((String) geopoi.getSource().get(GeoPoiFields.RADIUS))));
            }
        }
        
        return geoLocations;
    }
    
    
    //
    // Private methods
    //
    
    private SearchResult getAudienceFileLinks(Long audienceId) {
       // Search expression using audience Id 
       List<SearchExpression> searchExpressions = new ArrayList<>();
       searchExpressions.add(new SimpleSearchExpression(FileLinkFields.AUDIENCE_ID, audienceId.toString()));
       
       // Query all files
       SearchResult fileLinks = elasticSearchService.search(aeIndex, AudienceEngineIndexTypes.FILE_LINK, searchExpressions);
       if (fileLinks.getHits()!=null){
           if (fileLinks.getHits().size()<fileLinks.getTotalHits()){
               com.byyd.elasticsearch.model.Pagination pagination = new com.byyd.elasticsearch.model.Pagination(0, fileLinks.getTotalHits().intValue());
               fileLinks = elasticSearchService.search(aeIndex, AudienceEngineIndexTypes.FILE_LINK, searchExpressions, null, pagination);
           }
       }
       
       // Get assigned file Ids from audience engine index
       return fileLinks;
    }
    
    private SearchResult getFileFromAudienceFileLinks(SearchResult audienceFileLinks, List<String> filesIdsToAdd, Pagination pagination) {
        // Build search expression
        List<SearchExpression> fileIdsSearchExpression = new ArrayList<SearchExpression>(audienceFileLinks.getHits().size());
        for (Hit hit : audienceFileLinks.getHits()){
            fileIdsSearchExpression.add(new SimpleSearchExpression("_id", hit.getSource().get(FileLinkFields.FILE_ID).toString()));
        }
        if (filesIdsToAdd!=null){
            for (String id : filesIdsToAdd){
                fileIdsSearchExpression.add(new SimpleSearchExpression("_id", id));
            }
        }
        List<SearchExpression> searchExpression = new ArrayList<>(1);
        searchExpression.add(new MultipleSearchExpression(fileIdsSearchExpression, SearchLogicalOperator.OR));
        
        // Sortings
        List<SortingInfo> sortings = getSortingsInfo(pagination);
        
        // Pagination
        com.byyd.elasticsearch.model.Pagination esPagination = getESPagination(pagination);
        
        return elasticSearchService.search(fmIndex, FileMoverIndexTypes.FILE, searchExpression, sortings, esPagination);
    }
    
    private List<SortingInfo> getSortingsInfo(Pagination pagination) {
        List<SortingInfo> sortings = new ArrayList<>();
        
        if ((pagination!=null)&&(pagination.getSorting()!=null)){
           for(SortOrder sortOrder : pagination.getSorting().getSortOrders()){
               SortingOrder order = (sortOrder.getDirection()==Direction.ASC)? SortingOrder.ASC : SortingOrder.DESC; 
               sortings.add(new SortingInfo(sortOrder.getFieldName(), order));
           }
        }
        
        return sortings;
    }
    
    private com.byyd.elasticsearch.model.Pagination getESPagination(Pagination pagination) {
        com.byyd.elasticsearch.model.Pagination esPagination = null;
        if (pagination!=null){
            esPagination = new com.byyd.elasticsearch.model.Pagination(pagination.getOffet(), pagination.getLimit());
        }
        return esPagination;
    }
    
    private MuidSessionModel getMUIDInformation(SearchResult audienceFileLinks, String fileId) {
        // Search fileLink for the file id passed as argument
        Hit fileLink = searchFileLink(audienceFileLinks, fileId);
        
        // Call MUID to retrieve session information
        MuidSessionModel muidSessionModel = null;
        if (fileLink!=null){
            String muidSessionId = (String) fileLink.getSource().get(FileLinkFields.SESSION_ID); 
            muidSessionModel = muidNotificationDao.inboundCheckProgress(new BigDecimal(muidSessionId));
        }
        return muidSessionModel;
    }
    
    private Hit searchFileLink(SearchResult audienceFileLinks, String fileId) {
        Hit fileLink = null;
        for(int pos=0; (pos<audienceFileLinks.getHits().size() && fileLink==null) ; pos++){
            if (audienceFileLinks.getHits().get(pos).getSource().get(FileLinkFields.FILE_ID).equals(fileId)){
                fileLink = audienceFileLinks.getHits().get(pos);
            }
        }
        return fileLink;
    }
    
    private List<String> getAssignedFilesIds(Long audienceId) {
        List<String> assignedFileIds = new ArrayList<String>();
        
        // Get all files linked to the audience
        SearchResult audienceFileLinks = getAudienceFileLinks(audienceId);
        
        for(Hit fileLink : audienceFileLinks.getHits()){
            assignedFileIds.add(fileLink.getSource().get(FileLinkFields.FILE_ID).toString());
        }
        
        return assignedFileIds;
    }

    private List<SearchExpression> getUnassignedFilesSearchExpression(FileType fileType, String externalCompanyId, String externalAdvertiserId, List<String> removeFilesId) {
        String esFileType = (fileType == FileType.DEVICES ? com.byyd.elasticsearch.model.filemover.File.FileType.DEVICES.getType() : com.byyd.elasticsearch.model.filemover.File.FileType.GEOPOINTS.getType());
        List<SearchExpression> searchExpressions = new ArrayList<>();
        searchExpressions.add(new SimpleSearchExpression(FileFields.TYPE, esFileType));
        searchExpressions.add(new SimpleSearchExpression(FileFields.COMPANY_ID, externalCompanyId));
        searchExpressions.add(new SimpleSearchExpression(FileFields.ADVERTISER_ID, externalAdvertiserId));
        for (String fileId : removeFilesId){
            searchExpressions.add(new SimpleSearchExpression("_id", fileId, SearchFieldOperator.NOT_EQUAL));
        }
        return searchExpressions;
    }
}
