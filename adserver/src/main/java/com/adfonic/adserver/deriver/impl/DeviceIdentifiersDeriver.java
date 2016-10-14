package com.adfonic.adserver.deriver.impl;

import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.DeviceIdentifierLogic;
import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;
import com.adfonic.domain.DeviceIdentifierType;
import com.adfonic.domain.cache.dto.adserver.DeviceIdentifierTypeDto;

/**
 * Derive device identifiers from the request
 */
@Component
public class DeviceIdentifiersDeriver extends BaseDeriver {

    private static final transient Logger LOG = Logger.getLogger(DeviceIdentifiersDeriver.class.getName());

    @Autowired
    public DeviceIdentifiersDeriver(DeriverManager deriverManager) {
        super(deriverManager, TargetingContext.DEVICE_IDENTIFIERS);
    }

    /**
     * This method derives a Map<Long,String>, where the keys are
     * DeviceIdentifierType ids, and the values are device identifier values in
     * their secure form. That is, if the publisher has supplied a given device
     * identifier, it will end up in the map, and if it's known to be non-secure
     * in raw form, we make it secure by doing a SHA1 on it.
     * 
     * This method also transforms r.id values into respective secure d.* values
     * in order to provide backward compatibility for publishers who still pass
     * us only r.id -- prior to them migrating to the new d.* methodology.
     */
    @Override
    public Object getAttribute(String attribute, TargetingContext context) {
        if (!TargetingContext.DEVICE_IDENTIFIERS.equals(attribute)) {
            LOG.warning("Cannot derive attribute: " + attribute);
            return null;
        }

        LinkedHashMap<Long, String> map = new LinkedHashMap<Long, String>();
        for (DeviceIdentifierTypeDto deviceIdentifierType : context.getDomainCache().getAllDeviceIdentifierTypes()) {
            String paramName = makeParameterName(deviceIdentifierType);
            String value = context.getAttribute(paramName);
            if (StringUtils.isNotBlank(value)) {
                //iOS ifa - should be uppercase, Android AAID an others - lowercase

                String normalized = DeviceIdentifierType.SYSTEM_NAME_IFA.equals(deviceIdentifierType.getSystemName()) ? value.toUpperCase() : value.toLowerCase();
                //DeviceIdentifierUtils.normalizeDeviceIdentifier(value, deviceIdentifierType.getSystemName());

                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine(paramName + "=" + normalized);
                }

                // Make sure it conforms to the validation regex if there is
                // one
                if (deviceIdentifierType.getValidationPattern() != null && !deviceIdentifierType.getValidationPattern().matcher(normalized).matches()) {
                    LOG.warning("Supplied value for " + paramName + " (" + normalized + ") doesn't match validation pattern: " + deviceIdentifierType.getValidationPattern());
                    continue;
                }

                map.put(deviceIdentifierType.getId(), normalized);
            }
        }

        if (MapUtils.isEmpty(map)) {
            // No device identifier(s) were specified, so let's see if the
            // publisher is perhaps using the old r.id=... form. Note that we're
            // explicitly using the "raw" form of r.id here (as opposed to the
            // secure form TargetingContext.SECURE_TRACKING_ID). It's critical
            // that we use the raw form in order to determine what it was (i.e.
            // whether it's a UDID or Android ID).
            String rawTrackingId = context.getAttribute(Parameters.TRACKING_ID);
            if (StringUtils.isNotEmpty(rawTrackingId)) {
                rawTrackingId = rawTrackingId.toLowerCase(); // force
                                                             // lowercase...see
                                                             // above

                // Try to determine if the tracking id is actually a device
                // identifier,
                // and if so, what its DeviceIdentifierType is.
                DeviceIdentifierTypeDto deviceIdentifierType = DeviceIdentifierLogic.determineDeviceIdentifierTypeFromRawTrackingId(rawTrackingId, context.getAdSpace(),
                        context.getDomainCache());
                if (deviceIdentifierType != null) {
                    // It's a device identifier...store it in the map
                    map = new LinkedHashMap<Long, String>();

                    // ignoring that logic, because of mad-2473, android and udid are no longer supported
                    map.put(deviceIdentifierType.getId(), rawTrackingId);
                }
            }
        }

        if (MapUtils.isNotEmpty(map)) {
            // Auto-promote device identifiers to other secure forms as
            // applicable
            DeviceIdentifierLogic.promoteDeviceIdentifiers(map, context.getDomainCache().getDeviceIdentifierTypeIdsBySystemName());
            // AF-1467 - Prune any blacklisted device identifiers
            DeviceIdentifierLogic.enforceBlacklist(map, context.getDomainCache());
        }

        return map;
    }

    /**
     * Construct the name of the HTTP parameter that the publisher would have
     * used to express the value of the given DeviceIdentifierType
     * 
     * @param deviceIdentifierType
     *            the given DeviceIdentifierType
     * @return the full parameter name such as "d.odin-1" or "d.openudid"
     */
    static String makeParameterName(DeviceIdentifierTypeDto deviceIdentifierType) {
        return Parameters.DEVICE_PREFIX + deviceIdentifierType.getSystemName(); // i.e.
                                                                                // "d.odin-1"
                                                                                // or
                                                                                // "d.openudid"
    }
}
