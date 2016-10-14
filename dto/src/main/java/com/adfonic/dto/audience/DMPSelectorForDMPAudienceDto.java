package com.adfonic.dto.audience;

import org.jdto.annotation.DTOCascade;
import org.jdto.annotation.Source;

public class DMPSelectorForDMPAudienceDto extends DMPSelectorDto {

    private static final long serialVersionUID = 1L;

    @DTOCascade
    @Source("dmpAttribute")
    private DMPAttributeDto dmpAttribute;

    public DMPAttributeDto getDmpAttribute() {
        return dmpAttribute;
    }

    public void setDmpAttribute(DMPAttributeDto dmpAttribute) {
        this.dmpAttribute = dmpAttribute;
    }

}
