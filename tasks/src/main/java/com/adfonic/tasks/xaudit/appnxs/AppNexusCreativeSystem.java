package com.adfonic.tasks.xaudit.appnxs;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;

import com.adfonic.domain.Creative;
import com.adfonic.domain.Publisher;
import com.adfonic.tasks.xaudit.ApprovalServiceManager;
import com.adfonic.tasks.xaudit.RenderingService;
import com.adfonic.tasks.xaudit.RenderingService.RenderedCreative;
import com.adfonic.tasks.xaudit.appnxs.dat.AppNexusCreativeRecord;
import com.adfonic.tasks.xaudit.appnxs.dat.AppNexusCreativeRecord.AuditStatus;
import com.adfonic.tasks.xaudit.appnxs.dat.CreativeMobile;
import com.adfonic.tasks.xaudit.appnxs.dat.CreativeTemplate;
import com.adfonic.tasks.xaudit.exception.ExternalSystemException;
import com.adfonic.tasks.xaudit.impl.ExternalApprovalSystem;

/**
 * This is where we build and submit our creatives to AppNexus for auditing.
 * 
 * https://wiki.appnexus.com/display/adnexusdocumentation/Creative+Service
 * 
 * @author graemeparker
 *
 */
public class AppNexusCreativeSystem extends ExternalApprovalSystem {

    private static final transient Logger LOG = Logger.getLogger(AppNexusCreativeSystem.class.getName());

    private final ApprovalServiceManager<AppNexusCreativeRecord> appNexusManager;

    @Autowired
    private RenderingService renderingService;

    // The URL we are going to submit too.
    private final String assetBaseUrl;

    // Our byyd member id passed back on the URL and in creative.
    private final int memberId;

    /**
     * AppNexusCreativeSystem
     * @param publisherId
     * @param assetBaseUrl
     * @param appNexusManager
     */
    public AppNexusCreativeSystem(Set<Long> publisherIds, int memberId, String assetBaseUrl, ApprovalServiceManager<AppNexusCreativeRecord> appNexusManager) {
        super(publisherIds);
        this.appNexusManager = appNexusManager;
        this.assetBaseUrl = assetBaseUrl;
        this.memberId = memberId;
    }

    /**
     * Build and send creative.
     * @param creative
     * @return
     */
    @Override
    public String newCreative(Creative creative, Publisher publisher) {
        try {
            AppNexusCreativeRecord appNxsCreative = buildAppNexusCreative(creative, publisher);
            LOG.log(Level.INFO, "newCreative before post");
            String external = appNexusManager.postCreative(appNxsCreative);
            LOG.log(Level.INFO, "newCreative after post");
            if (external == null || external.isEmpty()) {
                LOG.log(Level.INFO, "AppNexusResponse is null");
                throw new RuntimeException("Couldn't get external creative id");
            }
            return external;
        } catch (Exception x) {
            LOG.log(Level.SEVERE, "newCreative exception", x);
        }
        LOG.log(Level.INFO, "newCreative null");
        return null;
    }

    /**
     * Builds the creative we want to send to appnexus in their format.
     * See MAX-1597 for last minute changes.
     * @param creative
     * @return
     * @throws IOException
     */
    public AppNexusCreativeRecord buildAppNexusCreative(Creative creative, Publisher publisher) throws IOException {

        // Defensive
        if (creative == null) {
            // native ad?
            throw new RuntimeException("Should not have got here!");
        }
        /*
        Map<DisplayType, AssetBundle> assetBundleByDisplayType = creative.getAssetBundleMap();
        DisplayType displayType = null;
        int width = 0;
        int height = 0;

        // Get the image size
        for (Entry<DisplayType, AssetBundle> assetBundleEntry : assetBundleByDisplayType.entrySet()) {
            for (Component component : assetBundleEntry.getValue().getAssetMap().keySet()) {
                if (component.getSystemName().equals("image")) {
                    String widthStr = component.getContentSpec(assetBundleEntry.getKey()).getManifestProperties().get("width");
                    if (widthStr != null) {
                        int widthI = Integer.parseInt(widthStr);
                        if (widthI > width) {
                            displayType = assetBundleEntry.getKey();
                            height = Integer.parseInt(component.getContentSpec(displayType).getManifestProperties().get("height"));
                            width = widthI;
                        }
                    }
                }
            }
        }

        // If the width is not set fall back to 300x50 - should really be 320.
        if (width == 0) {
            width = 300;
            height = 50;
            displayType = creative.getFormat().getDisplayType(0);
        }
        */
        RenderedCreative rendered = renderingService.renderContent(creative, publisher);
        AppNexusCreativeRecord appNxsCreative = new AppNexusCreativeRecord();
        appNxsCreative.setContent(rendered.getMarkup());
        appNxsCreative.setWidth(rendered.getAssetInfo().getWidth());
        appNxsCreative.setHeight(rendered.getAssetInfo().getHeight());
        appNxsCreative.setTemplate(CreativeTemplate.IFRAME_HTML);
        appNxsCreative.setNo_iframes(true);
        appNxsCreative.setAllow_audit(true);
        appNxsCreative.setAudit_status(AuditStatus.pending);
        appNxsCreative.setMember_id(memberId);
        appNxsCreative.setCode(creative.getExternalID());
        // Told to do this to get around creatives should open in new window
        // "Creative clicks through in the same window".
        appNxsCreative.setMobile(new CreativeMobile(rendered.getDestinationUrl()));
        return appNxsCreative;
    }

    /**
     * Get the audit status based on the appnexus external reference.
     * @param externalReference
     * @return
     */
    @Override
    public AppNexusCreativeRecord getAppNexusCreative(String externalReference) {
        return appNexusManager.getCreative(externalReference);
    }

    /**
     * Update the creative.
     * @param externalReference
     * @param creative
     */
    @Override
    public void updateCreative(String externalReference, Creative creative, Publisher publisher) {
        try {
            AppNexusCreativeRecord appNxsCreative = buildAppNexusCreative(creative, publisher);
            appNexusManager.updateCreative(externalReference, appNxsCreative);
        } catch (Exception e) {
            throw new ExternalSystemException(e);
        }
    }

    /**
     * This is where we block creative submission based on creative features.
     * See MAX-138 for a number of items removed.
     * @param creative
     * @return
     */
    @Override
    public String checkForAnyCreativeIncompatibility(Creative creative) {
        // Defensive
        if (creative == null) {
            LOG.log(Level.INFO, "checkForAnyCreativeIncompatibility null");
            return null;
        }
        // Plug-in based creative are now obsolete.
        if (creative.isPluginBased()) {
            return "Plugins not supported.";
        }

        if (creative.getFormat().getSystemName().equals("text")) {
            return "Text ads not supported.";
        }

        // Native ads for for MoPub only at this this time.
        if (creative.getFormat().getSystemName().equals("native_app_install")) {
            return "Native ads not supported.";
        }
        return null;
    }
}
