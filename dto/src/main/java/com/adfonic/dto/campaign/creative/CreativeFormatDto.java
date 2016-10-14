package com.adfonic.dto.campaign.creative;

import org.jdto.annotation.DTOCascade;
import org.jdto.annotation.Source;

import com.adfonic.dto.NameIdBusinessDto;
import com.adfonic.dto.format.FormatDto;

public class CreativeFormatDto extends NameIdBusinessDto {

    private static final long serialVersionUID = 1L;

    @DTOCascade
    @Source(value = "format")
    private FormatDto format;

    public FormatDto getFormat() {
        return format;
    }

    public void setFormat(FormatDto format) {
        this.format = format;
    }
}
