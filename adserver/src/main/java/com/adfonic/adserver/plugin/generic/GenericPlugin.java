package com.adfonic.adserver.plugin.generic;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.stereotype.Component;

import com.adfonic.adserver.ProxiedDestination;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.TimeLimit;
import com.adfonic.adserver.plugin.AbstractPlugin;
import com.adfonic.adserver.plugin.PluginProxiedDestination;
import com.adfonic.domain.cache.dto.adserver.AdserverPluginDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.dto.adserver.creative.PluginCreativeInfo;
import com.adfonic.util.ThreadLocalRandom;

/**
 * Generic exchange/plugin
 * 
 * @author Graeme
 * 
 */
@Component
public class GenericPlugin extends AbstractPlugin {
    
    private static final transient Logger LOG = Logger.getLogger(GenericPlugin.class.getName());

    /** Serializable */
    @SuppressWarnings("unused")
    private static final long serialVersionUID = 1L;

    /**
     * Generate the Ad request/response.
     */
    protected ProxiedDestination doGenerateAd(AdSpaceDto adSpace, CreativeDto creative, AdserverPluginDto adserverPlugin, PluginCreativeInfo pluginCreativeInfo, TargetingContext context, TimeLimit timeLimit) throws Exception {

        String clickUrl = pluginCreativeInfo.getParameterRequired("click");
        String imageUrl = pluginCreativeInfo.getParameterRequired("image");
        
        String cachebuster = pluginCreativeInfo.getParameterOptional("cachebuster");
        if (cachebuster != null && cachebuster.length() > 0){
            String cb = String.valueOf(ThreadLocalRandom.getRandom().nextInt(Integer.MAX_VALUE));
            clickUrl += "&cachebuster=";
            clickUrl += cb;
            imageUrl += "&cachebuster=";
            imageUrl += cb;
        }
        
        String cbStr = pluginCreativeInfo.getParameterOptional("cb");
        if (cbStr != null && cbStr.length() > 0){
            String cb = String.valueOf(ThreadLocalRandom.getRandom().nextInt(Integer.MAX_VALUE));
            clickUrl += "&cb=";
            clickUrl += cb;
            imageUrl += "&cb=";
            imageUrl += cb;
        }
        
        String ord = pluginCreativeInfo.getParameterOptional("ord");
        if (ord != null && ord.length() > 0){
            String cb = String.valueOf(ThreadLocalRandom.getRandom().nextInt(Integer.MAX_VALUE));
            clickUrl += ";ord=";
            clickUrl += cb;
            imageUrl += ";ord=";
            imageUrl += cb;
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("GenericPlugin: click =" + clickUrl);
            LOG.fine("GenericPlugin: image =" + imageUrl);
        }
        
        PluginProxiedDestination pd = new PluginProxiedDestination(adserverPlugin, creative, clickUrl);
        
        Map<String,String> component = new LinkedHashMap<String,String>();
        component.put("url", imageUrl);
        pd.getComponents().put("image", component);
        return pd;
    }
}
