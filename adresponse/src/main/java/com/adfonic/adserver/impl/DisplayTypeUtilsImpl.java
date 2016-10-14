package com.adfonic.adserver.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.stereotype.Component;

import com.adfonic.adserver.AdSize;
import com.adfonic.adserver.DisplayTypeUtils;
import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.SystemName;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.domain.cache.dto.adserver.DisplayTypeDto;
import com.adfonic.domain.cache.dto.adserver.FormatDto;
import com.adfonic.util.ConstraintsHelper;

@Component
public class DisplayTypeUtilsImpl implements DisplayTypeUtils {

    private static final transient Logger LOG = Logger.getLogger(DisplayTypeUtilsImpl.class.getName());

    // Hack for efficiency by reducing the actual number of lookups
    public static final DisplayTypeDto NULL_DISPLAY_TYPE = new DisplayTypeDto();

    // invariant; needed because if setter not called first null can break for-each etc
    public static final List<DisplayTypeDto> NULL_DISPLAY_TYPE_LIST = Collections.emptyList();

    /** @{inheritDoc} */
    @Override
    public int getDisplayTypeIndex(FormatDto format, TargetingContext context) {
        DisplayTypeDto displayType = getDisplayType(format, context);
        if (displayType == null) {
            return 0; // Best we can do is default to the first available
        }
        int idx = 0;
        for (DisplayTypeDto fdt : format.getDisplayTypes()) {
            if (fdt.getSystemName().equals(displayType.getSystemName())) {
                return idx;
            }
            ++idx;
        }
        throw new RuntimeException("Resolved DisplayTypeDto is not among the Format's DisplayTypes!!!");
    }

    /** @{inheritDoc} */
    @Override
    public DisplayTypeDto getDisplayType(FormatDto format, TargetingContext context, boolean defaultToFirstAvailable) {
        DisplayTypeDto displayType = getDisplayType(format, context);
        if (displayType == null && format != null) {
            displayType = format.getDisplayTypes().get(0);
            if (LOG.isLoggable(Level.INFO)) {
                LOG.info("DisplayTypeDto can't be derived for Format=" + format.getSystemName() + ", defaulting to first: " + displayType.getSystemName());
            }
        }
        return displayType;
    }

    /** @{inheritDoc} */
    @Override
    public DisplayTypeDto getDisplayType(FormatDto format, TargetingContext context) {
        // AF-1165
        if (format == null) {
            if (LOG.isLoggable(Level.INFO)) {
                LOG.info("Format is null, that shouldn't have happened, is AdSpace.formats empty?");
            }
            return null;
        }

        // First see if we already figured it out for the given Format
        final String attribute = makeContextAttributeName(format);
        DisplayTypeDto displayType = context.getAttribute(attribute);
        if (displayType != null) {
            if (displayType == NULL_DISPLAY_TYPE) {
                return null;
            } else {
                return displayType;
            }
        }

        // See if the request specified an exact content spec (i.e. image size)
        String contentSpecManifest = context.getAttribute(Parameters.CONSTRAINTS);
        if (contentSpecManifest != null) {
            // If the caller specified exactly what size content they
            // need, we'll use that to determine the DisplayType
            displayType = context.getDomainCache().getDisplayType(format, contentSpecManifest);
            context.setAttribute(makeDisplayTypesAttribute(format), displayType != null ? Arrays.asList(displayType) : Collections.EMPTY_LIST);
        } else {
            // Otherwise we'll use device properties to determine the
            // most appropriate DisplayType
            Map<String, String> deviceProps = context.getAttribute(TargetingContext.DEVICE_PROPERTIES);
            if (deviceProps == null) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Could not derive device properties");
                }
            } else {
                List<DisplayTypeDto> displayTypeList = ConstraintsHelper.findAllMatches(format.getDisplayTypes(), new ConstraintsHelper.MapPropertySource(deviceProps));
                context.setAttribute(makeDisplayTypesAttribute(format), displayTypeList);

                if (!displayTypeList.isEmpty()) {
                    displayType = displayTypeList.get(0);
                }
            }
        }

        if (displayType != null) {
            context.setAttribute(attribute, displayType);
        } else {
            // Store "NULL_DISPLAY_TYPE" so that next time the same lookup is
            // done, we already have our answer (albeit null)
            context.setAttribute(attribute, NULL_DISPLAY_TYPE);
        }

        return displayType;
    }

    @Override
    public List<DisplayTypeDto> getAllDisplayTypes(FormatDto format, TargetingContext context) {
        final String displayTypesAttribute = makeDisplayTypesAttribute(format);

        List<DisplayTypeDto> displayTypeList = context.getAttribute(displayTypesAttribute);
        if (displayTypeList != null && !displayTypeList.isEmpty()) {
            return displayTypeList;
        }

        return NULL_DISPLAY_TYPE_LIST;
    }

    /** @{inheritDoc} */
    @Override
    public void setDisplayType(FormatDto format, TargetingContext context, DisplayTypeDto displayType) {
        context.setAttribute(makeContextAttributeName(format), displayType);
    }

    private static String makeContextAttributeName(FormatDto format) {
        return "DisplayType.byFormat." + format.getSystemName();
    }

    private static String makeDisplayTypesAttribute(FormatDto format) {
        return "DisplayTypes.byFormat." + format.getSystemName();
    }

    // Allow 300x50 banner to be returned also for 320x50 dimensions
    private static String[] A_XXL_XL = new String[] { SystemName.DISPLAY_TYPE_XXL, SystemName.DISPLAY_TYPE_XL };
    private static String[] A_XL = new String[] { SystemName.DISPLAY_TYPE_XL };
    private static String[] A_XLP = new String[] { SystemName.DISPLAY_TYPE_XLP };
    private static String[] A_L = new String[] { SystemName.DISPLAY_TYPE_L };
    private static String[] EMPTY = new String[0];

    //private static final int MAX_LIMIT_WIDTH = (int) (AdSize.XXL_WIDTH * 1.5);
    // note that "high" variation of banner exist out there - 75 pixel high
    //private static final int MAX_LIMIT_HEIGHT = (int) (AdSize.XLP_HEIGHT * 1.5);

    public static String[] getBannerDisplayTypeSystemName(int maxWidth, int maxHeight) {
        // Go from largest to smallest 
        // Maybe we should check some upper limit here as sanity check...
        if (maxWidth >= AdSize.XXL_WIDTH && maxHeight >= AdSize.XXL_HEIGHT) {
            return A_XXL_XL;
        } else if (maxWidth >= AdSize.XL_WIDTH && maxHeight >= AdSize.XL_HEIGHT) {
            if (maxHeight < AdSize.XLP_HEIGHT) {
                return A_XL; // Banner 320x50
            } else {
                return A_XLP; // German 300x150 wierdo
            }
        } else if (maxWidth >= AdSize.L_WIDTH && maxHeight >= AdSize.L_HEIGHT) {
            return A_L;
            /*
             * ignore smaller
              } else if (maxWidth >= AdSize.M_WIDTH && maxWidth >= AdSize.M_HEIGHT) {
              return SystemName.DISPLAY_TYPE_M;
              } else if (maxWidth >= AdSize.S_WIDTH && maxWidth >= AdSize.S_HEIGHT) {
              return SystemName.DISPLAY_TYPE_S;
              */
        } else {
            return EMPTY;
        }
    }

}