package com.adfonic.domain;

/** This distinguishes the type of trackingIdentifier that a publisher passes
    to us.  It may be a device UDID (i.e. when an iPhone or Android app is
    hitting us), it may have come from a cookie that we generated for the
    end user directly.
*/
public enum TrackingIdentifierType {
    DEVICE, COOKIE, PUBLISHER_GENERATED, NONE;
}
