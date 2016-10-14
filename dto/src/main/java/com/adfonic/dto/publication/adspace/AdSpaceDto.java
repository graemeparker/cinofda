package com.adfonic.dto.publication.adspace;

import java.util.ArrayList;
import java.util.List;

import org.jdto.annotation.DTOCascade;
import org.jdto.annotation.Source;
import org.springframework.util.CollectionUtils;

import com.adfonic.domain.AdSpace.ColorScheme;
import com.adfonic.dto.NameIdBusinessDto;
import com.adfonic.dto.format.FormatDto;

public class AdSpaceDto extends NameIdBusinessDto {

    private static final long serialVersionUID = 1L;

    @Source(value = "externalID")
    private String externalId;

    @DTOCascade
    @Source(value = "formats")
    private List<FormatDto> formats = new ArrayList<FormatDto>();

    @Source(value = "colorScheme")
    private ColorScheme colorScheme;

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public List<FormatDto> getFormats() {
        return formats;
    }

    public void setFormats(List<FormatDto> formats) {
        this.formats = formats;
    }

    public ColorScheme getColorScheme() {
        return colorScheme;
    }

    public void setColorScheme(ColorScheme colorScheme) {
        this.colorScheme = colorScheme;
    }

    public FormatDto getFormat() {
        if (!CollectionUtils.isEmpty(formats)) {
            return formats.get(0);
        }
        return null;
    }

    public void setFormat(FormatDto format) {
        formats.clear();
        formats.add(format);
    }

}
