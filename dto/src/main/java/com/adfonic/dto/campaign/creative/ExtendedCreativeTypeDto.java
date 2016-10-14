package com.adfonic.dto.campaign.creative;

import org.jdto.annotation.Source;

import com.adfonic.dto.NameIdBusinessDto;

public class ExtendedCreativeTypeDto extends NameIdBusinessDto {

    private static final long serialVersionUID = 2L;

    @Source(value = "useDynamicTemplates")
    private boolean useDynamicTemplates;

    public boolean isUseDynamicTemplates() {
        return useDynamicTemplates;
    }

    public void setUseDynamicTemplates(boolean useDynamicTemplates) {
        this.useDynamicTemplates = useDynamicTemplates;
    }
}
