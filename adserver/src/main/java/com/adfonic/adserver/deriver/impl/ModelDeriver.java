package com.adfonic.adserver.deriver.impl;

import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;
import com.adfonic.ddr.DdrService;
import com.adfonic.domain.cache.dto.adserver.ModelDto;

/** Derive a ModelDto domain object from the request */
@Component
public class ModelDeriver extends BaseDeriver {

    private static final transient Logger LOG = Logger.getLogger(ModelDeriver.class.getName());

    private final DdrService ddrService;

    @Autowired
    public ModelDeriver(DeriverManager deriverManager, DdrService ddrService) {
        super(deriverManager, TargetingContext.MODEL, TargetingContext.DEVICE_PROPERTIES, TargetingContext.DEVICE_IS_ROBOT_CHECKER_OR_SPAM);
        this.ddrService = ddrService;
    }

    @Override
    public Object getAttribute(String attribute, TargetingContext context) {
        if (TargetingContext.MODEL.equals(attribute)) {
            return deriveModel(context);
        } else if (TargetingContext.DEVICE_PROPERTIES.equals(attribute)) {
            return deriveModelProperties(context);
        } else if (TargetingContext.DEVICE_IS_ROBOT_CHECKER_OR_SPAM.equals(attribute)) {
            return deriveDeviceIsRobotCheckerOrSpam(context);
        } else {
            LOG.warning("Cannot derive attribute: " + attribute);
            return null;
        }
    }

    public ModelDto deriveModel(TargetingContext context) {
        // To derive the Model, we first derive (and store in the context)
        // all of the device properties.
        Map<String, String> props = context.getAttribute(TargetingContext.DEVICE_PROPERTIES);
        if (props == null) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Failed to derive model properties");
            }
            return null;
        }

        String deviceID = props.get("id");
        if (deviceID == null) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("No deviceID found in DDR properties");
            }
            return null;
        }

        ModelDto model = context.getDomainCache().getModelByExternalID(deviceID);
        if (model == null) {
            LOG.warning("Could not find ModelDto by externalID: " + deviceID);
            return null;
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Found Model.id=" + model.getId() + " with externalID=" + model.getExternalID());
        }
        return model;
    }

    public Map<String, String> deriveModelProperties(TargetingContext context) {
        // This is a hack that compensates for occasionally absent properties.
        // Sometimes we see usableDisplayWidth is missing, but displayWidth
        // is provided.  Since our DisplayTypeDto constraints rely on the
        // usableDisplayWidth property, we do our best here to make sure
        // it's available.
        Map<String, String> props = ddrService.getDdrProperties(context);
        if (props != null) {
            if (!props.containsKey("usableDisplayWidth") && props.containsKey("displayWidth")) {
                props.put("usableDisplayWidth", props.get("displayWidth"));
            }
            if (!props.containsKey("usableDisplayHeight") && props.containsKey("displayHeight")) {
                props.put("usableDisplayHeight", props.get("displayHeight"));
            }
            return props;
        } else {
            // DdrService sometimes returns null so caller can have more fun with handling it. Checkoway design!
            return Collections.EMPTY_MAP;
        }

    }

    public boolean deriveDeviceIsRobotCheckerOrSpam(TargetingContext context) {
        Map<String, String> props = context.getAttribute(TargetingContext.DEVICE_PROPERTIES);
        if (props == null) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Failed to derive model properties");
            }
            return false;
        }
        return isDevicePropertySet(props, "isRobot") || isDevicePropertySet(props, "isChecker") || isDevicePropertySet(props, "isSpam");
    }

    /**
     * Convenience method for seeing if a device property was set.
     * @return true if the given property is present and not equal to "0".
     */
    static boolean isDevicePropertySet(Map<String, String> props, String propertyName) {
        String value = props.get(propertyName);
        return ((value != null && !"0".equals(value)) && (value != null && !"false".equalsIgnoreCase(value)));
    }
}
