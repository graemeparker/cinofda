package com.adfonic.presentation.vendor;

import java.util.List;

import com.adfonic.dto.devicegroup.DeviceGroupDto;
import com.adfonic.dto.publication.platform.PlatformDto;
import com.adfonic.dto.vendor.VendorInfoDto;

public interface VendorService {

    public List<VendorInfoDto> getVendorsByPlatformAndDeviceGroup(String vendorName, List<PlatformDto> platformsDto, DeviceGroupDto deviceGroupDto);

}
