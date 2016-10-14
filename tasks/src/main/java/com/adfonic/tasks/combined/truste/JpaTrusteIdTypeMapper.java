package com.adfonic.tasks.combined.truste;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adfonic.domain.DeviceIdentifierType;
import com.byyd.middleware.device.service.DeviceManager;

@Component
public class JpaTrusteIdTypeMapper implements TrusteIdTypeMapper {

    private final transient Logger logger = LoggerFactory.getLogger(getClass().getName());

    private Map<String, Long> map = new HashMap<>();

    @Autowired
    public JpaTrusteIdTypeMapper(DeviceManager deviceManager) {
        loadMappings(deviceManager);
    }

    @Override
    public long mapAdfonicIdType(String idName) {
        if (idName == null) {
            return 0;
        }

        String key = idName.toLowerCase();
        if (map.containsKey(key)) {
            return map.get(key);
        }

        logger.debug("unknown type: {}", idName);
        return 0;
    }

    private void loadMappings(DeviceManager deviceManager) {
        List<DeviceIdentifierType> allDeviceIdentifierTypes = deviceManager.getAllDeviceIdentifierTypes();
        logger.info("loading TrusteIdType mappings ");

        map.clear();
        for (DeviceIdentifierType type : allDeviceIdentifierTypes) {

            Long id = type.getId();
            String trusteIdType = type.getTrusteIdType();

            if (StringUtils.isNotBlank(trusteIdType)) {
                map.put(trusteIdType.toLowerCase(), id);
            }
        }
        logger.info("loaded TrusteIdType mappings ");

    }

}
