package com.adfonic.dto.vendor;

import java.util.Set;

import org.jdto.annotation.DTOCascade;
import org.jdto.annotation.Source;

import com.adfonic.dto.model.ModelDto;

public class VendorDto extends VendorInfoDto {

    private static final long serialVersionUID = 1L;

    @DTOCascade
    @Source(value = "modelss")
    private Set<ModelDto> modelss;

    @DTOCascade
    @Source(value = "aliasess")
    private Set<String> aliasess;

    public Set<ModelDto> getModelss() {
        return modelss;
    }

    public void setModelss(Set<ModelDto> models) {
        this.modelss = models;
    }

    public Set<String> getAliasess() {
        return aliasess;
    }

    public void setAliasess(Set<String> aliases) {
        this.aliasess = aliases;
    }

}
