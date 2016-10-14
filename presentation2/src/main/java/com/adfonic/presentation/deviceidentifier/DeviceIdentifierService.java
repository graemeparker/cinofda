package com.adfonic.presentation.deviceidentifier;

import java.util.Collection;

import com.adfonic.dto.deviceidentifier.DeviceIdentifierTypeDto;

public interface DeviceIdentifierService {
	public DeviceIdentifierTypeDto getDeviceIdentifierTypeBySystemName(final String value);
	public DeviceIdentifierTypeDto getDeviceIdentifierTypeById(final Long id);
	public Collection<DeviceIdentifierTypeDto> getDeviceIdentifierTypes() ;
}
