package com.adfonic.adserver.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.adserver.AdEvent;
import com.adfonic.adserver.AdEventFactory;
import com.adfonic.adserver.AdResponseLogic;
import com.adfonic.adserver.AdSpaceUtils;
import com.adfonic.adserver.BackupLogger;
import com.adfonic.adserver.BlacklistedException;
import com.adfonic.adserver.DisplayTypeUtils;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.ImpressionService;
import com.adfonic.adserver.InvalidIpAddressException;
import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.PreProcessor;
import com.adfonic.adserver.SelectedCreative;
import com.adfonic.adserver.StatusChangeManager;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.TargetingContextFactory;
import com.adfonic.adserver.TargetingEngine;
import com.adfonic.adserver.TargetingEventListener;
import com.adfonic.adserver.TimeLimit;
import com.adfonic.adserver.impl.ClickUtils;
import com.adfonic.adserver.monitor.AdserverMonitor;
import com.adfonic.adserver.rtb.nativ.ByydImp;
import com.adfonic.adserver.vhost.VhostManager;
import com.adfonic.domain.AdAction;
import com.adfonic.domain.AdSpace;
import com.adfonic.domain.Publication;
import com.adfonic.domain.UnfilledReason;
import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.dto.adserver.ComponentDto;
import com.adfonic.domain.cache.dto.adserver.DisplayTypeDto;
import com.adfonic.domain.cache.dto.adserver.FormatDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;
import com.adfonic.domain.cache.dto.adserver.creative.AssetDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.ext.AdserverDomainCache;
import com.adfonic.test.AbstractAdfonicTest;
import com.adfonic.test.AnyCapturingMatcher;

@SuppressWarnings("serial")
public class TestStaticTagController extends AbstractAdfonicTest {
    private TargetingContextFactory targetingContextFactory;
    private PreProcessor preProcessor;
    private TargetingEngine targetingEngine;
    private AdEventFactory adEventFactory;
    private BackupLogger backupLogger;
    private AdResponseLogic adResponseLogic;
    private AdSpaceUtils adSpaceUtils;
    private DisplayTypeUtils displayTypeUtils;
    private StatusChangeManager statusChangeManager;
    private ClickUtils clickUtils;
    private VhostManager vhostManager;
    private ImpressionService impressionService;
    private StaticTagController staticTagController;
    private TargetingContext targetingContext;
    private DomainCache domainCache;
    private AdserverDomainCache adserverDomainCache;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private String scheme;
    private String serverName;
    private int serverPort;
    private String contextPath;
    private AdserverMonitor adserverMonitor;

    @Before
    public void runBeforeEachTest() {
        targetingContextFactory = mock(TargetingContextFactory.class);
        preProcessor = mock(PreProcessor.class);
        targetingEngine = mock(TargetingEngine.class);
        adEventFactory = mock(AdEventFactory.class);
        backupLogger = mock(BackupLogger.class);
        adResponseLogic = mock(AdResponseLogic.class);
        adSpaceUtils = mock(AdSpaceUtils.class);
        displayTypeUtils = mock(DisplayTypeUtils.class);
        statusChangeManager = mock(StatusChangeManager.class);
        clickUtils = mock(ClickUtils.class);
        vhostManager = mock(VhostManager.class);
        impressionService = mock(ImpressionService.class);
        adserverMonitor = new AdserverMonitor();

        staticTagController = new StaticTagController();
        inject(staticTagController, "targetingContextFactory", targetingContextFactory);
        inject(staticTagController, "preProcessor", preProcessor);
        inject(staticTagController, "targetingEngine", targetingEngine);
        inject(staticTagController, "adEventFactory", adEventFactory);
        inject(staticTagController, "backupLogger", backupLogger);
        inject(staticTagController, "adResponseLogic", adResponseLogic);
        inject(staticTagController, "adSpaceUtils", adSpaceUtils);
        inject(staticTagController, "displayTypeUtils", displayTypeUtils);
        inject(staticTagController, "statusChangeManager", statusChangeManager);
        inject(staticTagController, "clickUtils", clickUtils);
        inject(staticTagController, "vhostManager", vhostManager);
        inject(staticTagController, "impressionService", impressionService);
        inject(staticTagController, "adserverMonitor", adserverMonitor);

        targetingContext = mock(TargetingContext.class);
        domainCache = mock(DomainCache.class);
        adserverDomainCache = mock(AdserverDomainCache.class);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        scheme = "http";
        serverName = randomHostName();
        serverPort = randomInteger(65535);
        contextPath = "/" + randomAlphaNumericString(10);
    }

    @Test
    public void testResolveImageAsset01_null_displayType() {
        final CreativeDto creative = mock(CreativeDto.class);
        final long formatId = randomLong();
        final FormatDto format = mock(FormatDto.class);

        expect(new Expectations() {
            {
                ignoring(creative).getId();
                ignoring(format).getSystemName();
                allowing(targetingContext).getDomainCache();
                will(returnValue(domainCache));
                allowing(creative).getFormatId();
                will(returnValue(formatId));
                allowing(domainCache).getFormatById(formatId);
                will(returnValue(format));
                allowing(displayTypeUtils).getDisplayType(format, targetingContext);
                will(returnValue(null));
            }
        });

        assertNull(staticTagController.resolveImageAsset(creative, targetingContext));
    }

    @Test(expected = IllegalStateException.class)
    public void testResolveImageAsset02_no_image_component() {
        final CreativeDto creative = mock(CreativeDto.class);
        final long formatId = randomLong();
        final FormatDto format = mock(FormatDto.class);
        final DisplayTypeDto displayType = mock(DisplayTypeDto.class);

        expect(new Expectations() {
            {
                ignoring(creative).getId();
                ignoring(format).getSystemName();
                ignoring(displayType).getSystemName();
                allowing(targetingContext).getDomainCache();
                will(returnValue(domainCache));
                allowing(creative).getFormatId();
                will(returnValue(formatId));
                allowing(domainCache).getFormatById(formatId);
                will(returnValue(format));
                allowing(displayTypeUtils).getDisplayType(format, targetingContext);
                will(returnValue(displayType));
                allowing(domainCache).getComponentByFormatAndSystemName(format, StaticTagController.IMAGE_COMPONENT_SYSTEM_NAME);
                will(returnValue(null));
            }
        });

        staticTagController.resolveImageAsset(creative, targetingContext);
    }

    @Test
    public void testResolveImageAsset03_null_asset_no_fallback() {
        final CreativeDto creative = mock(CreativeDto.class);
        final long formatId = randomLong();
        final FormatDto format = mock(FormatDto.class);
        final DisplayTypeDto displayType = mock(DisplayTypeDto.class, "displayType");
        final long displayTypeId = uniqueLong("DisplayType.id");
        final ComponentDto imageComponent = mock(ComponentDto.class, "imageComponent");
        final long imageComponentId = randomLong();
        final DisplayTypeDto alternateDisplayType = mock(DisplayTypeDto.class, "alternateDisplayType");
        final long alternateDisplayTypeId = uniqueLong("DisplayType.id");
        final List<DisplayTypeDto> allDisplayTypes = new ArrayList<DisplayTypeDto>() {
            {
                add(displayType);
                add(alternateDisplayType);
            }
        };

        expect(new Expectations() {
            {
                ignoring(creative).getId();
                ignoring(format).getSystemName();
                ignoring(displayType).getSystemName();
                ignoring(alternateDisplayType).getSystemName();
                allowing(targetingContext).getDomainCache();
                will(returnValue(domainCache));
                allowing(creative).getFormatId();
                will(returnValue(formatId));
                allowing(domainCache).getFormatById(formatId);
                will(returnValue(format));
                allowing(displayTypeUtils).getDisplayType(format, targetingContext);
                will(returnValue(displayType));
                allowing(domainCache).getComponentByFormatAndSystemName(format, StaticTagController.IMAGE_COMPONENT_SYSTEM_NAME);
                will(returnValue(imageComponent));
                allowing(displayType).getId();
                will(returnValue(displayTypeId));
                allowing(imageComponent).getId();
                will(returnValue(imageComponentId));
                allowing(creative).getAsset(displayTypeId, imageComponentId);
                will(returnValue(null));
                oneOf(displayTypeUtils).getAllDisplayTypes(format, targetingContext);
                will(returnValue(allDisplayTypes));
                allowing(alternateDisplayType).getId();
                will(returnValue(alternateDisplayTypeId));
                oneOf(creative).getAsset(alternateDisplayTypeId, imageComponentId);
                will(returnValue(null));
            }
        });

        assertNull(staticTagController.resolveImageAsset(creative, targetingContext));
    }

