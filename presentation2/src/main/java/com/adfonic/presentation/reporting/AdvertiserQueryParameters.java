package com.adfonic.presentation.reporting;

import java.util.Date;

import com.adfonic.util.Range;

public class AdvertiserQueryParameters {

    protected long advertiserId;
    protected String campaignIds;
    protected Range<Date> dateRange;
    
    public long getAdvertiserId() {
        return advertiserId;
    }

    public String getCampaignIds() {
        return campaignIds;
    }

    public Range<Date> getDateRange() {
        return dateRange;
    }

    public static class Builder {
        private long advertiserId;
        private String campaignIds;
        private Range<Date> dateRange;

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

        public AdvertiserQueryParameters build() {
            AdvertiserQueryParameters advertiserQueryParameters = new AdvertiserQueryParameters();
            advertiserQueryParameters.advertiserId = advertiserId;
            advertiserQueryParameters.campaignIds = campaignIds;
            advertiserQueryParameters.dateRange = dateRange;
            return advertiserQueryParameters;
        }
    }
}
