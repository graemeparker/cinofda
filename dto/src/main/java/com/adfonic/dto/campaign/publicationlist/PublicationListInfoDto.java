package com.adfonic.dto.campaign.publicationlist;

import org.jdto.annotation.Source;

import com.adfonic.dto.NameIdBusinessDto;

public class PublicationListInfoDto extends NameIdBusinessDto {

    private static final long serialVersionUID = 1L;

    @Source(value = "whiteList")
    private Boolean whiteList;

    public Boolean getWhiteList() {
        return whiteList;
    }

    public void setWhiteList(Boolean whiteList) {
        this.whiteList = whiteList;
    }
}
