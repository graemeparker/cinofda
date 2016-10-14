package com.byyd.adsquare.v2;

import java.util.Date;

public class AmpCompany {

    private String companyId;
    private String name;
    private Date created;
    private Date lastUpdated;

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return "AmpCompany {companyId=" + companyId + ", name=" + name + ", created=" + created + ", lastUpdated=" + lastUpdated + "}";
    }

}
