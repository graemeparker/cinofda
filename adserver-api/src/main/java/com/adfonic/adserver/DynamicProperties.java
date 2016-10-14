package com.adfonic.adserver;

import java.util.Map;
import java.util.Set;

import com.adfonic.util.StringUtils;

public interface DynamicProperties {

    public enum DcProperty {

        /**
         * http://as.byyd.net/as/
         */
        AssetBaseUrl("asset.base.url"), // 
        /**
         * http://as.byyd.net/pixel.gif
         */
        AssetPixelUrl("asset.pixel.url"), // 
        /**
         * http://weve.byyd.net/weve/bc/
         */
        WeveBeaconUrl("weve.beacon.url"), // 
        /**
         * http://tracker.adfonic.net
         */
        TrackerBaseUrl("tracker.base.url"), //
        /**
         * false
         */
        TrackerRedirection("tracker.redirection"), //
        /**
         * http://choices.truste.com/ca
         */
        TrusteChoicesUrl("truste_choices_url"), //
        /**
         * http://as.byyd.net/adtruth/prefs.js
         */
        AdtruthPrefsJsUrl("adtruth_prefs_js_url"), //

        AdsquareCountries("adsquare.countries"), //

        TrackAdsquareApm("track.adsquare.apm"), // Not permanent property - used only generate reports for Adsqaure out of tracker access logs

        WeveAdvertisers("weve.company_ids"), //
        TrusteWevePid("truste.weve.pid"), TrusteWeveWebAid("truste.weve.web-aid"), TrusteWeveAppAid("truste.weve.app-aid"), //
        TrusteDefaultAeskey("truste.default.aeskey"), //
        TrusteDefaultPid("truste.default.pid"), TrusteDefaultWebAid("truste.default.web-aid"), TrusteDefaultAppAid("truste.default.app-aid");

        private final String dbKey;

        private DcProperty(String dbKey) {
            this.dbKey = dbKey;
        }

        public String getDbKey() {
            return dbKey;
        }
    }

    public String getProperty(DcProperty keyName);

    public String getProperty(DcProperty keyName, String defaultValue);

    /**
     * TODO it is stupid to parse it every time...
     */
    default public Set<String> getPropertyAsSet(DcProperty keyName, Set<String> defaultValue) {
        String propertyValue = getProperty(keyName);
        if (propertyValue != null) {
            return StringUtils.toSetOfStrings(propertyValue, ",");
        } else {
            return defaultValue;
        }
    }

    /**
     * For testing puroses, this comes handy...
     */
    public static class StaticDynamicProperties implements DynamicProperties {

        private final Map<DcProperty, String> values;

        public StaticDynamicProperties(Map<DcProperty, String> values) {
            this.values = values;
        }

        @Override
        public String getProperty(DcProperty keyName) {
            return this.values.get(keyName);
        }

        @Override
        public String getProperty(DcProperty keyName, String defaultValue) {
            String value = this.values.get(keyName);
            if (value != null) {
                return value;
            } else {
                return defaultValue;
            }
        }

    }

}