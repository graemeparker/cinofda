package com.byyd.middleware.audience.filter;

import com.adfonic.domain.DMPVendor;
import com.byyd.middleware.iface.dao.LikeSpec;

public class DMPAttributeFilter {

    private DMPVendor dmpVendor;
    private String name;
    private LikeSpec nameLikeSpec;
    private boolean nameCaseSensitive;

    public DMPAttributeFilter setName(String name, boolean nameCaseSensitive) {
        return this.setName(name, null, nameCaseSensitive);
    }
    
    public DMPAttributeFilter setName(String name, LikeSpec nameLikeSpec, boolean nameCaseSensitive) {
        this.name = name;
        this.nameLikeSpec = nameLikeSpec;
        this.nameCaseSensitive = nameCaseSensitive;
        return this;
    }

    public DMPVendor getDMPVendor() {
        return dmpVendor;
    }

    public DMPAttributeFilter setDMPVendor(DMPVendor dmpVendor) {
        this.dmpVendor = dmpVendor;
        return this;
    }

    public String getName() {
        return name;
    }

    public LikeSpec getNameLikeSpec() {
        return nameLikeSpec;
    }

    public boolean isNameCaseSensitive() {
        return nameCaseSensitive;
    }
    

}
