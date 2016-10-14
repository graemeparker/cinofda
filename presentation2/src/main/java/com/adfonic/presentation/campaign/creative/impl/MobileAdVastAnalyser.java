package com.adfonic.presentation.campaign.creative.impl;

import java.math.BigInteger;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang.StringUtils;

import com.adfonic.dto.campaign.creative.MobileAdVastMetadataDto;
import com.adfonic.dto.campaign.creative.MobileAdVastMetadataDto.VastCompanionCreative;
import com.adfonic.dto.campaign.creative.MobileAdVastMetadataDto.VastVideoCreative;
import com.adfonic.dto.campaign.creative.MobileAdVastMetadataDto.VastWarning;
import com.adfonic.util.VastWorker.VastElementVisitor;
import com.byyd.vast2.CompanionType;
import com.byyd.vast2.CompanionType.StaticResource;
import com.byyd.vast2.ImpressionType;
import com.byyd.vast2.NonLinearType;
import com.byyd.vast2.TrackingEventsType;
import com.byyd.vast2.VAST;
import com.byyd.vast2.VAST.Ad.InLine;
import com.byyd.vast2.VAST.Ad.InLine.Creatives.Creative.Linear;
import com.byyd.vast2.VAST.Ad.InLine.Creatives.Creative.Linear.MediaFiles;
import com.byyd.vast2.VAST.Ad.Wrapper;
import com.byyd.vast2.VAST.Ad.Wrapper.Creatives.Creative.Linear.VideoClicks;
import com.byyd.vast2.VideoClicksType;

/**
 * 
 * @author mvanek
 *
 */
public class MobileAdVastAnalyser implements VastElementVisitor {

    private final MobileAdVastMetadataDto metaData = new MobileAdVastMetadataDto();

    public MobileAdVastMetadataDto getMetaData() {
        return metaData;
    }

    @Override
    public void onVAST(VAST vast, String url) {
        metaData.addVast(vast, url);
        String version = vast.getVersion();
        if (StringUtils.isNotBlank(version) && "2.0".equals(version) == false) {
            metaData.addWarning(VastWarning.VAST_VERSION, version);
        }

        if (vast.getAd().size() == 0) {
            metaData.addWarning(VastWarning.MISSING_ADS);
        } else if (vast.getAd().size() > 1) {
            metaData.addWarning(VastWarning.SINGLE_AD, String.valueOf(vast.getAd().size()));
        }
    }

    @Override
    public void onInLine(String id, InLine inline) {
        for (ImpressionType impression : inline.getImpression()) {
            metaData.addImpressionTracker(impression);
        }
    }

    @Override
    public boolean onWrapper(String id, Wrapper wrapper) {
        for (String impression : wrapper.getImpression()) {
            ImpressionType impressionType = new ImpressionType();
            impressionType.setId("Wrapper"); // ehm well...
            impressionType.setId(impression);
            metaData.addImpressionTracker(impressionType);
        }
        return true;
    }

    @Override
    public void onInLineLinear(String id, BigInteger sequence, String adID, Linear linear) {
        if (metaData.getVideoCreative() != null) {
            metaData.addWarning(VastWarning.MULTIPLE_LINEAR_ADS);
            return;
        }

        List<TrackingEventsType.Tracking> progressTracking = null;
        if (linear.getTrackingEvents() != null) {
            progressTracking = linear.getTrackingEvents().getTracking();
        }

        VideoClicksType.ClickThrough clickThrough = null;
        List<VideoClicksType.ClickTracking> clickTracking = null;
        VideoClicksType videoClicks = linear.getVideoClicks();
        if (videoClicks != null) {
            if (videoClicks.getClickThrough() != null) {
                clickThrough = videoClicks.getClickThrough();
            }
            clickTracking = videoClicks.getClickTracking();
        }

        XMLGregorianCalendar duration = linear.getDuration();
        int seconds = duration.getSecond() + duration.getMinute() * 60 + duration.getHour() * 3600;
        MediaFiles mediaFilesElement = linear.getMediaFiles();
        if (mediaFilesElement != null && mediaFilesElement.getMediaFile().size() != 0) {
            VastVideoCreative videoCreative = new VastVideoCreative(seconds, mediaFilesElement.getMediaFile(), clickThrough, progressTracking, clickTracking);
            metaData.setVideoCreative(videoCreative);
        } else {
            metaData.addWarning(VastWarning.MISSING_MEDIA_FILES, "Linear MediaFiles element not found or empty");
        }
    }

    @Override
    public void onWrapperLinear(String id, BigInteger sequence, String adID, com.byyd.vast2.VAST.Ad.Wrapper.Creatives.Creative.Linear linear) {
        //Wrapper can add additional trackers into inline linear
        linear.getTrackingEvents();
        VideoClicks videoClicks = linear.getVideoClicks();
        if (videoClicks != null) {
            videoClicks.getClickTracking();
        }
    }

    @Override
    public void onInLineNonlinear(String id, BigInteger sequence, String adID, NonLinearType nonlinear, TrackingEventsType trackingEvents) {
        metaData.addWarning(VastWarning.VIDEO_CREATIVE_TYPE);
    }

    @Override
    public void onWrapperNonlinear(String id, BigInteger sequence, String adID, NonLinearType nonlinear, TrackingEventsType trackingEvents) {
        metaData.addWarning(VastWarning.VIDEO_CREATIVE_TYPE);
    }

    @Override
    public void onInLineCompanion(String id, BigInteger sequence, String adID, CompanionType companion) {
        visitCompanion(id, sequence, adID, companion);
    }

    @Override
    public void onWrapperCompanion(String id, BigInteger sequence, String adID, CompanionType companion) {
        visitCompanion(id, sequence, adID, companion);
    }

    private void visitCompanion(String id, BigInteger sequence, String adID, CompanionType companion) {

        if (metaData.getCompanionCreative() != null) {
            metaData.addWarning(VastWarning.MULTIPLE_COMPANION_ADS);
        }

        TrackingEventsType trackingEventsElement = companion.getTrackingEvents();
        List<TrackingEventsType.Tracking> trackingEvents = null;
        if (trackingEventsElement != null) {
            trackingEvents = trackingEventsElement.getTracking();
        }

        int width = companion.getWidth().intValue();
        int height = companion.getHeight().intValue();
        String clickThroughUrl = companion.getCompanionClickThrough(); //XXX ClickThrough url
        VastCompanionCreative companionCreative;
        if (companion.getStaticResource() != null) {
            StaticResource staticResource = companion.getStaticResource();
            String mimeType = staticResource.getCreativeType();
            String creativeUrl = staticResource.getValue();
            companionCreative = new VastCompanionCreative(width, height, mimeType, creativeUrl, clickThroughUrl, trackingEvents);
        } else if (companion.getHTMLResource() != null) {
            companionCreative = new VastCompanionCreative(width, height, "html", companion.getHTMLResource(), clickThroughUrl, trackingEvents);
        } else if (companion.getIFrameResource() != null) {
            companionCreative = new VastCompanionCreative(width, height, "iframe", companion.getIFrameResource(), clickThroughUrl, trackingEvents);
        } else {
            metaData.addWarning(VastWarning.COMPANION_CREATIVE_TYPE); // Sanity check
            return;
        }
        metaData.setCompanionCreative(companionCreative);
    }

}