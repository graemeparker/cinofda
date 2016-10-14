package com.adfonic.dto.campaign.publicationlist;

import com.adfonic.dto.AbstractSearchDto;

public class PublicationInfoSearchForListDto extends AbstractSearchDto{

    private PublicationListInfoDto publicationListInfoDto;

    public PublicationInfoSearchForListDto() {
        super();
    }

    public PublicationInfoSearchForListDto(PublicationListInfoDto publicationListInfoDto, Integer first, Integer pageSize, String sortField, Boolean ascending) {
        super(first, pageSize, sortField, ascending);
        this.publicationListInfoDto = publicationListInfoDto;
    }

    public PublicationListInfoDto getPublicationListInfoDto() {
        return publicationListInfoDto;
    }

    public void setPublicationListInfoDto(PublicationListInfoDto publicationListInfoDto) {
        this.publicationListInfoDto = publicationListInfoDto;
    }
}
