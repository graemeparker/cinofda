package com.byyd.middleware.audience.filter;

import com.byyd.middleware.iface.dao.LikeSpec;

public class DMPVendorFilter {

    private String name;
    private LikeSpec nameLikeSpec;
    private boolean nameCaseSensitive;
    private Boolean restricted;
    private Boolean adminOnly;;
    
    public DMPVendorFilter setName(String name, boolean nameCaseSensitive) {
        return this.setName(name, null, nameCaseSensitive);
    }
    
    public DMPVendorFilter setName(String name, LikeSpec nameLikeSpec, boolean nameCaseSensitive) {
        this.name = name;
        this.nameLikeSpec = nameLikeSpec;
        this.nameCaseSensitive = nameCaseSensitive;
        return this;
    }
    
    public Boolean getRestricted() {
        return restricted;
    }

    public DMPVendorFilter setRestricted(Boolean restricted) {
        this.restricted = restricted;
        return this;
    }

    public Boolean getAdminOnly() {
		return adminOnly;
	}

	public DMPVendorFilter setAdminOnly(Boolean adminOnly) {
		this.adminOnly = adminOnly;
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
