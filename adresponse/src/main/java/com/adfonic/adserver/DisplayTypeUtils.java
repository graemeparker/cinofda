package com.adfonic.adserver;

import java.util.List;

import com.adfonic.domain.cache.dto.adserver.DisplayTypeDto;
import com.adfonic.domain.cache.dto.adserver.FormatDto;

public interface DisplayTypeUtils {
    /**
     * Determine the index of the DisplayTypeDto in the Format's displayTypes
     * based on derived device properties
     */
    int getDisplayTypeIndex(FormatDto format, TargetingContext context);

    /**
     * Determine the respective DisplayTypeDto from the Format's displayTypes
     * based on derived device properties
     */
    DisplayTypeDto getDisplayType(FormatDto format, TargetingContext context, boolean defaultToFirstAvailable);

    /**
     * Determine the respective DisplayTypeDto from the Format's displayTypes
     * based on derived device properties
     */
    DisplayTypeDto getDisplayType(FormatDto format, TargetingContext context);

    /**
     * Get all matching DisplayTypeDtos from the Format's displayTypes based on
     * derived device properties
     */
    List<DisplayTypeDto> getAllDisplayTypes(FormatDto format, TargetingContext context);

    /**
     * This allows us to force a pre-derived DisplayTypeDto into the
     * TargetingContext. This is used by RTB at win notice time.
     */
    void setDisplayType(FormatDto format, TargetingContext context, DisplayTypeDto displayType);

}