    @Test
    public void testResolveImageAsset04_null_asset_with_positive_fallback() {
        final CreativeDto creative = mock(CreativeDto.class);
        final long formatId = randomLong();
        final FormatDto format = mock(FormatDto.class);
        final DisplayTypeDto displayType = mock(DisplayTypeDto.class, "displayType");
        final long displayTypeId = uniqueLong("DisplayType.id");
        final ComponentDto imageComponent = mock(ComponentDto.class, "imageComponent");
        final long imageComponentId = randomLong();
        final DisplayTypeDto alternateDisplayType = mock(DisplayTypeDto.class, "alternateDisplayType");
        final long alternateDisplayTypeId = uniqueLong("DisplayType.id");
        final List<DisplayTypeDto> allDisplayTypes = new ArrayList<DisplayTypeDto>() {
            {
                add(displayType);
                add(alternateDisplayType);
            }
        };
        final AssetDto asset = mock(AssetDto.class);

        expect(new Expectations() {
            {
                ignoring(asset).getId();
                ignoring(creative).getId();
                ignoring(format).getSystemName();
                ignoring(displayType).getSystemName();
                ignoring(alternateDisplayType).getSystemName();
                allowing(targetingContext).getDomainCache();
                will(returnValue(domainCache));
                allowing(creative).getFormatId();
                will(returnValue(formatId));
                allowing(domainCache).getFormatById(formatId);
                will(returnValue(format));
                allowing(displayTypeUtils).getDisplayType(format, targetingContext);
                will(returnValue(displayType));
                allowing(domainCache).getComponentByFormatAndSystemName(format, StaticTagController.IMAGE_COMPONENT_SYSTEM_NAME);
                will(returnValue(imageComponent));
                allowing(displayType).getId();
                will(returnValue(displayTypeId));
                allowing(imageComponent).getId();
                will(returnValue(imageComponentId));
                allowing(creative).getAsset(displayTypeId, imageComponentId);
                will(returnValue(null));
                oneOf(displayTypeUtils).getAllDisplayTypes(format, targetingContext);
                will(returnValue(allDisplayTypes));
                allowing(alternateDisplayType).getId();
                will(returnValue(alternateDisplayTypeId));
                oneOf(creative).getAsset(alternateDisplayTypeId, imageComponentId);
                will(returnValue(asset));
            }
        });

        assertEquals(asset, staticTagController.resolveImageAsset(creative, targetingContext));
    }

    @Test
    public void testResolveImageAsset05_asset_found_via_displayType() {
        final CreativeDto creative = mock(CreativeDto.class);
        final long formatId = randomLong();
        final FormatDto format = mock(FormatDto.class);
        final DisplayTypeDto displayType = mock(DisplayTypeDto.class);
        final long displayTypeId = randomLong();
        final ComponentDto imageComponent = mock(ComponentDto.class, "imageComponent");
        final long imageComponentId = randomLong();
        final AssetDto asset = mock(AssetDto.class);

        expect(new Expectations() {
            {
                ignoring(creative).getId();
                ignoring(format).getSystemName();
                ignoring(displayType).getSystemName();
                allowing(targetingContext).getDomainCache();
                will(returnValue(domainCache));
                allowing(creative).getFormatId();
                will(returnValue(formatId));
                allowing(domainCache).getFormatById(formatId);
                will(returnValue(format));
                allowing(displayTypeUtils).getDisplayType(format, targetingContext);
                will(returnValue(displayType));
                allowing(domainCache).getComponentByFormatAndSystemName(format, StaticTagController.IMAGE_COMPONENT_SYSTEM_NAME);
                will(returnValue(imageComponent));
                allowing(displayType).getId();
                will(returnValue(displayTypeId));
                allowing(imageComponent).getId();
                will(returnValue(imageComponentId));
                allowing(creative).getAsset(displayTypeId, imageComponentId);
                will(returnValue(asset));
            }
        });

        assertEquals(asset, staticTagController.resolveImageAsset(creative, targetingContext));
    }

    // MAD-730 - Delete ignored tests in Adserver project     
    //    @Ignore
    //    @Test
    //    public void testRedirectToFallbackImage01_fallbackUrl_empty() throws Exception {
    //        final String pixelImageUrl = assetBaseUrl + pixelImageUri;
    //        
    //        expect(new Expectations() {{
    //            oneOf (request).getParameter(Parameters.STATIC_FALLBACK_URL); will(returnValue(null));
    //            oneOf (vhostManager).getAssetBaseUrl(request); will(returnValue(assetBaseUrl));
    //            oneOf (response).sendRedirect(pixelImageUrl);
    //        }});
    //
    //        staticTagController.redirectToFallbackImage(request, response);
    //    }

    @Test
    public void testRedirectToFallbackImage02_fallbackUrl_not_empty() throws Exception {
        final String fallbackUrl = randomUrl();

        expect(new Expectations() {
            {
                oneOf(request).getParameter(Parameters.STATIC_FALLBACK_URL);
                will(returnValue(fallbackUrl));
                oneOf(response).sendRedirect(fallbackUrl);
            }
        });

        staticTagController.redirectToFallbackImage(request, response);
    }

    @Test
    public void testRedirectToFallbackClick01_fallbackUrl_empty() throws Exception {

        expect(new Expectations() {
            {
                oneOf(request).getParameter(Parameters.STATIC_FALLBACK_URL);
                will(returnValue(null));
                oneOf(clickUtils).redirectToFallbackUrl(request, response);
            }
        });

        staticTagController.redirectToFallbackClick(request, response);
    }

    @Test
    public void testRedirectToFallbackClick02_fallbackUrl_not_empty() throws Exception {
        final String fallbackUrl = randomUrl();

        expect(new Expectations() {
            {
                oneOf(request).getParameter(Parameters.STATIC_FALLBACK_URL);
                will(returnValue(fallbackUrl));
                oneOf(response).sendRedirect(fallbackUrl);
            }
        });

        staticTagController.redirectToFallbackClick(request, response);
    }

    // MAD-730 - Delete ignored tests in Adserver project 
    //    @Ignore
    //    @Test
    //    public void testGetHoldingAdImageUrl01_displayType_null() {
    //        final AdSpaceDto adSpace = mock(AdSpaceDto.class);
    //        final FormatDto format1 = mock(FormatDto.class, "format1");
    //        final long formatId1 = randomLong();
    //        final String format1SystemName = "text";
    //        final FormatDto format2 = mock(FormatDto.class, "format2");
    //        final long formatId2 = randomLong();
    //        final String format2SystemName = "iab300x250";
    //        final FormatDto format3 = mock(FormatDto.class, "format3");
    //        final long formatId3 = randomLong();
    //        final String format3SystemName = "banner";
    //        final Set<Long> formatIds = new LinkedHashSet<Long>() {{
    //                add(formatId1);
    //                add(formatId2);
    //                add(formatId3);
    //            }};
    //        final ComponentDto imageComponent = mock(ComponentDto.class, "imageComponent");
    //
    //        expect(new Expectations() {{
    //            allowing (targetingContext).getDomainCache(); will(returnValue(domainCache));
    //            allowing (format1).getSystemName(); will(returnValue(format1SystemName));
    //            allowing (format2).getSystemName(); will(returnValue(format2SystemName));
    //            allowing (format3).getSystemName(); will(returnValue(format3SystemName));
    //            oneOf (adSpace).getFormatIds(); will(returnValue(formatIds));
    //            
    //            oneOf (domainCache).getFormatById(formatId1); will(returnValue(format1));
    //            oneOf (domainCache).getComponentByFormatAndSystemName(format1, StaticTagController.IMAGE_COMPONENT_SYSTEM_NAME); will(returnValue(null));
    //            
    //            oneOf (domainCache).getFormatById(formatId2); will(returnValue(format2));
    //            oneOf (domainCache).getComponentByFormatAndSystemName(format2, StaticTagController.IMAGE_COMPONENT_SYSTEM_NAME); will(returnValue(imageComponent));
    //            
    //            oneOf (domainCache).getFormatById(formatId3); will(returnValue(format3));
    //            oneOf (domainCache).getComponentByFormatAndSystemName(format3, StaticTagController.IMAGE_COMPONENT_SYSTEM_NAME); will(returnValue(imageComponent));
    //
    //            oneOf (displayTypeUtils).getDisplayType(format3, targetingContext); will(returnValue(null));
    //            oneOf (vhostManager).getAssetBaseUrl(request); will(returnValue(assetBaseUrl));
    //        }});
    //
    //        String expectedUrl = assetBaseUrl + pixelImageUri;
    //        String url = staticTagController.getHoldingAdImageUrl(request, adSpace, targetingContext);
    //        assertEquals(expectedUrl, url);
    //    }

