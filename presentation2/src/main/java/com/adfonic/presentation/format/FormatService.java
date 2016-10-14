package com.adfonic.presentation.format;

import java.util.List;

import com.adfonic.dto.format.FormatDto;
import com.adfonic.dto.publication.publicationtype.PublicationtypeDto;


public interface FormatService {
    public List<FormatDto> getFormatDtos(final PublicationtypeDto platformDto);
    public FormatDto getFormatByName(final String name);
    public FormatDto getFormatById(final Long id);
    public FormatDto getFormatBySystemName(final String name);
    public FormatDto getFormat(int width,int height);
    public List<FormatDto> getAllFormats();
    public FormatDto getVideoFormat(int width, int height);
}

