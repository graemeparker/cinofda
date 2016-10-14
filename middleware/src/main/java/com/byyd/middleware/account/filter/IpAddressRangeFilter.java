package com.byyd.middleware.account.filter;

import com.adfonic.domain.Company;

public class IpAddressRangeFilter {
    
    private Company company;
    
    public Company getCompany() {
        return company;
    }
    public IpAddressRangeFilter setCompany(Company company) {
        this.company = company;
        return this;
    }

}
