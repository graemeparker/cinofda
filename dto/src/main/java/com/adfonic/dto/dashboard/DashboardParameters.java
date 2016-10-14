package com.adfonic.dto.dashboard;

public class DashboardParameters {

    public static enum Interval {
        HOURS("1"), 
        THREE_HOURS("3"), 
        SIX_HOURS("6"), 
        TWELVE_HOURS("12"), 
        DAY("24");

        private final Object dbValue;

        private Interval(Object dbValue) {
            this.dbValue = dbValue;
        }

        public String getDbValue() {
            return dbValue.toString();
        }
    }

    public enum Report {
        // COST_PER_CONVERSION_MOCKED just to wait for the procedure to be done.
        IMPRESSIONS(1), 
        CLICKS(2), 
        CTR(3), 
        CONVERSION(4), 
        SPEND(5), 
        COST_PER_CONVERSION(6), 
        COST_PER_CONVERSION_MOCKED(5);

        private final Object dbValue;

        private Report(Object dbValue) {
            this.dbValue = dbValue;
        }

        public String getDbValue() {
            return dbValue.toString();
        }
    }

    public enum SortBy {
        CAMPAIGN_NAME(1), 
        BID_TYPE(2), 
        BID_PRICE(3), 
        CVR(4), 
        CTR(5), 
        ECPA(6), 
        ECPM(7), 
        TOTAL_SPEND(8), 
        SPEND_YESTERDAY(9), 
        TOTAL_BUDGET(10), 
        DAILY_BUDGET(11), 
        SPEND(12);

        private final Object dbValue;

        private SortBy(Object dbValue) {
            this.dbValue = dbValue;
        }

        public String getDbValue() {
            return dbValue.toString();
        }

    }

    public enum OrderBy {
        ASCENDING("ASC"), 
        DESCENDING("DESC");

        private final Object dbValue;

        private OrderBy(Object dbValue) {
            this.dbValue = dbValue;
        }

        public String getDbValue() {
            return dbValue.toString();
        }

    }

    public enum PublisherReport {
        IMPRESSIONS(2), 
        REQUESTS(1), 
        FILL_RATE(3), 
        REVENUE(4), 
        ECPM(5);

        private final Object dbValue;

        private PublisherReport(Object dbValue) {
            this.dbValue = dbValue;
        }

        public String getDbValue() {
            return dbValue.toString();
        }
    }

    public enum PublisherSortBy {
        PUBLICATION_NAME(1), 
        PLATFORM(2), 
        APPROVAL(3), 
        BACKFILL(4), 
        REQUESTS(5), 
        IMPRESSIONS(6), 
        FILL_RATE(7), 
        REVENUE(8), 
        ECPM(9), 
        CLICKS(10), 
        CTR(11);

        private final Object dbValue;

        private PublisherSortBy(Object dbValue) {
            this.dbValue = dbValue;
        }

        public String getDbValue() {
            return dbValue.toString();
        }
    }

    public enum AgencyConsoleSortBy {
        ADVERTISER_NAME(1), 
        IMPRESSIONS(2), 
        CLICKS(3), 
        SPEND_YESTERDAY(4), 
        BALANCE(5), 
        SPEND(6);

        private final Object dbValue;

        private AgencyConsoleSortBy(Object dbValue) {
            this.dbValue = dbValue;
        }

        public String getDbValue() {
            return dbValue.toString();
        }
    }

}
