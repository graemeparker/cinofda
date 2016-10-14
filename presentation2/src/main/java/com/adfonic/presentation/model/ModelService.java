package com.adfonic.presentation.model;

import java.util.Collection;
import java.util.List;

import com.adfonic.dto.devicegroup.DeviceGroupDto;
import com.adfonic.dto.model.ModelDto;
import com.adfonic.dto.publication.platform.PlatformDto;

public interface ModelService {

	public Collection<ModelDto> doQuery(String search,
			List<PlatformDto> platformsDto, 
			DeviceGroupDto deviceGroup);
	
	public ModelDto getModelByName(String name);
	public ModelDto getModelById(Long id);
	public List<ModelDto> getModelsByName(String modelName);     
	public List<ModelDto> getModelsByNameAndVendor(String vendorModelName);
	public List<ModelDto> getModelsByVendorNameAndPlatformAndDeviceGroup(String vendorName, List<PlatformDto> platformsDto, DeviceGroupDto deviceGroupDto);
	
}
