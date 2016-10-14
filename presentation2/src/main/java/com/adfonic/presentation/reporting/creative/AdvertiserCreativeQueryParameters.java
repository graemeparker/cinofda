package com.adfonic.presentation.reporting.creative;

import java.util.Date;

import com.adfonic.presentation.reporting.AdvertiserQueryParameters;
import com.adfonic.util.Range;

public class AdvertiserCreativeQueryParameters extends AdvertiserQueryParameters {

    private String formatIds;
    private String creativeIds;

    public String getFormatIds() {
        return formatIds;
    }

    public String getCreativeIds() {
        return creativeIds;
    }

    public static class Builder {
        private long advertiserId;
        private String campaignIds;
        private Range<Date> dateRange;
        private String formatIds;
        private String creativeIds;

        public Builder advertiserId(long advertiserId) {
            this.advertiserId = advertiserId;
            return this;
        }

        public Builder campaignIds(String campaignIds) {
            this.campaignIds = campaignIds;
            return this;
        }

        public Builder dateRange(Range<Date> dateRange) {
            this.dateRange = dateRange;
            return this;
        }

        public Builder formatIds(String formatIds) {
            this.formatIds = formatIds;
            return this;
        }

        public Builder creativeIds(String creativeIds) {
            this.creativeIds = creativeIds;
            return this;
        }

        public AdvertiserCreativeQueryParameters build() {
            AdvertiserCreativeQueryParameters advertiserCreativeQueryParameters = new AdvertiserCreativeQueryParameters();
            advertiserCreativeQueryParameters.advertiserId = advertiserId;
            advertiserCreativeQueryParameters.campaignIds = campaignIds;
            advertiserCreativeQueryParameters.dateRange = dateRange;
            advertiserCreativeQueryParameters.formatIds = formatIds;
            advertiserCreativeQueryParameters.creativeIds = creativeIds;
            return advertiserCreativeQueryParameters;
        }
    }
}
