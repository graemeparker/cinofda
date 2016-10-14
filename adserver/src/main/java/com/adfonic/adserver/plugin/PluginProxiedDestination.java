package com.adfonic.adserver.plugin;

import com.adfonic.adserver.AbstractAdComponents;
import com.adfonic.adserver.ProxiedDestination;
import com.adfonic.domain.DestinationType;
import com.adfonic.domain.cache.dto.adserver.AdserverPluginDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;

/**
 * Generic proxied destination implementation that may be used by plugins.
 */
public class PluginProxiedDestination extends AbstractAdComponents implements ProxiedDestination {
    private static final long serialVersionUID = 4L;

    private final AdserverPluginDto adserverPlugin;

    /**
     * Constructor
     */
    public PluginProxiedDestination(AdserverPluginDto adserverPlugin, DestinationType destinationType, String destinationUrl) {
        this.adserverPlugin = adserverPlugin;
        setDestinationType(destinationType);
        setDestinationUrl(destinationUrl);
    }

    /**
     * Convenience constructor
     */
    public PluginProxiedDestination(AdserverPluginDto adserverPlugin, CreativeDto creative, String destinationUrl) {
        this(adserverPlugin, creative.getDestination().getDestinationType(), destinationUrl);
    }
    
    public AdserverPluginDto getAdserverPlugin() {
        return adserverPlugin;
    }

    @Override
    public String toString() {
        return "PluginProxiedDestination["
            + "adserverPlugin=" + adserverPlugin.getSystemName()
            + ",format=" + getFormat()
            + ",destinationType=" + getDestinationType()
            + ",destinationUrl=" + getDestinationUrl()
            + ",components=" + getComponents() + "]";
    }
}
