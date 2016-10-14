package com.adfonic.presentation.publication.service;

import java.util.List;
import java.util.Map;

import com.adfonic.domain.AdfonicUser;
import com.adfonic.dto.publication.PublicationDto;
import com.adfonic.dto.publication.PublicationInfoDto;
import com.adfonic.dto.publication.enums.PublicationStatus;
import com.adfonic.dto.publication.platform.PlatformDto;
import com.adfonic.dto.publication.publicationtype.PublicationtypeDto;
import com.adfonic.dto.publication.search.PublicationSearchDto;
import com.adfonic.dto.publication.typeahead.PublicationTypeAheadDto;
import com.adfonic.dto.user.UserDTO;
import com.adfonic.presentation.publication.model.PublicationApprovalDetailModel;
import com.adfonic.presentation.publication.model.PublicationApprovalModel;

public interface PublicationService {

    public PublicationSearchDto getPublications(final PublicationSearchDto dto);

    public PublicationTypeAheadDto getPublicationWithNameForPublisher(final PublicationSearchDto dto);

    public PublicationTypeAheadDto getPublicationById(final PublicationSearchDto dto);

    public PublicationDto getPublicationById(final Long id);

    public PublicationDto getPublicationByExternalId(final String id);

    public Map<Long, PublicationInfoDto> getActivePublicationInfoByExternalIds(final List<String> ids);

    public List<PublicationTypeAheadDto> getPublicationsById(final String[] publicationsIds);

    public PlatformDto getPlatformById(final long id);

    public PlatformDto getPlatformByName(final String name);

    public List<PlatformDto> getAllPlatforms(final boolean hasAllObject);

    public List<PlatformDto> getAllOrderedPlatforms(final boolean hasAllObject);

    public PublicationDto save(PublicationDto dto) throws Exception;

    public List<PublicationtypeDto> getPublicationType(final PlatformDto platformDDto);

    public List<PublicationtypeDto> getPublicationType(String medium);

    public List<PublicationtypeDto> getPublicationType(final boolean hasAllObject);

    public PublicationtypeDto getPublicationTypeByName(final String name);

    public PublicationtypeDto getPublicationTypeBySystemName(final String systemName);

    public PublicationtypeDto getPublicationTypeById(final Long id);

    public void changePublicationStatus(List<Long> publicationIds, PublicationStatus pubStatus);

    public PublicationDto submit(PublicationDto dto, UserDTO userDto) throws Exception;

    // For publication Approval in admin
    
    public boolean savePublicationApprovalDetails(PublicationApprovalDetailModel dto, AdfonicUser adfonicUser, boolean assignedToUserChanged, boolean commentChanged);

    public void assignUserToPublication(PublicationApprovalModel dto, AdfonicUser adfonicUser, Long assignedToUserId);
    
    public void changePublicationStatusWithHistory(Long publicationId, PublicationStatus publicationStatus, AdfonicUser adfonicUser);
}
