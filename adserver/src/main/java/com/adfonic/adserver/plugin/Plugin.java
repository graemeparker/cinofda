package com.adfonic.adserver.plugin;

import com.adfonic.adserver.ProxiedDestination;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.TimeLimit;
import com.adfonic.domain.cache.dto.adserver.AdserverPluginDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.dto.adserver.creative.PluginCreativeInfo;

/** Interface for plugins that can generate ads using methods other than the
    usual stuff.
*/
public interface Plugin {
    /**
     * Generate an ad and its components
     * @param adSpace the AdSpace for which the ad is being generated
     * @param creative the Creative placeholder that represents this plugin
     * @param adserverPlugin the respective AdserverPlugin
     * @param info the creative-specific config for the plugin
     * @param context the targeting context
     * @param timeLimit the TimeLimit in effect, if applicable
     * @return a ProxiedDestination with the various ad components set
     * @throws Exception if anything goes wrong
     */
    ProxiedDestination generateAd(AdSpaceDto adSpace, CreativeDto creative, AdserverPluginDto adserverPlugin, PluginCreativeInfo info, TargetingContext context, TimeLimit timeLimit) throws Exception;
}
