package com.adfonic.ddr;

import java.util.Map;

/**
 * DDR service interface
 */
public interface DdrService {
    /**
     * Look for device properties for a given User-Agent
     */
    Map<String,String> getDdrProperties(String userAgent);

    /**
     * Look for device properties for a given HTTP request.  We use the
     * UserAgentAware interface instead of HttpServletRequest, since
     * direct access of HttpServletRequest may not be practical (i.e. in
     * adserver, where server-to-server calls proxy headers, we deal with
     * TargetingContext instead).
     * NOTE: if the supplied context is UserAgentAware, it can receive
     * effective User-Agent updates if the device is recognized using an
     * alternate header (i.e. X-Such-And-Such).
     * @param context the header-aware context
     */
    Map<String,String> getDdrProperties(HttpHeaderAware context);
}