    @Test
    public void testGetHoldingAdImageUrl02_displayType_found() {
        final AdSpaceDto adSpace = mock(AdSpaceDto.class);
        final FormatDto format1 = mock(FormatDto.class, "format1");
        final long formatId1 = randomLong();
        final String format1SystemName = "text";
        final FormatDto format2 = mock(FormatDto.class, "format2");
        final long formatId2 = randomLong();
        final String format2SystemName = "iab300x250";
        final FormatDto format3 = mock(FormatDto.class, "format3");
        final long formatId3 = randomLong();
        final String format3SystemName = "banner";
        final Set<Long> formatIds = new LinkedHashSet<Long>() {
            {
                add(formatId1);
                add(formatId2);
                add(formatId3);
            }
        };
        final ComponentDto imageComponent = mock(ComponentDto.class, "imageComponent");
        final DisplayTypeDto displayType = mock(DisplayTypeDto.class);
        final String displayTypeSystemName = randomAlphaNumericString(10);

        expect(new Expectations() {
            {
                allowing(targetingContext).getDomainCache();
                will(returnValue(domainCache));
                allowing(format1).getSystemName();
                will(returnValue(format1SystemName));
                allowing(format2).getSystemName();
                will(returnValue(format2SystemName));
                allowing(format3).getSystemName();
                will(returnValue(format3SystemName));
                oneOf(adSpace).getFormatIds();
                will(returnValue(formatIds));

                oneOf(domainCache).getFormatById(formatId1);
                will(returnValue(format1));
                oneOf(domainCache).getComponentByFormatAndSystemName(format1, StaticTagController.IMAGE_COMPONENT_SYSTEM_NAME);
                will(returnValue(null));

                oneOf(domainCache).getFormatById(formatId2);
                will(returnValue(format2));
                oneOf(domainCache).getComponentByFormatAndSystemName(format2, StaticTagController.IMAGE_COMPONENT_SYSTEM_NAME);
                will(returnValue(imageComponent));

                oneOf(domainCache).getFormatById(formatId3);
                will(returnValue(format3));
                oneOf(domainCache).getComponentByFormatAndSystemName(format3, StaticTagController.IMAGE_COMPONENT_SYSTEM_NAME);
                will(returnValue(imageComponent));

                oneOf(displayTypeUtils).getDisplayType(format3, targetingContext);
                will(returnValue(displayType));
                atLeast(1).of(request).getScheme();
                will(returnValue(scheme));
                atLeast(1).of(request).getServerName();
                will(returnValue(serverName));
                atLeast(1).of(request).getServerPort();
                will(returnValue(serverPort));
                oneOf(request).getContextPath();
                will(returnValue(contextPath));
                allowing(displayType).getSystemName();
                will(returnValue(displayTypeSystemName));
            }
        });

        String expectedUrl = scheme + "://" + serverName + ":" + serverPort + contextPath + "/images/verified_" + format3SystemName + "_" + displayTypeSystemName + ".gif";
        String url = staticTagController.getHoldingAdImageUrl(request, adSpace, targetingContext);
        assertEquals(expectedUrl, url);
    }

    @Test
    public void testHandleStaticImage01_invalid_ip_address() throws Exception {
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final String staticFallbackUrl = randomUrl();

        expect(new Expectations() {
            {
                oneOf(response).setHeader("Expires", "0");
                oneOf(response).setHeader("Pragma", "No-Cache");
                oneOf(targetingContextFactory).createTargetingContext(request, true);
                will(throwException(new InvalidIpAddressException("bummer")));

                // redirectToFallbackImage
                oneOf(request).getParameter(Parameters.STATIC_FALLBACK_URL);
                will(returnValue(staticFallbackUrl));
                oneOf(response).sendRedirect(staticFallbackUrl);
            }
        });

        staticTagController.handleStaticImage(request, response, adSpaceExternalID);
    }

    @Test
    public void testHandleStaticImage02_exception_creating_targetingContext() throws Exception {
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final String staticFallbackUrl = randomUrl();

        expect(new Expectations() {
            {
                oneOf(response).setHeader("Expires", "0");
                oneOf(response).setHeader("Pragma", "No-Cache");
                oneOf(targetingContextFactory).createTargetingContext(request, true);
                will(throwException(new IllegalStateException("bummer")));

                // redirectToFallbackImage
                oneOf(request).getParameter(Parameters.STATIC_FALLBACK_URL);
                will(returnValue(staticFallbackUrl));
                oneOf(response).sendRedirect(staticFallbackUrl);
            }
        });

        staticTagController.handleStaticImage(request, response, adSpaceExternalID);
    }

    @Test
    public void testHandleStaticImage03_blacklisted() throws Exception {
        final String adSpaceExternalID = randomAlphaNumericString(10);

        expect(new Expectations() {
            {
                oneOf(response).setHeader("Expires", "0");
                oneOf(response).setHeader("Pragma", "No-Cache");
                oneOf(targetingContextFactory).createTargetingContext(request, true);
                will(returnValue(targetingContext));
                oneOf(preProcessor).preProcessRequest(targetingContext);
                will(throwException(new BlacklistedException("bummer")));
                oneOf(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
            }
        });

        staticTagController.handleStaticImage(request, response, adSpaceExternalID);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testHandleStaticImage04_adSpace_dormant() throws Exception {
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final String staticFallbackUrl = randomUrl();

        expect(new Expectations() {
            {
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));

                oneOf(response).setHeader("Expires", "0");
                oneOf(response).setHeader("Pragma", "No-Cache");
                oneOf(targetingContextFactory).createTargetingContext(request, true);
                will(returnValue(targetingContext));
                oneOf(preProcessor).preProcessRequest(targetingContext);
                oneOf(adserverDomainCache).getAdSpaceByExternalID(adSpaceExternalID);
                will(returnValue(null));
                oneOf(adserverDomainCache).getDormantAdSpaceExternalIds();
                will(returnValue(Collections.singleton(adSpaceExternalID)));
                oneOf(adSpaceUtils).reactivateDormantAdSpace(adSpaceExternalID);

                // redirectToFallbackImage
                oneOf(request).getParameter(Parameters.STATIC_FALLBACK_URL);
                will(returnValue(staticFallbackUrl));
                oneOf(response).sendRedirect(staticFallbackUrl);
            }
        });

        staticTagController.handleStaticImage(request, response, adSpaceExternalID);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testHandleStaticImage05_invalid_adSpaceExternalID() throws Exception {
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final String staticFallbackUrl = randomUrl();

        expect(new Expectations() {
            {
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));

                oneOf(response).setHeader("Expires", "0");
                oneOf(response).setHeader("Pragma", "No-Cache");
                oneOf(targetingContextFactory).createTargetingContext(request, true);
                will(returnValue(targetingContext));
                oneOf(preProcessor).preProcessRequest(targetingContext);
                oneOf(adserverDomainCache).getAdSpaceByExternalID(adSpaceExternalID);
                will(returnValue(null));
                oneOf(adserverDomainCache).getDormantAdSpaceExternalIds();
                will(returnValue(Collections.emptySet()));

                // redirectToFallbackImage
                oneOf(request).getParameter(Parameters.STATIC_FALLBACK_URL);
                will(returnValue(staticFallbackUrl));
                oneOf(response).sendRedirect(staticFallbackUrl);
            }
        });

