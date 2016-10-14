package com.adfonic.domain.cache.dto.datacollector.campaign;

import com.adfonic.domain.cache.dto.BusinessKeyDto;

public class AdvertiserDto extends BusinessKeyDto {
    private static final long serialVersionUID = 2L;

    private Long accountId;
    private CompanyDto company;

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public CompanyDto getCompany() {
        return company;
    }

    public void setCompany(CompanyDto company) {
        this.company = company;
    }

    @Override
    public String toString() {
        return "AdvertiserDto {" + getId() + ", accountId=" + accountId + ", company=" + company + "}";
    }

}
