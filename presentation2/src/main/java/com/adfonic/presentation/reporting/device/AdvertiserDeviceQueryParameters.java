package com.adfonic.presentation.reporting.device;

import java.util.Date;

import com.adfonic.presentation.reporting.AdvertiserQueryParameters;
import com.adfonic.util.Range;

public class AdvertiserDeviceQueryParameters extends AdvertiserQueryParameters {

    protected String vendorIds;
    protected String modelIds;

    public String getVendorIds() {
        return vendorIds;
    }

    public String getModelIds() {
        return modelIds;
    }

    public static class Builder {
        private long advertiserId;
        private String campaignIds;
        private Range<Date> dateRange;
        private String vendorIds;
        private String modelIds;

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

        public Builder vendorIds(String vendorIds) {
            this.vendorIds = vendorIds;
            return this;
        }

        public Builder modelIds(String modelIds) {
            this.modelIds = modelIds;
            return this;
        }

        public AdvertiserDeviceQueryParameters build() {
            AdvertiserDeviceQueryParameters advertiserDeviceQueryParameters = new AdvertiserDeviceQueryParameters();
            advertiserDeviceQueryParameters.advertiserId = advertiserId;
            advertiserDeviceQueryParameters.campaignIds = campaignIds;
            advertiserDeviceQueryParameters.dateRange = dateRange;
            advertiserDeviceQueryParameters.vendorIds = vendorIds;
            advertiserDeviceQueryParameters.modelIds = modelIds;
            return advertiserDeviceQueryParameters;
        }
    }
}
