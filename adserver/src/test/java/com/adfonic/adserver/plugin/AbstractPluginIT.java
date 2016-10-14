package com.adfonic.adserver.plugin;

import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jmock.Expectations;
import org.junit.Before;

import com.adfonic.adserver.DisplayTypeUtils;
import com.adfonic.adserver.DynamicProperties;
import com.adfonic.adserver.IconManager;
import com.adfonic.adserver.ProxiedDestination;
import com.adfonic.adserver.TimeLimit;
import com.adfonic.adserver.deriver.DeriverManager;
import com.adfonic.adserver.impl.AdResponseLogicImpl;
import com.adfonic.adserver.impl.TargetingContextImpl;
import com.adfonic.adserver.vhost.VhostManager;
import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.dto.adserver.AdserverPluginDto;
import com.adfonic.domain.cache.dto.adserver.ComponentDto;
import com.adfonic.domain.cache.dto.adserver.ContentSpecDto;
import com.adfonic.domain.cache.dto.adserver.DisplayTypeDto;
import com.adfonic.domain.cache.dto.adserver.FormatDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.dto.adserver.creative.DestinationDto;
import com.adfonic.domain.cache.dto.adserver.creative.PluginCreativeInfo;
import com.adfonic.domain.cache.ext.AdserverDomainCache;
import com.adfonic.geo.PostalCodeIdManager;
import com.adfonic.test.AbstractAdfonicTest;

public abstract class AbstractPluginIT extends AbstractAdfonicTest {
    private static final transient Logger LOG = Logger.getLogger(AbstractPluginIT.class.getName());

    private final PluginHttpManager pluginHttpManager;
    
    private DomainCache domainCache;
    private AdserverDomainCache adserverDomainCache;
    private DisplayTypeUtils displayTypeUtils;
    private AdResponseLogicImpl adResponseLogic;
    private IconManager iconManager;
    private VhostManager vhostManager;
    private DynamicProperties dProperties;

    protected AbstractPluginIT() {
        LOG.info("Creating PluginHttpManager");
        pluginHttpManager = new PluginHttpManager(-1, 10, 10, 1000, 2500);
    }

    @Before
    public void setupMockObjectsForPlugin() {
        domainCache = mock(DomainCache.class);
        adserverDomainCache = mock(AdserverDomainCache.class);
        displayTypeUtils = mock(DisplayTypeUtils.class);
        adResponseLogic = new AdResponseLogicImpl(displayTypeUtils, iconManager, vhostManager, dProperties);
    }

    /**
     * Optionally override this method if/as required
     */
    protected void setupAdserverPluginProperties(Map<String,String> properties) {
        // Nothing by default
    }
    
    /**
     * Optionally override this method if/as required
     */
    protected void setupTargetingContext(TargetingContextImpl targetingContext) {
        // Nothing by default
    }

    /**
     * Optionally override this method if/as required
     */
    protected void setupExpectations(Expectations expectations) {
        // Nothing by default
    }

