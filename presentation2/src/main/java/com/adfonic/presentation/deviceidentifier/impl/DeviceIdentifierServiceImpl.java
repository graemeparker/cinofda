package com.adfonic.presentation.deviceidentifier.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.adfonic.domain.DeviceIdentifierType;
import com.adfonic.dto.deviceidentifier.DeviceIdentifierTypeDto;
import com.adfonic.presentation.deviceidentifier.DeviceIdentifierService;
import com.adfonic.presentation.util.GenericServiceImpl;
import com.byyd.middleware.device.service.DeviceManager;

@Service("deviceIdentifierService")
public class DeviceIdentifierServiceImpl extends GenericServiceImpl implements DeviceIdentifierService {
	
	@Autowired
	private DeviceManager deviceManager;
	
	public Collection<DeviceIdentifierTypeDto> getDeviceIdentifierTypes() {
		List<DeviceIdentifierType> list = deviceManager.getAllNonHiddenDeviceIdentifierTypes();
		Collection<DeviceIdentifierTypeDto> col = getList(DeviceIdentifierTypeDto.class, list);
        return col;
    }
	
	public DeviceIdentifierTypeDto getDeviceIdentifierTypeBySystemName(String value) {
		DeviceIdentifierType source = deviceManager.getDeviceIdentifierTypeBySystemName(value);
		return getObjectDto(DeviceIdentifierTypeDto.class, source);
	}

	@Override
	public DeviceIdentifierTypeDto getDeviceIdentifierTypeById(Long id) {
		DeviceIdentifierType obj = deviceManager.getObjectById(DeviceIdentifierType.class, id);
		return getObjectDto(DeviceIdentifierTypeDto.class, obj);
	}

}