        staticTagController.handleStaticImage(request, response, adSpaceExternalID);
    }

    // MAD-730 - Delete ignored tests in Adserver project 
    //    @Ignore
    //    @Test
    //    public void testHandleStaticImage06_publication_pending_NO_AD() throws Exception {
    //        final String adSpaceExternalID = randomAlphaNumericString(10);
    //        final AdSpaceDto adSpace = mock(AdSpaceDto.class);
    //        final PublicationDto publication = mock(PublicationDto.class);
    //        final PublisherDto publisher = mock(PublisherDto.class);
    //        final String pixelImageUrl = assetBaseUrl + pixelImageUri;
    //
    //        expect(new Expectations() {{
    //            ignoring (publication).getId();
    //            
    //            allowing (targetingContext).getAdserverDomainCache(); will(returnValue(adserverDomainCache));
    //            allowing (adserverDomainCache).getAdSpaceByExternalID(adSpaceExternalID); will(returnValue(adSpace));
    //            allowing (adSpace).getPublication(); will(returnValue(publication));
    //            allowing (publication).getPublisher(); will(returnValue(publisher));
    //            
    //            oneOf (response).setHeader("Expires", "0");
    //            oneOf (response).setHeader("Pragma", "No-Cache");
    //            oneOf (targetingContextFactory).createTargetingContext(request, true); will(returnValue(targetingContext));
    //            oneOf (preProcessor).preProcessRequest(targetingContext);
    //            oneOf (targetingContext).setAdSpace(adSpace);
    //            oneOf (statusChangeManager).getStatus(publication); will(returnValue(Publication.Status.PENDING));
    //
    //            oneOf (publisher).getPendingAdType(); will(returnValue(PendingAdType.NO_AD));
    //            oneOf (vhostManager).getAssetBaseUrl(request); will(returnValue(assetBaseUrl));
    //            oneOf (response).sendRedirect(pixelImageUrl);
    //            
    //            // verifyAdSpace
    //            oneOf (statusChangeManager).getStatus(adSpace); will(returnValue(AdSpace.Status.VERIFIED));
    //        }});
    //
    //        staticTagController.handleStaticImage(request, response, adSpaceExternalID);
    //    }
    //
    //    @Ignore
    //    @Test
    //    public void testHandleStaticImage07_publication_pending_HOLDING_AD() throws Exception {
    //        final String adSpaceExternalID = randomAlphaNumericString(10);
    //        final AdSpaceDto adSpace = mock(AdSpaceDto.class);
    //        final PublicationDto publication = mock(PublicationDto.class);
    //        final PublisherDto publisher = mock(PublisherDto.class);
    //        final String pixelImageUrl = assetBaseUrl + pixelImageUri;
    //
    //        expect(new Expectations() {{
    //            ignoring (publication).getId();
    //            
    //            allowing (targetingContext).getAdserverDomainCache(); will(returnValue(adserverDomainCache));
    //            allowing (adserverDomainCache).getAdSpaceByExternalID(adSpaceExternalID); will(returnValue(adSpace));
    //            allowing (adSpace).getPublication(); will(returnValue(publication));
    //            allowing (publication).getPublisher(); will(returnValue(publisher));
    //            
    //            oneOf (response).setHeader("Expires", "0");
    //            oneOf (response).setHeader("Pragma", "No-Cache");
    //            oneOf (targetingContextFactory).createTargetingContext(request, true); will(returnValue(targetingContext));
    //            oneOf (preProcessor).preProcessRequest(targetingContext);
    //            oneOf (targetingContext).setAdSpace(adSpace);
    //            oneOf (statusChangeManager).getStatus(publication); will(returnValue(Publication.Status.PENDING));
    //
    //            oneOf (publisher).getPendingAdType(); will(returnValue(PendingAdType.HOLDING_AD));
    //            oneOf (adSpace).getFormatIds(); will(returnValue(Collections.emptySet()));
    //            oneOf (displayTypeUtils).getDisplayType(with(aNull(FormatDto.class)), with(targetingContext)); will(returnValue(null));
    //            oneOf (vhostManager).getAssetBaseUrl(request); will(returnValue(assetBaseUrl));
    //            oneOf (response).sendRedirect(pixelImageUrl);
    //
    //            // verifyAdSpace
    //            oneOf (statusChangeManager).getStatus(adSpace); will(returnValue(AdSpace.Status.VERIFIED));
    //        }});
    //
    //        staticTagController.handleStaticImage(request, response, adSpaceExternalID);
    //    }

    /*
    @Test
    public void testHandleStaticImage08_formats_specified() throws Exception {
        // TODO
    }

    @Test
    public void testHandleStaticImage09_adSpace_formats_allowedFormatIds_mismatch() throws Exception {
        // TODO
    }
    */

    @Test
    public void testHandleStaticImage10_no_staticImpressionId() throws Exception {
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final long adSpaceId = randomLong();
        final AdSpaceDto adSpace = mock(AdSpaceDto.class);
        final PublicationDto publication = mock(PublicationDto.class);
        final FormatDto format = mock(FormatDto.class);
        final long formatId = randomLong();
        final Set<FormatDto> allFormats = new LinkedHashSet<FormatDto>() {
            {
                add(format);
            }
        };
        final ComponentDto imageComponent = mock(ComponentDto.class, "imageComponent");
        final Set<Long> adSpaceFormatIds = new LinkedHashSet<Long>() {
            {
                add(formatId);
            }
        };
        final String staticFallbackUrl = randomUrl();
        final UnfilledReason unfilledReason = UnfilledReason.UNKNOWN;
        final AdEvent adEvent = mock(AdEvent.class);
        final Date eventTime = new Date();

        final AnyCapturingMatcher<Impression> impressionCapturer = new AnyCapturingMatcher<Impression>(Impression.class);

        expect(new Expectations() {
            {
                allowing(targetingContext).getDomainCache();
                will(returnValue(domainCache));
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(adSpace).getId();
                will(returnValue(adSpaceId));
                allowing(adSpace).getPublication();
                will(returnValue(publication));
                allowing(format).getId();
                will(returnValue(formatId));
                oneOf(response).setHeader("Expires", "0");
                oneOf(response).setHeader("Pragma", "No-Cache");
                oneOf(targetingContextFactory).createTargetingContext(request, true);
                will(returnValue(targetingContext));
                oneOf(preProcessor).preProcessRequest(targetingContext);
                oneOf(adserverDomainCache).getAdSpaceByExternalID(adSpaceExternalID);
                will(returnValue(adSpace));
                oneOf(targetingContext).setAdSpace(adSpace);
                oneOf(statusChangeManager).getStatus(publication);
                will(returnValue(Publication.Status.ACTIVE));
                oneOf(targetingContext).getAttribute(Parameters.FORMATS);
                will(returnValue(null));
                oneOf(domainCache).getAllFormats();
                will(returnValue(allFormats));
                oneOf(domainCache).getComponentByFormatAndSystemName(format, StaticTagController.IMAGE_COMPONENT_SYSTEM_NAME);
                will(returnValue(imageComponent));
                oneOf(adSpace).getFormatIds();
                will(returnValue(adSpaceFormatIds));
                oneOf(targetingContext).getAttribute(Parameters.STATIC_IMPRESSION_ID);
                will(returnValue(null));

                oneOf(request).getParameter(Parameters.STATIC_FALLBACK_URL);
                will(returnValue(staticFallbackUrl));
                oneOf(adEventFactory).newInstance(AdAction.UNFILLED_REQUEST);
                will(returnValue(adEvent));
                oneOf(targetingContext).getAttribute(TargetingContext.UNFILLED_REASON);
                will(returnValue(null));
                oneOf(adEvent).setUnfilledReason(unfilledReason);
                oneOf(response).sendRedirect(staticFallbackUrl);

                // Capture the Impression here, we'll need it below
                oneOf(targetingContext).populateAdEvent(with(adEvent), with(impressionCapturer), with(aNull(CreativeDto.class)));

                oneOf(adEvent).getAdAction(); 
                oneOf(adEvent).getEventTime();
                will(returnValue(eventTime));
                oneOf(backupLogger).logUnfilledRequest(unfilledReason, eventTime, targetingContext);
                
                // verifyAdSpace
                oneOf(statusChangeManager).getStatus(adSpace);
                will(returnValue(AdSpace.Status.VERIFIED));
            }
        });

        staticTagController.handleStaticImage(request, response, adSpaceExternalID);

        // AF-1265 - make sure Impression.creativeId is zero
        Impression impression = impressionCapturer.getCapturedArgument();
        assertEquals(0, impression.getCreativeId());
    }

    @Test
    public void testHandleStaticImage11_exception_thrown_by_targetAd() throws Exception {
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final long adSpaceId = randomLong();
        final AdSpaceDto adSpace = mock(AdSpaceDto.class);
        final PublicationDto publication = mock(PublicationDto.class);
        final FormatDto format = mock(FormatDto.class);
        final long formatId = randomLong();
        final Set<FormatDto> allFormats = new LinkedHashSet<FormatDto>() {
            {
                add(format);
            }
        };
        final ComponentDto imageComponent = mock(ComponentDto.class, "imageComponent");
        final Set<Long> adSpaceFormatIds = new LinkedHashSet<Long>() {
            {
                add(formatId);
            }
        };
        final String staticImpressionId = randomAlphaNumericString(10);
        final String staticFallbackUrl = randomUrl();
        final UnfilledReason unfilledReason = UnfilledReason.EXCEPTION;
        final AdEvent adEvent = mock(AdEvent.class);
        
        final Date eventTime = new Date();

        final AnyCapturingMatcher<Impression> impressionCapturer = new AnyCapturingMatcher<Impression>(Impression.class);

        expect(new Expectations() {
            {
                allowing(targetingContext).getDomainCache();
                will(returnValue(domainCache));
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(adSpace).getId();
                will(returnValue(adSpaceId));
                allowing(adSpace).getPublication();
                will(returnValue(publication));
                allowing(format).getId();
                will(returnValue(formatId));
                oneOf(response).setHeader("Expires", "0");
                oneOf(response).setHeader("Pragma", "No-Cache");
                oneOf(targetingContextFactory).createTargetingContext(request, true);
                will(returnValue(targetingContext));
                oneOf(preProcessor).preProcessRequest(targetingContext);
                oneOf(adserverDomainCache).getAdSpaceByExternalID(adSpaceExternalID);
                will(returnValue(adSpace));
                oneOf(targetingContext).setAdSpace(adSpace);
                oneOf(statusChangeManager).getStatus(publication);
                will(returnValue(Publication.Status.ACTIVE));
                oneOf(targetingContext).getAttribute(Parameters.FORMATS);
                will(returnValue(null));
                oneOf(domainCache).getAllFormats();
                will(returnValue(allFormats));
                oneOf(domainCache).getComponentByFormatAndSystemName(format, StaticTagController.IMAGE_COMPONENT_SYSTEM_NAME);
                will(returnValue(imageComponent));
                oneOf(adSpace).getFormatIds();
                will(returnValue(adSpaceFormatIds));
                oneOf(targetingContext).getAttribute(Parameters.STATIC_IMPRESSION_ID);
                will(returnValue(staticImpressionId));

                // targetAd...throw quickly
                oneOf(publication).getEffectiveAdRequestTimeout();
                will(throwException(new IllegalStateException("bummer")));

                oneOf(request).getParameter(Parameters.STATIC_FALLBACK_URL);
                will(returnValue(staticFallbackUrl));
                oneOf(adEventFactory).newInstance(AdAction.UNFILLED_REQUEST);
                will(returnValue(adEvent));
                oneOf(adEvent).setUnfilledReason(unfilledReason);
                oneOf(response).sendRedirect(staticFallbackUrl);

                // Capture the Impression here, we'll need it below
                oneOf(targetingContext).populateAdEvent(with(adEvent), with(impressionCapturer), with(aNull(CreativeDto.class)));

                oneOf(adEvent).getAdAction(); 
                oneOf(adEvent).getEventTime();
                will(returnValue(eventTime));
                oneOf(backupLogger).logUnfilledRequest(unfilledReason, eventTime, targetingContext);
                

                // verifyAdSpace
                oneOf(statusChangeManager).getStatus(adSpace);
                will(returnValue(AdSpace.Status.VERIFIED));
            }
        });

        staticTagController.handleStaticImage(request, response, adSpaceExternalID);

        // AF-1265 - make sure Impression.creativeId is zero
        Impression impression = impressionCapturer.getCapturedArgument();
        assertEquals(0, impression.getCreativeId());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testHandleStaticImage12_unfilled_request() throws Exception {
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final long adSpaceId = randomLong();
        final AdSpaceDto adSpace = mock(AdSpaceDto.class);
        final PublicationDto publication = mock(PublicationDto.class);
        final FormatDto format = mock(FormatDto.class);
        final long formatId = randomLong();
        final Set<FormatDto> allFormats = new LinkedHashSet<FormatDto>() {
            {
                add(format);
            }
        };
        final ComponentDto imageComponent = mock(ComponentDto.class, "imageComponent");
        final Set<Long> adSpaceFormatIds = new LinkedHashSet<Long>() {
            {
                add(formatId);
            }
        };
        final String staticImpressionId = randomAlphaNumericString(10);
        final String staticFallbackUrl = randomUrl();
        final UnfilledReason unfilledReason = UnfilledReason.NO_CREATIVES;
        final AdEvent adEvent = mock(AdEvent.class);

        final AnyCapturingMatcher<Impression> impressionCapturer = new AnyCapturingMatcher<Impression>(Impression.class);

        final ByydImp byydImp = null;
        
        final Date eventTime = new Date();

        expect(new Expectations() {
            {
                allowing(targetingContext).getDomainCache();
                will(returnValue(domainCache));
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(adSpace).getId();
                will(returnValue(adSpaceId));
                allowing(adSpace).getPublication();
                will(returnValue(publication));
                allowing(format).getId();
                will(returnValue(formatId));
                oneOf(response).setHeader("Expires", "0");
                oneOf(response).setHeader("Pragma", "No-Cache");
                oneOf(targetingContextFactory).createTargetingContext(request, true);
                will(returnValue(targetingContext));
                oneOf(preProcessor).preProcessRequest(targetingContext);
                oneOf(adserverDomainCache).getAdSpaceByExternalID(adSpaceExternalID);
                will(returnValue(adSpace));
                oneOf(targetingContext).setAdSpace(adSpace);
                oneOf(statusChangeManager).getStatus(publication);
                will(returnValue(Publication.Status.ACTIVE));
                oneOf(targetingContext).getAttribute(Parameters.FORMATS);
                will(returnValue(null));
                oneOf(domainCache).getAllFormats();
                will(returnValue(allFormats));
                oneOf(domainCache).getComponentByFormatAndSystemName(format, StaticTagController.IMAGE_COMPONENT_SYSTEM_NAME);
                will(returnValue(imageComponent));
                oneOf(adSpace).getFormatIds();
                will(returnValue(adSpaceFormatIds));
                oneOf(targetingContext).getAttribute(Parameters.STATIC_IMPRESSION_ID);
                will(returnValue(staticImpressionId));

                // targetAd
                oneOf(publication).getEffectiveAdRequestTimeout();
                will(returnValue(2000L));
                oneOf(targetingEngine).selectCreative(with(adSpace), with(any(Set.class)), with(targetingContext), with(false), with(false), with(any(TimeLimit.class)),
                        with(aNull(TargetingEventListener.class)));
                will(returnValue(null));
                oneOf(targetingContext).populateImpression(with(any(Impression.class)), with(aNull(SelectedCreative.class)));

                oneOf(request).getParameter(Parameters.STATIC_FALLBACK_URL);
                will(returnValue(staticFallbackUrl));
                oneOf(adEventFactory).newInstance(AdAction.UNFILLED_REQUEST);
                will(returnValue(adEvent));
                oneOf(targetingContext).getAttribute(TargetingContext.UNFILLED_REASON);
                will(returnValue(unfilledReason));
                oneOf(adEvent).setUnfilledReason(unfilledReason);
                oneOf(response).sendRedirect(staticFallbackUrl);

                // Capture the Impression here, we'll need it below
                oneOf(targetingContext).populateAdEvent(with(adEvent), with(impressionCapturer), with(aNull(CreativeDto.class)));

                oneOf(adEvent).getAdAction(); 
                oneOf(adEvent).getEventTime();
                will(returnValue(eventTime));
                oneOf(backupLogger).logUnfilledRequest(unfilledReason, eventTime, targetingContext);
                
                // verifyAdSpace
                oneOf(statusChangeManager).getStatus(adSpace);
                will(returnValue(AdSpace.Status.VERIFIED));

                allowing(targetingContext).getAdSpace();
                will(returnValue(null));
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(adserverDomainCache).getCreativeById(with(any(Long.class)));
                will(returnValue(null));
            }
        });

        staticTagController.handleStaticImage(request, response, adSpaceExternalID);

        // AF-1265 - make sure Impression.creativeId is zero
        Impression impression = impressionCapturer.getCapturedArgument();
        assertEquals(0, impression.getCreativeId());
    }

    /*
    @Test
    public void testHandleStaticImage13_successful_targeting_with_pd() throws Exception {
        // TODO
    }
    */

    @Test
    @SuppressWarnings("unchecked")
    public void testHandleStaticImage15_successful_targeting_no_asset() throws Exception {
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final long adSpaceId = randomLong();
        final AdSpaceDto adSpace = mock(AdSpaceDto.class);
        final PublicationDto publication = mock(PublicationDto.class);
        final FormatDto format = mock(FormatDto.class);
        final long formatId = randomLong();
        final Set<FormatDto> allFormats = new LinkedHashSet<FormatDto>() {
            {
                add(format);
            }
        };
        final ComponentDto imageComponent = mock(ComponentDto.class, "imageComponent");
        final long imageComponentId = randomLong();
        final Set<Long> adSpaceFormatIds = new LinkedHashSet<Long>() {
            {
                add(formatId);
            }
        };
        final String staticImpressionId = randomAlphaNumericString(10);
        final SelectedCreative selectedCreative = mock(SelectedCreative.class);
        final CreativeDto creative = mock(CreativeDto.class);
        final DisplayTypeDto displayType = mock(DisplayTypeDto.class);
        final long displayTypeId = randomLong();
        final String staticFallbackUrl = randomUrl();
        final UnfilledReason unfilledReason = UnfilledReason.NO_CREATIVES;
        final AdEvent adEvent = mock(AdEvent.class);

        final AnyCapturingMatcher<Impression> impressionCapturer = new AnyCapturingMatcher<Impression>(Impression.class);
        final ByydImp byydImp = null;
        
        final Date eventTime = new Date();

        expect(new Expectations() {
            {
                ignoring(format).getSystemName();
                ignoring(displayType).getSystemName();
                ignoring(creative).getId();
                ignoring(creative).getExternalID();
                ignoring(creative).getName();
                ignoring(creative).getPriority();

                allowing(targetingContext).getDomainCache();
                will(returnValue(domainCache));
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(adSpace).getId();
                will(returnValue(adSpaceId));
                allowing(adSpace).getPublication();
                will(returnValue(publication));
                allowing(format).getId();
                will(returnValue(formatId));
                allowing(creative).getFormatId();
                will(returnValue(formatId));
                allowing(displayType).getId();
                will(returnValue(displayTypeId));
                allowing(imageComponent).getId();
                will(returnValue(imageComponentId));

                oneOf(response).setHeader("Expires", "0");
                oneOf(response).setHeader("Pragma", "No-Cache");
                oneOf(targetingContextFactory).createTargetingContext(request, true);
                will(returnValue(targetingContext));
                oneOf(preProcessor).preProcessRequest(targetingContext);
                oneOf(adserverDomainCache).getAdSpaceByExternalID(adSpaceExternalID);
                will(returnValue(adSpace));
                oneOf(targetingContext).setAdSpace(adSpace);
                oneOf(statusChangeManager).getStatus(publication);
                will(returnValue(Publication.Status.ACTIVE));
                oneOf(targetingContext).getAttribute(Parameters.FORMATS);
                will(returnValue(null));
                oneOf(domainCache).getAllFormats();
                will(returnValue(allFormats));
                oneOf(domainCache).getComponentByFormatAndSystemName(format, StaticTagController.IMAGE_COMPONENT_SYSTEM_NAME);
                will(returnValue(imageComponent));
                oneOf(adSpace).getFormatIds();
                will(returnValue(adSpaceFormatIds));
                oneOf(targetingContext).getAttribute(Parameters.STATIC_IMPRESSION_ID);
                will(returnValue(staticImpressionId));

                // targetAd
                oneOf(publication).getEffectiveAdRequestTimeout();
                will(returnValue(2000L));
                oneOf(targetingEngine).selectCreative(with(adSpace), with(any(Set.class)), with(targetingContext), with(false), with(false), with(any(TimeLimit.class)),
                        with(aNull(TargetingEventListener.class)));
                will(returnValue(selectedCreative));
                allowing(selectedCreative).getCreative();
                will(returnValue(creative));
                oneOf(targetingContext).setAttribute(TargetingContext.SELECTED_CREATIVE, selectedCreative);
                oneOf(targetingContext).populateImpression(with(any(Impression.class)), with(selectedCreative));

                oneOf(selectedCreative).getProxiedDestination();
                will(returnValue(null));

                // resolveImageAsset
                oneOf(domainCache).getFormatById(formatId);
                will(returnValue(format));
                oneOf(displayTypeUtils).getDisplayType(format, targetingContext);
                will(returnValue(displayType));
                oneOf(domainCache).getComponentByFormatAndSystemName(format, StaticTagController.IMAGE_COMPONENT_SYSTEM_NAME);
                will(returnValue(imageComponent));
                oneOf(creative).getAsset(displayTypeId, imageComponentId);
                will(returnValue(null));
                oneOf(displayTypeUtils).getAllDisplayTypes(format, targetingContext);
                will(returnValue(Collections.emptyList()));

                // Unfilled request logic
                oneOf(request).getParameter(Parameters.STATIC_FALLBACK_URL);
                will(returnValue(staticFallbackUrl));
                oneOf(adEventFactory).newInstance(AdAction.UNFILLED_REQUEST);
                will(returnValue(adEvent));
                oneOf(targetingContext).getAttribute(TargetingContext.UNFILLED_REASON);
                will(returnValue(unfilledReason));
                oneOf(adEvent).setUnfilledReason(unfilledReason);
                oneOf(response).sendRedirect(staticFallbackUrl);

                // Capture the Impression here, we'll need it below
                oneOf(targetingContext).populateAdEvent(with(adEvent), with(impressionCapturer), with(aNull(CreativeDto.class)));

                oneOf(adEvent).getAdAction(); 
                oneOf(adEvent).getEventTime();
                will(returnValue(eventTime));
                oneOf(backupLogger).logUnfilledRequest(unfilledReason, eventTime, targetingContext);

                // verifyAdSpace
                oneOf(statusChangeManager).getStatus(adSpace);
                will(returnValue(AdSpace.Status.VERIFIED));

                allowing(targetingContext).getAdSpace();
                will(returnValue(null));
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(adserverDomainCache).getCreativeById(with(any(Long.class)));
                will(returnValue(null));
            }
        });

        staticTagController.handleStaticImage(request, response, adSpaceExternalID);

        // AF-1265 - make sure Impression.creativeId is zero
        Impression impression = impressionCapturer.getCapturedArgument();
        assertEquals(0, impression.getCreativeId());
    }

    // MAD-730 - Delete ignored tests in Adserver project 	
    //    @Test
    //    @Ignore
    //    @SuppressWarnings("unchecked")
    //    public void testHandleStaticImage16_successful_targeting_all_good() throws Exception {
    //        final String adSpaceExternalID = randomAlphaNumericString(10);
    //        final long adSpaceId = randomLong();
    //        final AdSpaceDto adSpace = mock(AdSpaceDto.class);
    //        final PublicationDto publication = mock(PublicationDto.class);
    //        final FormatDto format = mock(FormatDto.class);
    //        final long formatId = randomLong();
    //        final Set<FormatDto> allFormats = new LinkedHashSet<FormatDto>() {{
    //                add(format);
    //            }};
    //        final ComponentDto imageComponent = mock(ComponentDto.class, "imageComponent");
    //        final long imageComponentId = randomLong();
    //        final Set<Long> adSpaceFormatIds = new LinkedHashSet<Long>() {{
    //                add(formatId);
    //            }};
    //        final String staticImpressionId = randomAlphaNumericString(10);
    //        final SelectedCreative selectedCreative = mock(SelectedCreative.class);
    //        final CreativeDto creative = mock(CreativeDto.class);
    //        final DisplayTypeDto displayType = mock(DisplayTypeDto.class);
    //        final long displayTypeId = randomLong();
    //        final AssetDto asset = mock(AssetDto.class);
    //        final String assetExternalID = randomAlphaNumericString(10);
    //        final AdEvent adEvent = mock(AdEvent.class);
    //        final Imp imp = mock(Imp.class, "imp");
    //        
    //        final AnyCapturingMatcher<String> imageUrlCapturer = new AnyCapturingMatcher<String>(String.class);
    //        
    //        expect(new Expectations() {{
    //            ignoring (format).getSystemName();
    //            ignoring (displayType).getSystemName();
    //            ignoring (creative).getId();
    //            ignoring (creative).getExternalID();
    //            ignoring (creative).getName();
    //            ignoring (creative).getPriority();
    //            
    //            allowing (targetingContext).getDomainCache(); will(returnValue(domainCache));
    //            allowing (targetingContext).getAdserverDomainCache(); will(returnValue(adserverDomainCache));
    //            allowing (adSpace).getId(); will(returnValue(adSpaceId));
    //            allowing (adSpace).getPublication(); will(returnValue(publication));
    //            allowing (format).getId(); will(returnValue(formatId));
    //            allowing (creative).getFormatId(); will(returnValue(formatId));
    //            allowing (displayType).getId(); will(returnValue(displayTypeId));
    //            allowing (imageComponent).getId(); will(returnValue(imageComponentId));
    //            
    //            oneOf (response).setHeader("Expires", "0");
    //            oneOf (response).setHeader("Pragma", "No-Cache");
    //            oneOf (targetingContextFactory).createTargetingContext(request, true); will(returnValue(targetingContext));
    //            oneOf (preProcessor).preProcessRequest(targetingContext);
    //            oneOf (adserverDomainCache).getAdSpaceByExternalID(adSpaceExternalID); will(returnValue(adSpace));
    //            oneOf (targetingContext).setAdSpace(adSpace);
    //            oneOf (statusChangeManager).getStatus(publication); will(returnValue(Publication.Status.ACTIVE));
    //            oneOf (targetingContext).getAttribute(Parameters.FORMATS); will(returnValue(null));
    //            oneOf (domainCache).getAllFormats(); will(returnValue(allFormats));
    //            oneOf (domainCache).getComponentByFormatAndSystemName(format, StaticTagController.IMAGE_COMPONENT_SYSTEM_NAME); will(returnValue(imageComponent));
    //            oneOf (adSpace).getFormatIds(); will(returnValue(adSpaceFormatIds));
    //            oneOf (targetingContext).getAttribute(Parameters.STATIC_IMPRESSION_ID); will(returnValue(staticImpressionId));
    //            
    //            // targetAd
    //            oneOf (publication).getEffectiveAdRequestTimeout(); will(returnValue(2000L));
    //            oneOf (targetingEngine).selectCreative(with(adSpace), with(any(Set.class)), with(targetingContext), with(false), with(false), with(any(TimeLimit.class)), with(aNull(TargetingEventListener.class)), imp); will(returnValue(selectedCreative));
    //            allowing (selectedCreative).getCreative(); will(returnValue(creative));
    //            oneOf (targetingContext).setAttribute(TargetingContext.SELECTED_CREATIVE, selectedCreative);
    //            oneOf (targetingContext).populateImpression(with(any(Impression.class)), with(selectedCreative));
    //            
    //            oneOf (selectedCreative).getProxiedDestination(); will(returnValue(null));
    //
    //            // resolveImageAsset
    //            oneOf (domainCache).getFormatById(formatId); will(returnValue(format));
    //            oneOf (displayTypeUtils).getDisplayType(format, targetingContext); will(returnValue(displayType));
    //            oneOf (domainCache).getComponentByFormatAndSystemName(format, StaticTagController.IMAGE_COMPONENT_SYSTEM_NAME); will(returnValue(imageComponent));
    //            oneOf (creative).getAsset(displayTypeId, imageComponentId); will(returnValue(asset));
    //
    //            oneOf (vhostManager).getAssetBaseUrl(request); will(returnValue(assetBaseUrl));
    //            oneOf (asset).getExternalID(); will(returnValue(assetExternalID));
    //
    //            oneOf (impressionService).saveImpression(with(any(Impression.class)));
    //            oneOf (impressionService).saveStaticImpression(with(adSpaceId), with(staticImpressionId), with(any(Impression.class)));
    //            oneOf (adEventFactory).newInstance(AdAction.AD_SERVED_AND_IMPRESSION); will(returnValue(adEvent));
    //
    //            // Capture the imageUrl here, we'll want to verify it below
    //            oneOf (response).sendRedirect(with(imageUrlCapturer));
    //            
    //            oneOf (targetingContext).populateAdEvent(with(adEvent), with(any(Impression.class)), with(creative));
    //            oneOf (adEventLogger).logAdEvent(adEvent, targetingContext);
    //            
    //            // verifyAdSpace
    //            oneOf (statusChangeManager).getStatus(adSpace); will(returnValue(AdSpace.Status.VERIFIED));
    //            
    //            allowing (targetingContext).getAdSpace(); will(returnValue(null));
    //            allowing (targetingContext).getAdserverDomainCache(); will(returnValue(adserverDomainCache));
    //            allowing (adserverDomainCache).getCreativeById(with(any(Long.class))); will(returnValue(null));
    //        }});
    //
    //        staticTagController.handleStaticImage(request, response, adSpaceExternalID);
    //
    //        String imageUrl = imageUrlCapturer.getCapturedArgument();
    //        String expectedImageUrl = assetBaseUrl + "/" + assetExternalID;
    //        assertEquals(expectedImageUrl, imageUrl);
    //    }

    @Test
    public void testHandleStaticClick01_invalid_ip_address() throws Exception {
        final String adSpaceExternalID = randomAlphaNumericString(10);

        expect(new Expectations() {
            {
                oneOf(response).setHeader("Expires", "0");
                oneOf(response).setHeader("Pragma", "No-Cache");
                oneOf(targetingContextFactory).createTargetingContext(request, true);
                will(throwException(new InvalidIpAddressException("bummer")));
                oneOf(response).sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        });

        staticTagController.handleStaticClick(request, response, adSpaceExternalID);
    }

    @Test
    public void testHandleStaticClick02_exception_creating_targetingContext() throws Exception {
        final String adSpaceExternalID = randomAlphaNumericString(10);

        expect(new Expectations() {
            {
                oneOf(response).setHeader("Expires", "0");
                oneOf(response).setHeader("Pragma", "No-Cache");
                oneOf(targetingContextFactory).createTargetingContext(request, true);
                will(throwException(new IllegalStateException("bummer")));
                oneOf(response).sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        });

        staticTagController.handleStaticClick(request, response, adSpaceExternalID);
    }

    @Test
    public void testHandleStaticClick03_staticImpressionId_not_found() throws Exception {
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final String staticFallbackUrl = randomUrl();

        expect(new Expectations() {
            {
                oneOf(response).setHeader("Expires", "0");
                oneOf(response).setHeader("Pragma", "No-Cache");
                oneOf(targetingContextFactory).createTargetingContext(request, true);
                will(returnValue(targetingContext));
                oneOf(targetingContext).getAttribute(Parameters.STATIC_IMPRESSION_ID);
                will(returnValue(null));

                // redirectToFallbackClick
                oneOf(request).getParameter(Parameters.STATIC_FALLBACK_URL);
                will(returnValue(staticFallbackUrl));
                oneOf(response).sendRedirect(staticFallbackUrl);
            }
        });

        staticTagController.handleStaticClick(request, response, adSpaceExternalID);
    }

    @Test
    public void testHandleStaticClick04_blacklisted() throws Exception {
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final String staticImpressionId = randomAlphaNumericString(10);

        expect(new Expectations() {
            {
                oneOf(response).setHeader("Expires", "0");
                oneOf(response).setHeader("Pragma", "No-Cache");
                oneOf(targetingContextFactory).createTargetingContext(request, true);
                will(returnValue(targetingContext));
                oneOf(targetingContext).getAttribute(Parameters.STATIC_IMPRESSION_ID);
                will(returnValue(staticImpressionId));
                oneOf(preProcessor).preProcessRequest(targetingContext);
                will(throwException(new BlacklistedException("bummer")));
                oneOf(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
            }
        });

        staticTagController.handleStaticClick(request, response, adSpaceExternalID);
    }

    @Test
    public void testHandleStaticClick05_invalid_adSpaceExternalID() throws Exception {
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final String staticImpressionId = randomAlphaNumericString(10);
        final String staticFallbackUrl = randomUrl();

        expect(new Expectations() {
            {
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));

                oneOf(response).setHeader("Expires", "0");
                oneOf(response).setHeader("Pragma", "No-Cache");
                oneOf(targetingContextFactory).createTargetingContext(request, true);
                will(returnValue(targetingContext));
                oneOf(targetingContext).getAttribute(Parameters.STATIC_IMPRESSION_ID);
                will(returnValue(staticImpressionId));
                oneOf(preProcessor).preProcessRequest(targetingContext);
                oneOf(adserverDomainCache).getAdSpaceByExternalID(adSpaceExternalID);
                will(returnValue(null));

                // redirectToFallbackClick
                oneOf(request).getParameter(Parameters.STATIC_FALLBACK_URL);
                will(returnValue(staticFallbackUrl));
                oneOf(response).sendRedirect(staticFallbackUrl);
            }
        });

        staticTagController.handleStaticClick(request, response, adSpaceExternalID);
    }

    @Test
    public void testHandleStaticClick06_impression_not_found() throws Exception {
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final String staticImpressionId = randomAlphaNumericString(10);
        final String staticFallbackUrl = randomUrl();
        final AdSpaceDto adSpace = mock(AdSpaceDto.class);
        final long adSpaceId = randomLong();

        expect(new Expectations() {
            {
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(adSpace).getId();
                will(returnValue(adSpaceId));

                oneOf(response).setHeader("Expires", "0");
                oneOf(response).setHeader("Pragma", "No-Cache");
                oneOf(targetingContextFactory).createTargetingContext(request, true);
                will(returnValue(targetingContext));
                oneOf(targetingContext).getAttribute(Parameters.STATIC_IMPRESSION_ID);
                will(returnValue(staticImpressionId));
                oneOf(preProcessor).preProcessRequest(targetingContext);
                oneOf(adserverDomainCache).getAdSpaceByExternalID(adSpaceExternalID);
                will(returnValue(adSpace));
                oneOf(targetingContext).setAdSpace(adSpace);
                oneOf(impressionService).getStaticImpression(adSpaceId, staticImpressionId);
                will(returnValue(null));

                // redirectToFallbackClick
                oneOf(request).getParameter(Parameters.STATIC_FALLBACK_URL);
                will(returnValue(staticFallbackUrl));
                oneOf(response).sendRedirect(staticFallbackUrl);
            }
        });

        staticTagController.handleStaticClick(request, response, adSpaceExternalID);
    }

    @Test
    public void testHandleStaticClick07_creative_not_found() throws Exception {
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final String staticImpressionId = randomAlphaNumericString(10);
        final String staticFallbackUrl = randomUrl();
        final AdSpaceDto adSpace = mock(AdSpaceDto.class);
        final long adSpaceId = randomLong();
        final Impression impression = mock(Impression.class);
        final long creativeId = randomLong();

        expect(new Expectations() {
            {
                ignoring(impression).getExternalID();

                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(adSpace).getId();
                will(returnValue(adSpaceId));
                allowing(impression).getCreativeId();
                will(returnValue(creativeId));

                oneOf(response).setHeader("Expires", "0");
                oneOf(response).setHeader("Pragma", "No-Cache");
                oneOf(targetingContextFactory).createTargetingContext(request, true);
                will(returnValue(targetingContext));
                oneOf(targetingContext).getAttribute(Parameters.STATIC_IMPRESSION_ID);
                will(returnValue(staticImpressionId));
                oneOf(preProcessor).preProcessRequest(targetingContext);
                oneOf(adserverDomainCache).getAdSpaceByExternalID(adSpaceExternalID);
                will(returnValue(adSpace));
                oneOf(targetingContext).setAdSpace(adSpace);
                oneOf(impressionService).getStaticImpression(adSpaceId, staticImpressionId);
                will(returnValue(impression));
                oneOf(adserverDomainCache).getCreativeById(creativeId);
                will(returnValue(null));
                oneOf(adserverDomainCache).getRecentlyStoppedCreativeById(creativeId);
                will(returnValue(null));

                // redirectToFallbackClick
                oneOf(request).getParameter(Parameters.STATIC_FALLBACK_URL);
                will(returnValue(staticFallbackUrl));
                oneOf(response).sendRedirect(staticFallbackUrl);
            }
        });

        staticTagController.handleStaticClick(request, response, adSpaceExternalID);
    }

    @Test
    public void testHandleStaticClick08_creative_recently_stopped() throws Exception {
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final String staticImpressionId = randomAlphaNumericString(10);
        final String staticFallbackUrl = randomUrl();
        final AdSpaceDto adSpace = mock(AdSpaceDto.class);
        final long adSpaceId = randomLong();
        final Impression impression = mock(Impression.class);
        final CreativeDto creative = mock(CreativeDto.class);
        final long creativeId = randomLong();

        expect(new Expectations() {
            {
                ignoring(impression).getExternalID();

                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(adSpace).getId();
                will(returnValue(adSpaceId));
                allowing(impression).getCreativeId();
                will(returnValue(creativeId));
                allowing(creative).getId();
                will(returnValue(creativeId));

                oneOf(response).setHeader("Expires", "0");
                oneOf(response).setHeader("Pragma", "No-Cache");
                oneOf(targetingContextFactory).createTargetingContext(request, true);
                will(returnValue(targetingContext));
                oneOf(targetingContext).getAttribute(Parameters.STATIC_IMPRESSION_ID);
                will(returnValue(staticImpressionId));
                oneOf(preProcessor).preProcessRequest(targetingContext);
                oneOf(adserverDomainCache).getAdSpaceByExternalID(adSpaceExternalID);
                will(returnValue(adSpace));
                oneOf(targetingContext).setAdSpace(adSpace);
                oneOf(impressionService).getStaticImpression(adSpaceId, staticImpressionId);
                will(returnValue(impression));
                oneOf(adserverDomainCache).getCreativeById(creativeId);
                will(returnValue(null));
                oneOf(adserverDomainCache).getRecentlyStoppedCreativeById(creativeId);
                will(returnValue(creative));
                oneOf(clickUtils).getTargetUrl(impression, creative);
                will(returnValue(null));

                // redirectToFallbackClick
                oneOf(request).getParameter(Parameters.STATIC_FALLBACK_URL);
                will(returnValue(staticFallbackUrl));
                oneOf(response).sendRedirect(staticFallbackUrl);
            }
        });

        staticTagController.handleStaticClick(request, response, adSpaceExternalID);
    }

    @Test
    public void testHandleStaticClick09_all_good() throws Exception {
        final String adSpaceExternalID = randomAlphaNumericString(10);
        final String staticImpressionId = randomAlphaNumericString(10);
        final AdSpaceDto adSpace = mock(AdSpaceDto.class);
        final long adSpaceId = randomLong();
        final Impression impression = mock(Impression.class);
        final CreativeDto creative = mock(CreativeDto.class);
        final long creativeId = randomLong();
        final String targetURL = randomUrl();

        expect(new Expectations() {
            {
                allowing(targetingContext).getAdserverDomainCache();
                will(returnValue(adserverDomainCache));
                allowing(adSpace).getId();
                will(returnValue(adSpaceId));
                allowing(impression).getCreativeId();
                will(returnValue(creativeId));
                allowing(creative).getId();
                will(returnValue(creativeId));

                oneOf(response).setHeader("Expires", "0");
                oneOf(response).setHeader("Pragma", "No-Cache");
                oneOf(targetingContextFactory).createTargetingContext(request, true);
                will(returnValue(targetingContext));
                oneOf(targetingContext).getAttribute(Parameters.STATIC_IMPRESSION_ID);
                will(returnValue(staticImpressionId));
                oneOf(preProcessor).preProcessRequest(targetingContext);
                oneOf(adserverDomainCache).getAdSpaceByExternalID(adSpaceExternalID);
                will(returnValue(adSpace));
                oneOf(targetingContext).setAdSpace(adSpace);
                oneOf(impressionService).getStaticImpression(adSpaceId, staticImpressionId);
                will(returnValue(impression));
                oneOf(adserverDomainCache).getCreativeById(creativeId);
                will(returnValue(creative));
                oneOf(clickUtils).getTargetUrl(impression, creative);
                will(returnValue(targetURL));
                oneOf(clickUtils).setClickIdCookie(response, impression, creative);
                //oneOf (adResponseLogic).postProcessVariables(targetURL, adSpace, creative, impression, targetingContext, null, true, null); will(returnValue(targetURL));
                oneOf(response).sendRedirect(targetURL);
                oneOf(clickUtils).trackClick(adSpace, creative, impression, targetingContext, null);
                allowing(clickUtils).processRedirectUrl(targetURL, false, adSpace, creative, impression, targetingContext, true);
                will(returnValue(targetURL));
            }
        });

        staticTagController.handleStaticClick(request, response, adSpaceExternalID);
    }
}
