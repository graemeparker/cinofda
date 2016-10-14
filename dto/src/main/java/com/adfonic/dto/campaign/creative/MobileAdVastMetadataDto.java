package com.adfonic.dto.campaign.creative;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.byyd.vast2.ImpressionType;
import com.byyd.vast2.TrackingEventsType;
import com.byyd.vast2.VAST;
import com.byyd.vast2.VAST.Ad.InLine.Creatives.Creative.Linear.MediaFiles.MediaFile;
import com.byyd.vast2.VideoClicksType;

/**
 * Only linear Media files and static Companion creative
 * 
 * @author mvanek
 */
public class MobileAdVastMetadataDto {

    public static enum VastWarning {
        BROKEN_FILE, MISSING_MEDIA_FILES, UNSUPPORTED_FORMAT, MEDIAFILE_SIZE_NOT_ALLOWED, CONTENT_TYPE_NOT_FOUND, VAST_VERSION, MISSING_ADS, SINGLE_AD, MULTIPLE_LINEAR_ADS, MULTIPLE_COMPANION_ADS, VIDEO_CREATIVE_TYPE, COMPANION_CREATIVE_TYPE;
    }

    private final Map<String, VAST> vasts = new HashMap<String, VAST>();

    private final List<ImpressionType> impressionTrackers = new ArrayList<ImpressionType>();

    private VastVideoCreative videoCreative;

    private VastCompanionCreative companionCreative;

    //private Boolean sslCompliant;
    private List<String> nonSecureAssets = new ArrayList<String>();

    private final List<Warning> warnings = new ArrayList<Warning>();

    public boolean isSslCompliant() {
        return nonSecureAssets.isEmpty();
    }

    public List<String> getNonSecureAssets() {
        return nonSecureAssets;
    }

    public void addVast(VAST vast, String url) {
        if (url != null && url.startsWith("http:")) {
            nonSecureAssets.add("VAST " + url);
        }
        vasts.put(url, vast);
    }

    public List<ImpressionType> getImpressionTrackers() {
        return impressionTrackers;
    }

    public void addImpressionTracker(ImpressionType impression) {
        impressionTrackers.add(impression);
        if (isNotHttps(impression.getValue())) {
            nonSecureAssets.add("ImpressionTracker " + impression.getValue());
        }
    }

    public VastVideoCreative getVideoCreative() {
        return videoCreative;
    }

    public void setVideoCreative(VastVideoCreative videoCreative) {
        this.videoCreative = videoCreative;
        // Consider checking playback and click trackers inside... 
        for (MediaFile mediaFile : videoCreative.getMediaFiles()) {
            if (isNotHttps(mediaFile.getValue())) {
                nonSecureAssets.add("VideoCreative MediaFile " + mediaFile.getValue());
            }
        }
        for (TrackingEventsType.Tracking tracker : videoCreative.getProgressTrackers()) {
            if (isNotHttps(tracker.getValue())) {
                nonSecureAssets.add("VideoCreative ProgressTracker " + tracker.getValue());
            }
        }
    }

    public VastCompanionCreative getCompanionCreative() {
        return companionCreative;
    }

    public void setCompanionCreative(VastCompanionCreative companionCreative) {
        this.companionCreative = companionCreative;
        if (isNotHttps(companionCreative.getCreativeUrl())) {
            nonSecureAssets.add("CompanionCreative " + companionCreative.getCreativeUrl());
        }
        List<TrackingEventsType.Tracking> trackingEvents = companionCreative.getTrackingEvents();
        if (trackingEvents != null) {
            for (TrackingEventsType.Tracking event : trackingEvents) {
                if (isNotHttps(event.getValue())) {
                    nonSecureAssets.add("CompanionCreative TrackingEvent " + event.getValue());
                }
            }
        }
    }

    /**
     * URLs in VAST are commonly wrapped inside CDATA section, 
     * that also adds newline at start and at the end of url -> trim that crap!  
     */
    private boolean isNotHttps(String url) {
        return url != null && !url.trim().startsWith("https");
    }

    public void addWarning(VastWarning type, String... values) {
        this.warnings.add(new Warning(type, values));
    }

    public List<Warning> getWarnings() {
        return warnings;
    }

    public Map<String, VAST> getVasts() {
        return vasts;
    }

    public static class VastVideoCreative {
        private final int duration; //seconds
        private final List<MediaFile> mediaFiles;
        private final VideoClicksType.ClickThrough clickThrough;
        private final List<TrackingEventsType.Tracking> progressTrackers;
        private final List<VideoClicksType.ClickTracking> clickTrackers;

        public VastVideoCreative(int duration, List<MediaFile> mediaFiles, VideoClicksType.ClickThrough clickThrough, List<TrackingEventsType.Tracking> progressTrackers,
                List<VideoClicksType.ClickTracking> clickTrackers) {
            this.duration = duration;
            if (clickThrough != null && clickThrough.getValue() != null) {
                clickThrough.setValue(clickThrough.getValue().trim());
            }
            this.clickThrough = clickThrough;

            if (mediaFiles != null) {
                for (MediaFile mediaFile : mediaFiles) {
                    if (mediaFile.getValue() != null) {
                        mediaFile.setValue(mediaFile.getValue().trim());
                    }
                }
            }
            this.mediaFiles = mediaFiles != null ? mediaFiles : Collections.<MediaFile> emptyList();
            this.progressTrackers = progressTrackers != null ? progressTrackers : Collections.<TrackingEventsType.Tracking> emptyList();
            this.clickTrackers = clickTrackers != null ? clickTrackers : Collections.<VideoClicksType.ClickTracking> emptyList();
        }

        public int getDuration() {
            return duration;
        }

        public List<MediaFile> getMediaFiles() {
            return mediaFiles;
        }

        public VideoClicksType.ClickThrough getClickThrough() {
            return clickThrough;
        }

        public List<TrackingEventsType.Tracking> getProgressTrackers() {
            return progressTrackers;
        }

        public List<VideoClicksType.ClickTracking> getClickTrackers() {
            return clickTrackers;
        }

    }

    public static class VastCompanionCreative {

        private final int width;
        private final int height;
        private final String mimeType;
        private final String creativeUrl;
        private final String clickThroughUrl;
        private final List<TrackingEventsType.Tracking> trackingEvents;

        public VastCompanionCreative(int width, int height, String mimeType, String creativeUrl, String clickThroughUrl, List<TrackingEventsType.Tracking> trackingEvents) {
            this.width = width;
            this.height = height;
            this.mimeType = mimeType;
            if (creativeUrl != null) {
                creativeUrl = creativeUrl.trim();
            }
            this.creativeUrl = creativeUrl;

            if (clickThroughUrl != null) {
                clickThroughUrl = clickThroughUrl.trim();
            }
            this.clickThroughUrl = clickThroughUrl;
            this.trackingEvents = trackingEvents;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public String getMimeType() {
            return mimeType;
        }

        public String getCreativeUrl() {
            return creativeUrl;
        }

        public String getClickThroughUrl() {
            return clickThroughUrl;
        }

        public List<TrackingEventsType.Tracking> getTrackingEvents() {
            return trackingEvents;
        }
    }

    public static class Warning {
        private VastWarning type;
        private List<String> values;

        public Warning(VastWarning type, String... values) {
            super();
            this.type = type;
            this.values = Arrays.asList(values);
        }

        public VastWarning getType() {
            return type;
        }

        public List<String> getValues() {
            return values;
        }

        public String[] getValuesAsArray() {
            String[] list = null;
            if (!values.isEmpty()) {
                list = values.toArray(new String[values.size()]);
            }
            return list;
        }
    }

}
