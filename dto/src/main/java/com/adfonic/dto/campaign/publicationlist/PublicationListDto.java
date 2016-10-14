package com.adfonic.dto.campaign.publicationlist;

import java.util.ArrayList;
import java.util.List;

import org.jdto.annotation.DTOCascade;
import org.jdto.annotation.Source;

import com.adfonic.dto.publication.PublicationInfoDto;

public class PublicationListDto extends PublicationListInfoDto {

    private static final long serialVersionUID = 1L;

    @DTOCascade
    @Source(value = "publications")
    private List<PublicationInfoDto> publicationsInfo;

    public List<PublicationInfoDto> getPublicationsInfo() {
        if (publicationsInfo == null) {
            publicationsInfo = new ArrayList<PublicationInfoDto>();
        }
        return publicationsInfo;
    }

    public void setPublicationsInfo(List<PublicationInfoDto> publicationsInfo) {
        this.publicationsInfo = publicationsInfo;
    }
}
