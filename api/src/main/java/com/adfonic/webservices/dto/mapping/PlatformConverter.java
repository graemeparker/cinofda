package com.adfonic.webservices.dto.mapping;

import com.adfonic.domain.Platform;

public class PlatformConverter extends BaseReferenceEntityConverter<Platform> {

    public PlatformConverter() {
        super(Platform.class, "systemName");
    }

    @Override
    public Platform resolveEntity(String systemName) {
        return getDeviceManager().getPlatformBySystemName(systemName);
    }
}
