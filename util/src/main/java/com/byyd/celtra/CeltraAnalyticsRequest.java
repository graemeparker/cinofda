package com.byyd.celtra;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class CeltraAnalyticsRequest {

    private Format format;

    private Spec spec;

    CeltraAnalyticsRequest() {

    }

    public Format getFormat() {
        return format;
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    public Spec getSpec() {
        return spec;
    }

    public void setSpec(Spec spec) {
        this.spec = spec;
    }

    public CeltraAnalyticsRequest(Format format, Spec spec) {
        this.format = format;
        this.spec = spec;
    }

    public static class Spec {

        private List<Metric> metrics;

        private List<Dimension> dimensions;

        private List<Filter> filters;

        private List<SortValue> sorts;

        private Integer limit;

        Spec() {

        }

        public Spec(List<Metric> metrics, List<Dimension> dimensions, List<Filter> filters, List<SortValue> sorts, Integer limit) {
            this.metrics = metrics;
            this.dimensions = dimensions;
            this.filters = filters;
            this.sorts = sorts;
            this.limit = limit;
        }

        public List<Metric> getMetrics() {
            return metrics;
        }

        public void setMetrics(List<Metric> metrics) {
            this.metrics = metrics;
        }

        public List<Dimension> getDimensions() {
            return dimensions;
        }

        public void setDimensions(List<Dimension> dimensions) {
            this.dimensions = dimensions;
        }

        public List<Filter> getFilters() {
            return filters;
        }

        public void setFilters(List<Filter> filters) {
            this.filters = filters;
        }

        public List<SortValue> getSorts() {
            return sorts;
        }

        public void setSorts(List<SortValue> sorts) {
            this.sorts = sorts;
        }

        public Integer getLimit() {
            return limit;
        }

        public void setLimit(Integer limit) {
            this.limit = limit;
        }

    }

    @JsonSerialize(using = FilterSerializer.class)
    public static class Filter {
        private Dimension field;
        private Operator operator;
        // operand can be array if operator is in
        private List<String> operand;

        Filter() {

        }

        public Filter(Dimension field, Operator operator, String operand) {
            this.field = field;
            this.operator = operator;
            this.operand = new ArrayList<String>();
            this.operand.add(operand);
        }

        public Dimension getField() {
            return field;
        }

        public void setField(Dimension field) {
            this.field = field;
        }

        public Operator getOperator() {
            return operator;
        }

        public void setOperator(Operator operator) {
            this.operator = operator;
        }

        public List<String> getOperand() {
            return operand;
        }

        public void setOperand(List<String> operand) {
            this.operand = operand;
        }

    }

    public static enum Format {
        json, csv, html;
    }

    public static enum Metric {

        /**
         * Those exist in example but are not documented
         */
        creativeViews, interactions,

        /**
         * Requested impressions
         */
        sessions, fallbackSessions, nonFallbackSessions,
        /**
         * Served impressions
         */
        creativeLoads, creativeLoadRate,
        /**
         * Served impressions (Core viewability)
         */
        creativeViews00, creativeNonViews00, creativeLoadsWithMeasurableViewability00, creativeLoadsWithoutMeasurableViewability00, potentialCreativeViews00, pctCreativeLoadsWithMeasurableViewability00, viewableRate00,
        /**
         * Served impressions (IAB viewability)
         */
        creativeViews501, creativeNonViews501, creativeLoadsWithMeasurableViewability501, creativeLoadsWithoutMeasurableViewability501, potentialCreativeViews501, pctCreativeLoadsWithMeasurableViewability501, viewableRate501,
        /**
         * Expansions
         */
        sessionsWithExpandAttempt, sessionsWithIntentionalExpandAttempt, expansionRate,
        /**
         * Pages
         */
        unitShows, screenShows,
        /**
         * Engagement
         */
        sessionsWithInteraction, interactionRate, unitShowsWithInteraction, unitInteractionRate, screenShowsWithInteraction, screenInteractionRate, timeOnScreen, avgTimeOnScreen, avgTimeOnUnit, sessionsWithClickReportedToExternalAdServer, sessionsWithClick,
        /**
         * Actions
         */
        customEventOccurs, customEventOccursUniqueBySession, urlOpens, storeOpens, phoneCalls, pinterestPinAttempts, photoSelections, facebookLikes, facebookShareAttempts, facebookShareSuccesses, twitterShareAttempts, whatsAppShareAttempts, facebookShareRate, twitterProfileOpens, tweetPageOpens, formSubmissionAttempts, formSubmissionSuccesses, saveImageAttempts,
        /**
         * Video
         */
        videoShows, videoManualShows, videoManualPlayRate, videoPlays, videoTrackablePlays, videoPctTrackable, videoUniqueStarts, videoStartRate, videoAutoPlays, videoManualPlays, videoPlaysWithSoundOff, videoPlaysWithSoundOn, videoMutes, videoMuteRate, videoUnmutes, videoUnmuteRate, videoPlayTime, videoAvgPlayTime, videoUniquePlayTime, videoMuteTime, videoAvgMuteTime, videoAudibleTime, videoAvgAudibleTime, videoAudibilityRate, videoSoundOffPlayRate, videoSoundOnPlayRate, videoUniqueFinishes, videoFinishRate, videoSecondViews, videoSecondUniqueViews, videoSecondAttention, videoQuarterViews, videoQuarterUniqueViews, videoQuarterAttention, videoSeconds, videoConsumptionRate,
        /**
         * Money
         */
        cost,
        /**
         * Errors
         */
        errors,
        /**
         * Locator
         */
        locatorShows, locatorShowsWithInteraction, locatorInteractionRate, locationDirectionClicks, locationEmailClicks, locationUrlOpens, locationPhoneCalls, locationShows, locationDetailsClicks;
    }

    public static enum Dimension {
        /**
         * Time (UTC)
         */
        utcDate, utcYear, utcMonth, utcDay, utcHour, utcHourTimestamp,
        /**
         * Time (account)
         */
        accountDate, accountYear, accountMonth, accountDay,
        /**
         * Account
         */
        accountId, accountName, accountIdentifier, clientType, billingCountry, billingRegion,
        /**
         * Campaign
         */
        campaignId, campaignName, industry, goal, campaignStartDate, campaignEndDate, campaignManagerId, campaignManagerFullName, managedCampaign,
        /**
         * Creative
         */
        creativeId, creativeName, product, format, actualDeviceType, creativeVersion,
        /**
         * Placement
         */
        placementId, placementName, placementStartDate, placementEndDate, placementPlannedUnitsKnown, plannedUnits, unitPrice, billingMetric,
        /**
         *  Partner
         */
        supplierId, supplierName, agencyId, agencyName, brandId, brandName,
        /**
         * External ad server
         */
        externalAdServer, externalCreativeId, externalCreativeName, externalPlacementId, externalPlacementName, externalSiteId, externalSiteName, externalSupplierId, externalSupplierName,
        /**
         * SDK
         */
        sdk,
        /**
         * Device
         */
        platform,
        /**
         * Money
         */
        mediaCurrency,
        /**
         * Pages
         */
        unitName, isUnitShowAccidental, screenDepth, timeOnScreenKnown, screenLocalId, screenTitle,
        /**
         * Social
         */
        sharedUrl,
        /**
         * Actions
         */
        label, imageBlobHash,
        /**
         * Video
         */
        /*label,*/videoSourceType, videoSource, videoFilename, videoPlayerMode, videoLength, videoActualAudibility, videoIntendedAudibility, videoPlaySetting, videoSecond, videoQuarter,
        /**
         * Locator
         */
        locatorLocalId, locatorName, locationId, locationName, locationAddress, locationCity, locationState, locationCountry,
        /**
         * Errors
         */
        errorType, error,
    }

    public static enum Operator {
        in, gt, gte, lt, lte;
    }

    public static enum Direction {
        asc, desc;
    }

    /*
        public static class FilterValue {

            private final Operator operator;

            private final String value;

            public FilterValue(Operator operator, String value) {
                Objects.requireNonNull(operator);
                this.operator = operator;
                Objects.requireNonNull(value);
                this.value = value;
            }

            public Operator getOperator() {
                return operator;
            }

            public String getValue() {
                return value;
            }
        }
    */
    public static class SortValue {

        private Metric field;

        private Direction direction;

        SortValue() {
            //json
        }

        public SortValue(Metric metric, Direction direction) {
            Objects.requireNonNull(metric);
            this.field = metric;
            Objects.requireNonNull(direction);
            this.direction = direction;
        }

        public Metric getField() {
            return field;
        }

        public Direction getDirection() {
            return direction;
        }

    }
}
