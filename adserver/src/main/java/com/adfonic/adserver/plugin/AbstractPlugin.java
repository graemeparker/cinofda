package com.adfonic.adserver.plugin;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;

import com.adfonic.adserver.DisplayTypeUtils;
import com.adfonic.adserver.ProxiedDestination;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.TimeLimit;
import com.adfonic.domain.cache.dto.adserver.AdserverPluginDto;
import com.adfonic.domain.cache.dto.adserver.ComponentDto;
import com.adfonic.domain.cache.dto.adserver.ContentSpecDto;
import com.adfonic.domain.cache.dto.adserver.DisplayTypeDto;
import com.adfonic.domain.cache.dto.adserver.FormatDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.dto.adserver.creative.PluginCreativeInfo;

/**
 * Abstract base class for plugin implementations
 */
public abstract class AbstractPlugin implements Plugin {
    private static final transient Logger LOG = Logger.getLogger(AbstractPlugin.class.getName());

    @Autowired
    private DisplayTypeUtils displayTypeUtils;
    @Autowired
    private PluginHttpManager pluginHttpManager;

    /** {@inheritDoc} */
    @Override
    public ProxiedDestination generateAd(AdSpaceDto adSpace, CreativeDto creative, AdserverPluginDto adserverPlugin, PluginCreativeInfo pluginCreativeInfo,
            TargetingContext context, TimeLimit timeLimit) throws Exception {
        ProxiedDestination pd = doGenerateAd(adSpace, creative, adserverPlugin, pluginCreativeInfo, context, timeLimit);

        FormatDto format = context.getDomainCache().getFormatById(creative.getFormatId());

        // Make sure the format gets set if it wasn't set already
        if (pd.getFormat() == null) {
            pd.setFormat(format.getSystemName());
        }

        // Validate the ProxiedDestination to make sure it has the components
        // we expect it to, all required attributes, etc.
        if ("text".equals(format.getSystemName())) {
            // We expect a "text" component
            if (!pd.getComponents().containsKey("text")) {
                throw new PluginException("No \"text\" component from " + adserverPlugin.getSystemName() + ", creative.format=" + format.getSystemName());
            }
        } else {
            // We expect an "image" component
            Map<String, String> image = pd.getComponents().get("image");
            if (image == null) {
                throw new PluginException("No \"image\" component from " + adserverPlugin.getSystemName() + ", creative.format=" + format.getSystemName());
            }

            // Ensure that image width/height attributes are both set
            if (!image.containsKey("width") || !image.containsKey("height")) {
                // Look up the correct DisplayTypeDto for the given device
                int displayTypeIndex = displayTypeUtils.getDisplayTypeIndex(format, context);
                if (displayTypeIndex == -1) {
                    LOG.warning("No DisplayTypeDto index for Format=" + format.getSystemName() + ", defaulting to 0");
                    displayTypeIndex = 0;
                }
                DisplayTypeDto displayType = format.getDisplayTypes().get(displayTypeIndex);
                for (ComponentDto component : format.getComponents()) {
                    if ("image".equals(component.getSystemName())) {
                        ContentSpecDto contentSpec = component.getContentSpec(displayType);
                        if (contentSpec != null) {
                            Map<String, String> props = contentSpec.getManifestProperties();
                            if (props.containsKey("width")) {
                                image.put("width", props.get("width"));
                            }
                            if (props.containsKey("height")) {
                                image.put("height", props.get("height"));
                            }
                            if (LOG.isLoggable(Level.FINE)) {
                                LOG.fine("Setting width=" + props.get("width") + ", height=" + props.get("height") + " for Format=" + format.getSystemName() + ", DisplayType="
                                        + displayType.getSystemName() + ", ContentSpec.id=" + contentSpec.getId());
                            }
                            break; // found what we were looking for
                        }
                    }
                }
            }
        }

        return pd;
    }

    /**
     * Subclasses must implement this to generate an ad and its components
     * @param adSpace the AdSpace for which the ad is being generated
     * @param creative the Creative placeholder that represents this plugin
     * @param adserverPlugin the respective AdserverPlugin
     * @param info the creative-specific config for the plugin
     * @param context the targeting context
     * @param timeLimit the TimeLimit in effect, if applicable
     * @return a ProxiedDestination with the various ad components set
     * @throws Exception if anything goes wrong
     */
    protected abstract ProxiedDestination doGenerateAd(AdSpaceDto adSpace, CreativeDto creative, AdserverPluginDto adserverPlugin, PluginCreativeInfo pluginCreativeInfo,
            TargetingContext context, TimeLimit timeLimit) throws Exception;

    protected final PluginHttpManager getPluginHttpManager() {
        return pluginHttpManager;
    }

}
