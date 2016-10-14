package com.adfonic.adserver.rtb.util;

public enum AsCounter {

    DeviceRedisCall, DeviceRedisError, GeoRedisCall, GeoRedisError, //
    AdsquareEnrichCall, AdsquareEnrichError, AdsquareCassandraInsertCall, AdsquareCassandraInsertError, //
    AdsquareCassandraSelectCall, AdsquareCassandraSelectError, AdsquareImpressionTrackCall, AdsquareImpressionTrackError, AdsquareClickTrackCall, AdsquareClickTrackError, //
    FactualAudienceCall, FactualAudienceError, FactualProximityCall, FactualProximityError, //
    LossNotification, RtbLoss, LossNotificationError, LossNotificationRtbDetailsNotFound, //
    QuovaCalls, QuovaError, // 
    RenderError, //
    BidRequest, BidCapable, BidDeduced, BidResponse, BidError, // 
    ClickWithImpression, ClickImpressionNotFound, ClickAdSpaceNotFound, ClickAdSpaceMismatch, ClickCreativeNotFound, ClickRepeated, ClickCompleted, ClickError, //
    ClickDeviceModelMismatch, ClickCountryMismatch, //
    WinOnImpression, WinOnRtbNurl, WinCompleted, WinRtbDetailsNotFound, WinAdSpaceNotFound, WinCreativeNotFound, WinPriceMissing, WinPriceError, //
    BeaconWithImpression, BeaconImpressionNotFound, BeaconAdSpaceNotFound, BeaconAdSpaceMismatch, BeaconCreativeNotFound, BeaconRepeated, BeaconCompleted, //
    BeaconDeviceModelMismatch, BeaconCountryMismatch, //
    VastImpressionNotFound, VastAdSpaceNotFound, VastAdSpaceMismatch, VastCreativeNotFound, VastCompleted,
    // Temporary Omax and MobFox counters. Only for onboarding/certification period
    OmaxNative, OmaxVideo, MobFoxNative, MobFoxVideo, AdsqaureTrackerError;
}
