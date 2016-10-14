package com.adfonic.webservices.dto.mapping;

import com.adfonic.domain.Vendor;

public class VendorConverter extends BaseReferenceEntityConverter<Vendor> {

    public VendorConverter() {
        super(Vendor.class, "name");
    }

    @Override
    public Vendor resolveEntity(String name) {
        return getDeviceManager().getVendorByName(name);
    }
}
