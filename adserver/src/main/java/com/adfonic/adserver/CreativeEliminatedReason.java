package com.adfonic.adserver;

public enum CreativeEliminatedReason {
    countryNotTargeted, payoutLessThanMinBid, creativeWeightZero, revenueLessThenRevenueFloor, bidPriceLessThenFloorValue, //
    ecpmLessThenMinimumDefault, creativeStatusChanged, campaignStatusChanged, countryNotPresent, countryNotWhiteListed, //
    ConnectionTypeMismatch, OperatorNotWhitelisted, OperatorBlacklisted, //
    countryBlackListed, DeviceVendorModelMismatch, DeviceVendorMismatch, DeviceModelMismatch, DevicePlatformMismatch, DeviceModelExcluded, //
    creativeOrAdvertiserStopped, creativeEndDateExpired, formatNotAllowed, //
    beaconsNotPresent, smsNotPresent, GenderMismatch, AgeRangeMismatch, CapabilityNotRequired, CapabilityMismatch, //
    differentTimeOfTheDay, IpIsBlacklisted, IpNotWhitelisted, SiteAppMismatch, blockedDestinationType, blockedBidType, modelNotPresentForBackfill, //
    blockedPlugin, BrowsersMismatch, campaignNotCurrentlyActive, notClickToCallDevice, marketUrlForNotApplication, //
    blockedCampaignCategory, animatedCreativeOnOldAndroid, differentUserLanguage, differentPubblicationLanguage, displayTypeNotPresent, //
    NoAssetBundle, LanguageBlocked, DomainBlocked, ExtendedCreativeBlocked, NoIntegrationType, notAvailableMediaType, //
    missingContentForm, notAcceptedFeature, noSupportForBeacon, NoGeolocations, notInGeotargetArea, NoDeviceIdentifier, //
    differentDeviceIdentifier, alreadyInstalled, alreadyConverted, excludedByRetargeting, notIncludedByRetargeting, //
    blockedExtendedCreativeType, ClosedMode, Selected, NotSelected, PmpDealMismatch, NotInGeolocationArea, //
    explicitGeoLocationExpected, frequenyCapping, deviceGroupDidNotMatch, noDeviceInRequestForDeviceGroupTargetedCampaign, TrusteWeveIcon, RequestOrCreativeNotNative, //
    OptedOut, DeviceRedisMismatch, bidFloorIsNullInDealObject, //
    AdsquareMismatch, FactualMismatch, blockedCreativeAttributes, notExternalAuditApproved, notSslCompliant, //
    // Very special used in targeting segment eligibility scrachpad as a positive value  
    SegmentIsTargeted;
}
