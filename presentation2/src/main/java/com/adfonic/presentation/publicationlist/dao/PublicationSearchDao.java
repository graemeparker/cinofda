package com.adfonic.presentation.publicationlist.dao;

import java.util.List;

import com.adfonic.dto.campaign.publicationlist.PublicationForListDto;

public interface PublicationSearchDao {

    public Long getNumberOfRecordsForPublications(String searchString, int publicationType, List<Long> publicationIds);
   
    public List<PublicationForListDto> getPublications(String searchString, int publicationType, Long numRecords, Long start, List<Long> publicationIds);

}