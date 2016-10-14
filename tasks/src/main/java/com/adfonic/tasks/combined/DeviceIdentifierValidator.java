package com.adfonic.tasks.combined;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adfonic.domain.DeviceIdentifierType;
import com.byyd.middleware.device.service.DeviceManager;

@Component
public class DeviceIdentifierValidator {
	private Logger logger = LoggerFactory.getLogger(getClass());

	private final Map<Long, Pattern> validationPatternForTypeId = new HashMap<>();

    @Autowired
	public DeviceIdentifierValidator(DeviceManager deviceManager) {
    	loadMappings(deviceManager);
	}

	public boolean isDeviceIdValid(String deviceIdValue, long deviceTypeId) {
		
		
		if (StringUtils.isBlank(deviceIdValue)) {		    
		    return false;
		}

		Pattern validationPattern = validationPatternForTypeId.get(deviceTypeId);
		if (validationPattern == null) {
			logger.error("missing validation pattern for Type", deviceTypeId);
			return false;
		}
		
		if (validationPattern.matcher(deviceIdValue).matches()) {
			return true;
		}
		logger.debug("Device id " + deviceIdValue + " did not match validation pattern for type " + deviceTypeId);
		return false;
	}

	void loadMappings(DeviceManager deviceManager) {
		List<DeviceIdentifierType> allDeviceIdentifierTypes = deviceManager.getAllDeviceIdentifierTypes();

		for (DeviceIdentifierType type : allDeviceIdentifierTypes) {

			Long id = type.getId();
			String validationRegex = type.getValidationRegex();

			if (StringUtils.isNotBlank(validationRegex)) {
				try {
					Pattern validationPattern = Pattern.compile(validationRegex);
					validationPatternForTypeId.put(id, validationPattern);
				} catch (PatternSyntaxException e) {
					logger.error("error compiling pattern", e);
				}
			}
		}
	}
}
