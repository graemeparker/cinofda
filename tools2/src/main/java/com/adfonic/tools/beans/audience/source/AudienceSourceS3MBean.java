package com.adfonic.tools.beans.audience.source;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.commons.lang.time.StopWatch;
import org.elasticsearch.ElasticsearchException;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.domain.FirstPartyAudience;
import com.adfonic.dto.audience.AudienceDto;
import com.adfonic.dto.audience.AudienceFileDto;
import com.adfonic.dto.audience.FirstPartyAudienceDto;
import com.adfonic.dto.user.UserDTO;
import com.adfonic.presentation.audience.enums.FileType;
import com.adfonic.presentation.audience.service.AudienceFileService;
import com.adfonic.presentation.audience.service.AudienceService;
import com.adfonic.presentation.audienceengine.exception.AudienceEngineApiException;
import com.adfonic.presentation.audienceengine.service.AudienceEngineApi;
import com.adfonic.presentation.company.CompanyService;
import com.adfonic.presentation.util.DateUtils;
import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.byyd.elasticsearch.model.filemover.FileFields;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.SortOrder.Direction;
import com.byyd.middleware.iface.dao.Sorting;

@Component
@Scope("view")
public class AudienceSourceS3MBean extends GenericAbstractBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final transient Logger LOGGER = LoggerFactory.getLogger(AudienceSourceS3MBean.class);
    private static final Long DEF_AUDIENCE_ID = 0L;

    private String companyExtId;
    private String advertiserExtId;
    private Long audienceId;
    private FileType fileType;
    private FastDateFormat fastDateFormat;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private AudienceService audienceService;
    
    @Autowired
    private AudienceFileService audienceFileService;
    
    @Autowired
    private AudienceEngineApi audienceEngineApi;
    

    // ////////////////////
    // Models for the view
    // ////////////////////

    // S3 Unassigned Cloud files
    private LazyDataModel<AudienceFileDto> s3UnassignedFilesLazyDataModel = null;
    private List<AudienceFileDto> selectedS3UnassignedFiles;
    private Integer totalUnassigned = 0;

    // S3 Assigned Cloud files
    private LazyDataModel<AudienceFileDto> s3AssignedFilesLazyDataModel = null;
    private List<AudienceFileDto> selectedS3AssignedFiles;
    private Integer totalFilesAssigned = 0;
    private Set<AudienceFileDto> addedAudienceFiles = new HashSet<AudienceFileDto>();
    
    // Keep information about elasticsearch health status
    Boolean elasticHealthy = Boolean.TRUE;

    @Override
    @PostConstruct
    protected void init() throws Exception {
        UserDTO userDto = getUser();
        fastDateFormat = FastDateFormat.getInstance(DateUtils.getTimeStampFormat(), 
                                                    companyService.getTimeZoneForAdvertiser(userDto.getAdvertiserDto()), getLanguageSessionBean().getLocale());

        companyExtId = userDto.getCompany().getExternalID();
        advertiserExtId = userDto.getAdvertiserDto().getExternalID();
        audienceId = getAudienceId();
        
        if (!DEF_AUDIENCE_ID.equals(audienceId)) {
            fileType = determineFileType();
    
            createS3UnassignedFilesLazyDataModel();
            createS3AssignedFilesLazyDataModel();
        }
    }
    
    // ////////////////////
    // Action Handlers
    // ////////////////////

    public void assign() {
        if (!selectedS3UnassignedFiles.isEmpty()) {
            addedAudienceFiles.addAll(selectedS3UnassignedFiles);
        }
    }

    public void unassign() {
        if (!selectedS3AssignedFiles.isEmpty()) {
            addedAudienceFiles.removeAll(selectedS3AssignedFiles);
        }
    }
    
    public void notifyAudienceEngineWithAudienceCloudFiles() throws AudienceEngineApiException {
        // Cloud file assigns
        if (!addedAudienceFiles.isEmpty()) {
            Set<String> addedFileIds = new HashSet<String>(addedAudienceFiles.size());
            for(AudienceFileDto audienceFile : addedAudienceFiles){
                addedFileIds.add(audienceFile.getId());
            }
            audienceEngineApi.notifyAssignedFiles(audienceId, addedFileIds);
        }
    }
    
    // ////////////////////
    // Getters
    // ////////////////////
    
    public LazyDataModel<AudienceFileDto> getS3UnassignedFilesLazyDataModel() {
        return s3UnassignedFilesLazyDataModel;
    }

    public LazyDataModel<AudienceFileDto> getS3AssignedFilesLazyDataModel() {
        return s3AssignedFilesLazyDataModel;
    }
    
    public FastDateFormat getFastDateFormat() {
        return fastDateFormat;
    }
    
    public FileType getFileType() {
        return fileType;
    }
    
    public List<AudienceFileDto> getSelectedS3UnassignedFiles() {
        return selectedS3UnassignedFiles;
    }

    public void setSelectedS3UnassignedFiles(List<AudienceFileDto> selectedS3UnassignedFiles) {
        this.selectedS3UnassignedFiles = selectedS3UnassignedFiles;
    }

    public List<AudienceFileDto> getSelectedS3AssignedFiles() {
        return selectedS3AssignedFiles;
    }

    public void setSelectedS3AssignedFiles(List<AudienceFileDto> selectedS3AssignedFiles) {
        this.selectedS3AssignedFiles = selectedS3AssignedFiles;
    }
    
    public Integer getTotalUnassigned() {
        return totalUnassigned;
    }

    public Integer getTotalFilesAssigned() {
        return totalFilesAssigned;
    }
    
    /**
     * Healthy check weather elastic is healthy so it can provide data
     */
    public Boolean isElasticHealthy() {
        return elasticHealthy;
    }
    

    // ////////////////////
    // Private methods
    // ////////////////////

    /**
     * Choose elastic file type filter based on selected audience type
     */
    private FileType determineFileType() {
        FirstPartyAudienceDto firstPartyAudience = getAudience().getFirstPartyAudience();
        if (firstPartyAudience != null) {
            return FirstPartyAudience.Type.UPLOAD.equals(firstPartyAudience.getType()) ? FileType.DEVICES : FileType.GEOPOINTS;
        }
        return FileType.DEVICES;
    }

    private Long getAudienceId() {
        AudienceDto audience = getAudience();
        return (audience != null) ? audience.getId() : DEF_AUDIENCE_ID;
    }

    private AudienceDto getAudience() {
        String encodedAudienceExtId = getAudienceNavigationBean().getEncodedId();
        if (encodedAudienceExtId != null) {
            try {
                return audienceService.getAudienceByExternalId(URLDecoder.decode(encodedAudienceExtId, "UTF-8"));
            } catch (UnsupportedEncodingException uee) {
                LOGGER.error("Error during decoding Audience external id: " + encodedAudienceExtId, uee);
            }
        }
        return null;
    }

    private void createS3UnassignedFilesLazyDataModel() {
        this.s3UnassignedFilesLazyDataModel = new LazyDataModel<AudienceFileDto>() {

            private static final long serialVersionUID = 1L;

            @Override
            public List<AudienceFileDto> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters) {
                
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();
                
                // Get pagination information
                Pagination pagination = new Pagination(first, pageSize, getSorting(sortField, sortOrder));
                
                // Get list of file ids to be removed from the query
                List<String> fileIdsToRemove = getAddedAudienceFileIds();
                
                // Get data
                int rowCount = 0;
                List<AudienceFileDto> unassignedFiles = null;
                try{
                    rowCount = audienceFileService.countUnassignedFiles(fileType, audienceId, companyExtId, advertiserExtId, fileIdsToRemove).intValue();
                    unassignedFiles = audienceFileService.getUnassignedFiles(fileType, audienceId, companyExtId, advertiserExtId, fileIdsToRemove, pagination);
                    elasticHealthy = Boolean.TRUE;
                }catch(ElasticsearchException ese){
                    LOGGER.error("Found an exception accessing elasticsearch data.", ese);
                    elasticHealthy = Boolean.FALSE;
                }
                
                totalUnassigned = rowCount;
                setRowCount(rowCount);
                
                stopWatch.stop();
                LOGGER.info("S3 Unassigned Files for audience " + audienceId + " loaded in " + stopWatch.getTime() + " ms");
                
                return unassignedFiles;
            }

            private Sorting getSorting(String sortField, SortOrder sortOrder) {
                Direction direction = (sortOrder == SortOrder.ASCENDING)? Direction.ASC : Direction.DESC;
                
                String sortFieldName = sortField;
                if (sortFieldName == null) {
                    sortFieldName = FileFields.NAME;
                }
                
                com.byyd.middleware.iface.dao.SortOrder mwSortOrder = new com.byyd.middleware.iface.dao.SortOrder(direction, sortFieldName);
                return new Sorting(mwSortOrder);
            }
        };
    }

    private void createS3AssignedFilesLazyDataModel() {
        this.s3AssignedFilesLazyDataModel = new LazyDataModel<AudienceFileDto>() {

            private static final long serialVersionUID = 1L;

            @Override
            public List<AudienceFileDto> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters) {
                
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();
                
                // Get pagination information
                Pagination pagination = new Pagination(first, pageSize, getSorting(sortField, sortOrder));
                
                // Get list of file ids to be removed from the query
                List<String> fileIdsToAdd = getAddedAudienceFileIds();
                
                // Get data
                int rowCount = 0;
                List<AudienceFileDto> assignedFiles = null;
                try{
                    rowCount = audienceFileService.countAssignedFiles(audienceId).intValue() + fileIdsToAdd.size();
                    assignedFiles = audienceFileService.getAssignedFiles(fileType, audienceId, fileIdsToAdd, pagination);
                    elasticHealthy = Boolean.TRUE;
                }catch(ElasticsearchException ese){
                    LOGGER.error("Found an exception accessing elasticsearch data.", ese);
                    elasticHealthy = Boolean.FALSE;
                }
                
                totalFilesAssigned = rowCount;
                setRowCount(rowCount);
                
                stopWatch.stop();
                LOGGER.info("S3 Assigned Files for audience " + audienceId + " loaded in " + stopWatch.getTime() + " ms");
                
                return assignedFiles;
            }

            private Sorting getSorting(String sortField, SortOrder sortOrder) {
                Direction direction = (sortOrder == SortOrder.ASCENDING)? Direction.ASC : Direction.DESC;
                
                String sortFieldName = sortField;
                if (sortFieldName == null) {
                    sortFieldName = FileFields.NAME;
                }
                
                com.byyd.middleware.iface.dao.SortOrder mwSortOrder = new com.byyd.middleware.iface.dao.SortOrder(direction, sortFieldName);
                return new Sorting(mwSortOrder);
            }
        };
        
        //Get initial row count when bean is loaded
        try{
            totalFilesAssigned = audienceFileService.countAssignedFiles(audienceId).intValue();
            elasticHealthy = Boolean.TRUE;
        }catch(ElasticsearchException ese){
            LOGGER.error("Found an exception accessing elasticsearch data.", ese);
            elasticHealthy = Boolean.FALSE;
        }
    }
    
    private List<String> getAddedAudienceFileIds(){
        List<String> fileIds = new ArrayList<>();
        for(AudienceFileDto audienceFile : addedAudienceFiles){
            fileIds.add(audienceFile.getId());
        }
        return fileIds;
    }
}
