package com.adfonic.presentation.publication.adspace;

import com.adfonic.dto.publication.PublicationDto;
import com.adfonic.dto.publication.adspace.AdSpaceDto;

public interface AdSpaceService {
	public AdSpaceDto getAdSpaceById(final AdSpaceDto dto);
    public AdSpaceDto save(AdSpaceDto dto,PublicationDto publicationDto) throws Exception;
    public void delete(AdSpaceDto dto) throws Exception;
}
