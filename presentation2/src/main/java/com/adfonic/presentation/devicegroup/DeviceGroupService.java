package com.adfonic.presentation.devicegroup;
import com.adfonic.dto.devicegroup.DeviceGroupDto;

public interface DeviceGroupService {
    DeviceGroupDto getDeviceGroupBySystemName(String name);
    DeviceGroupDto getDeviceGroupById(Long id);
}
