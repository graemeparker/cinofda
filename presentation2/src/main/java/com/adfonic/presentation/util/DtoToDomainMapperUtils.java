package com.adfonic.presentation.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.adfonic.domain.DeviceGroup;
import com.adfonic.domain.Platform;
import com.adfonic.dto.devicegroup.DeviceGroupDto;
import com.adfonic.dto.publication.platform.PlatformDto;
import com.byyd.middleware.device.service.DeviceManager;

/**
 * Defines helper mapper functions between Dto's and Domain entities
 */
public class DtoToDomainMapperUtils {

    public static List<Platform> mapPlatformsDtoToPlatforms(List<PlatformDto> platformsDto, DeviceManager deviceManager) {
        List<Platform> platforms = new ArrayList<Platform>(0);
        if (!CollectionUtils.isEmpty(platformsDto)) {
            for (PlatformDto p : platformsDto) {
                Platform platform = deviceManager.getPlatformById(p.getId());
                platforms.add(platform);
            }
        }
        return platforms;
    }

    public static DeviceGroup mapDeviceGroupDtoToDeviceGroup(DeviceGroupDto deviceGroupDto, DeviceManager deviceManager) {
        DeviceGroup deviceGroup = null;
        if (deviceGroupDto != null) {
            deviceGroup = deviceManager.getDeviceGroupById(deviceGroupDto.getId());
        }
        return deviceGroup;
    }

}