    /**
     * Call this method to test your plugin
     * @param plugin the Plugin instance being tested
     * @param displayTypeSystem the systemName of the display type being tested (i.e. "xxl")
     * @param formatSystemName the systemName of the creative format (i.e. "banner")
     * @param pluginDestinationUrl the plugin:// style destination URL
     */
    protected final void testPlugin(Plugin plugin, String displayTypeSystemName, String formatSystemName, String pluginDestinationUrl) {
        AdserverPluginDto adserverPlugin = new AdserverPluginDto();
        setupAdserverPluginProperties(adserverPlugin.getProperties());
        
        DestinationDto destination = new DestinationDto();
        destination.setData(pluginDestinationUrl);

        final long formatId = randomLong();
        
        DisplayTypeDto displayType = new DisplayTypeDto();
        displayType.setSystemName(displayTypeSystemName);

        final FormatDto format = new FormatDto();
        format.setId(formatId);
        format.setSystemName(formatSystemName);
        format.getDisplayTypes().add(displayType);

        LOG.info("Testing with displayType=" + displayTypeSystemName + ", format=" + format.getSystemName());

        Matcher imageSizeMatcher = null;
        if ("banner".equals(formatSystemName)) {
            imageSizeMatcher = SIZE_PATTERN.matcher(BANNER_SIZE_MAP.get(displayTypeSystemName));
        } else if (SIZE_PATTERN.matcher(displayTypeSystemName).matches()) {
            imageSizeMatcher = SIZE_PATTERN.matcher(displayTypeSystemName);
        }
        if (imageSizeMatcher != null && imageSizeMatcher.find()) {
            ContentSpecDto contentSpec = new ContentSpecDto();
            contentSpec.getManifestProperties().put("width", imageSizeMatcher.group(1));
            contentSpec.getManifestProperties().put("height", imageSizeMatcher.group(2));
            
            ComponentDto component = new ComponentDto();
            component.setSystemName("image");
            component.getContentSpecMap().put(displayType, contentSpec);
            format.getComponents().add(component);
        }
        
        CreativeDto creative = new CreativeDto();
        creative.setPluginBased(true);
        creative.setDestination(destination);
        creative.setFormatId(formatId);

        PluginCreativeInfo pluginCreativeInfo = new PluginCreativeInfo(creative);

        DeriverManager deriverManager = new DeriverManager();
        // No derivers...instead let the subclass override setupTargetingContext
        // and call setAttribute with any attribute values the plugin requires.

        PostalCodeIdManager postalCodeIdManager = null; // not needed for plugins
        
        final TargetingContextImpl targetingContext = new TargetingContextImpl(domainCache, adserverDomainCache, deriverManager, postalCodeIdManager);
        setupTargetingContext(targetingContext);

        Expectations expectations = new Expectations() {{
            allowing (domainCache).getFormatById(formatId); will(returnValue(format));
            allowing (displayTypeUtils).getDisplayTypeIndex(format, targetingContext); will(returnValue(0));
        }};
        setupExpectations(expectations);
        expect(expectations);

        PublicationDto publication = new PublicationDto();
        publication.setExternalID(UUID.randomUUID().toString());
        
        AdSpaceDto adSpace = new AdSpaceDto();
        adSpace.setExternalID(UUID.randomUUID().toString());
        adSpace.setPublication(publication);
        adSpace.getFormatIds().add(formatId);

        TimeLimit timeLimit = null; // not needed for this test, impose no limit

        inject(plugin, "displayTypeUtils", displayTypeUtils);
        inject(plugin, "pluginHttpManager", pluginHttpManager);

        // The plugin may or may not have an "adResponseLogic" variable
        injectQuietly(plugin, "adResponseLogic", adResponseLogic);

        try {
            ProxiedDestination pd = plugin.generateAd(adSpace, creative, adserverPlugin, pluginCreativeInfo, targetingContext, timeLimit);
            LOG.info("generateAd call returned successfully, generated:\n" + pd);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to generate ad", e);
            fail();
        }
    }

    /**
     * Convenience method used to test a banner plugin with every possible
     * displayType variation (s, m, l, xl, xxl)
     */
    protected void testAllBannerVariations(Plugin plugin, String pluginDestinationUrl) {
        for (String displayTypeSystemName : new String[] { "s", "m", "l", "xl", "xxl" }) {
            testPlugin(plugin, displayTypeSystemName, "banner", pluginDestinationUrl);
        }
    }
    
    /**
     * Convenience method used to test a banner plugin a specific displayType variation (s, m, l, xl, xxl)
     */
    protected void testBanner(Plugin plugin, String pluginDestinationUrl, String displayTypeSystemName) {
            testPlugin(plugin, displayTypeSystemName, "banner", pluginDestinationUrl);
    }
    
    /**
     * Convenience method used to test a text banner plugin
     */
    protected void testTextBanner(Plugin plugin, String pluginDestinationUrl, String displayTypeSystemName) {
            testPlugin(plugin, displayTypeSystemName, "text", pluginDestinationUrl);
    }
    
    private static final Pattern SIZE_PATTERN = Pattern.compile("^[^\\d]*(\\d+)x(\\d+)$");
    
    @SuppressWarnings("serial")
	private static final Map<String,String> BANNER_SIZE_MAP = new HashMap<String,String>() {{
            put("s", "120x20");
            put("m", "168x28");
            put("l", "216x36");
            put("xl", "300x50");
            put("xxl", "320x50");
        }};
}
