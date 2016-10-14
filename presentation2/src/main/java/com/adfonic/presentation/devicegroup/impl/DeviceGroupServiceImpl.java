package com.adfonic.presentation.devicegroup.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.DeviceGroup;
import com.adfonic.dto.devicegroup.DeviceGroupDto;
import com.adfonic.presentation.devicegroup.DeviceGroupService;
import com.adfonic.presentation.util.GenericServiceImpl;
import com.byyd.middleware.device.service.DeviceManager;

@Service("deviceGroup")
public class DeviceGroupServiceImpl extends GenericServiceImpl implements DeviceGroupService {
    @Autowired
    private DeviceManager deviceMaanger;

    @Transactional(readOnly=true)
    public DeviceGroupDto getDeviceGroupBySystemName(String name) {
        DeviceGroupDto deviceGroupDto = null;
        if (StringUtils.isNotBlank(name)) {
            DeviceGroup deviceGroup = deviceMaanger.getDeviceGroupBySystemName(name);
            if (deviceGroup != null) {
                deviceGroupDto = getObjectDto(DeviceGroupDto.class, deviceGroup);
            }
        }
        return deviceGroupDto;
    }

    @Transactional(readOnly=true)
    public DeviceGroupDto getDeviceGroupById(Long id) {
        DeviceGroupDto deviceGroupDto = null;
        if (id != null) {
            DeviceGroup deviceGroup = deviceMaanger.getDeviceGroupById(id);
            if (deviceGroup != null) {
                deviceGroupDto = getObjectDto(DeviceGroupDto.class, deviceGroup);
            }
        }
        return deviceGroupDto;
    }
}
