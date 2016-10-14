package com.adfonic.domain;

/**
 * Represents the fallback action to take when no creative is available
 * for a given AdSpace.
 *
 * NO_AD means display nothing.
 * HOUSE_AD will eventually pull from this company's own ads where applicable.
 * TEST_AD will display the test pattern.
 * BLANK_SPACE will display a coloured block (at least for banners).
 */
public enum UnfilledAction {
    NO_AD, HOUSE_AD, TEST_AD, BLANK_SPACE;
}
