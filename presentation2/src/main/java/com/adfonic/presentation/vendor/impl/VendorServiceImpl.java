package com.adfonic.presentation.vendor.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.adfonic.domain.Vendor;
import com.adfonic.dto.devicegroup.DeviceGroupDto;
import com.adfonic.dto.publication.platform.PlatformDto;
import com.adfonic.dto.vendor.VendorInfoDto;
import com.adfonic.presentation.util.DtoToDomainMapperUtils;
import com.adfonic.presentation.util.GenericServiceImpl;
import com.adfonic.presentation.vendor.VendorService;
import com.byyd.middleware.device.service.DeviceManager;

@Service("vendorService")
public class VendorServiceImpl extends GenericServiceImpl implements VendorService {

    @Autowired
    private DeviceManager deviceManager;

    @Override
    public List<VendorInfoDto> getVendorsByPlatformAndDeviceGroup(String vendorName, List<PlatformDto> platformsDto, DeviceGroupDto deviceGroupDto) {
        List<Vendor> entities = deviceManager.getVendorsByPlatformAndDeviceGroup( vendorName,
                DtoToDomainMapperUtils.mapPlatformsDtoToPlatforms(platformsDto, deviceManager),
                DtoToDomainMapperUtils.mapDeviceGroupDtoToDeviceGroup(deviceGroupDto, deviceManager));

        List<VendorInfoDto> vendors = new ArrayList<VendorInfoDto>();
        for (Vendor v : entities) {
            vendors.add(getObjectDto(VendorInfoDto.class, v));
        }

        return vendors;
    }

}
