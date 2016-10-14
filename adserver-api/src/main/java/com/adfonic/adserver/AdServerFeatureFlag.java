package com.adfonic.adserver;

public enum AdServerFeatureFlag {

    OFFENCE_REGISTRY(false), LONG_REQUEST_REAPER(false), RTB_STATS(true), OMAX_VIDEO(false), OMAX_NATIVE(false), MOBFOX_VIDEO(false), MOBFOX_NATIVE(false), EXCHANGE_IN_URL(false);

    private boolean enabled;

    private AdServerFeatureFlag() {
        this(false);
    }

    private AdServerFeatureFlag(boolean defaultValue) {
        this.enabled = defaultValue;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
