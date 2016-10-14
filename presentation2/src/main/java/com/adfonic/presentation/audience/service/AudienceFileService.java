package com.adfonic.presentation.audience.service;

import java.util.List;

import com.adfonic.dto.audience.AudienceFileDto;
import com.adfonic.presentation.audience.enums.FileType;
import com.adfonic.presentation.location.model.GeoLocationModel;
import com.byyd.middleware.iface.dao.Pagination;

public interface AudienceFileService {
    
    //
    // Audience Files methods
    //
    
    public Long getAudienceSize(FileType fileType, Long audienceId);
    public Long countAssignedFiles(Long audienceId);
    public List<String> getFileNamesFromAssignedFiles(Long audienceId);
    public List<AudienceFileDto> getAssignedFiles(FileType fileType, Long audienceId, List<String> filesIdsToAdd, Pagination pagination);
    public Long countUnassignedFiles(FileType fileType, Long audienceId, String externalCompanyId, String externalAdvertiserId, List<String> removeFilesId);
    public List<AudienceFileDto> getUnassignedFiles(FileType fileType, Long audienceId, String externalCompanyId, String externalAdvertiserId, List<String> filesIdsToRemove, Pagination pagination);
    
    //
    // Geopoint methods
    //
    public List<GeoLocationModel> getGeopointsFromAudience(Long audienceId, Integer limit);
}
