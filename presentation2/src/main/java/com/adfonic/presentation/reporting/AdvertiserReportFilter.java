package com.adfonic.presentation.reporting;

public class AdvertiserReportFilter {

    protected boolean groupByCategory;
    protected boolean groupByInventorySource;
    protected boolean useConversionTracking;
    protected boolean useGeotargeting;
    protected boolean detailedByDay;

    public boolean isGroupByCategory() {
        return groupByCategory;
    }

    public boolean isGroupByInventorySource() {
        return groupByInventorySource;
    }

    public boolean isUseConversionTracking() {
        return useConversionTracking;
    }

    public boolean isUseGeotargeting() {
        return useGeotargeting;
    }

    public boolean isDetailedByDay() {
        return detailedByDay;
    }

    public static class Builder {
        private boolean groupByCategory;
        private boolean groupByInventorySource;
        private boolean useConversionTracking;
        private boolean useGeotargeting;
        private boolean detailedByDay;

        public Builder groupByCategory(boolean groupByCategory) {
            this.groupByCategory = groupByCategory;
            return this;
        }

        public Builder groupByInventorySource(boolean groupByInventorySource) {
            this.groupByInventorySource = groupByInventorySource;
            return this;
        }

        public Builder useConversionTracking(boolean useConversionTracking) {
            this.useConversionTracking = useConversionTracking;
            return this;
        }

        public Builder useGeotargeting(boolean useGeotargeting) {
            this.useGeotargeting = useGeotargeting;
            return this;
        }

        public Builder detailedByDay(boolean detailedByDay) {
            this.detailedByDay = detailedByDay;
            return this;
        }

        public AdvertiserReportFilter build() {
            AdvertiserReportFilter advertiserReportFilter = new AdvertiserReportFilter();
            advertiserReportFilter.groupByCategory = groupByCategory;
            advertiserReportFilter.groupByInventorySource = groupByInventorySource;
            advertiserReportFilter.useConversionTracking = useConversionTracking;
            advertiserReportFilter.useGeotargeting = useGeotargeting;
            advertiserReportFilter.detailedByDay = detailedByDay;
            return advertiserReportFilter;
        }
    }
}
