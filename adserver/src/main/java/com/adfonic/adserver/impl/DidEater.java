package com.adfonic.adserver.impl;

public class DidEater {

    public static final int RAW_HEX_LENGTH = 36; // 32 hex + 4 hyphens
    public static final int MD5_HEX_LENGTH = 32;
    public static final int MD5_BYTES = 16; // 128 bits / 8
    public static final int SHA1_HEX_LENGTH = 40;
    public static final int SHA1_BYTES = 20; // 160 bits / 8

    @Deprecated
    // Old Android Device ID 
    // android.provider.Settings.System.getString(super.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
    public static final int ANDROID_ID_HEX_LENGTH = 16; // Which is odd because it is 64 bits number

    enum MobileOs {
        ANDROID, IOS, WINDOWS, BLACKBERY;
    }

    enum DigestType {
        MD5(MD5_HEX_LENGTH, MD5_BYTES), //  32 hex characters (128 bits)
        SHA1(SHA1_HEX_LENGTH, SHA1_BYTES); // 40 hex characters (160 bits)

        private final int length;

        private final int bytes;

        private DigestType(int length, int bytes) {
            this.length = length;
            this.bytes = bytes;
        }
    }

    enum RtbDidType {

        /**
         * iOS IFA (Apple Identifier for Advertisers)
         * iOS 6+
         * https://developer.apple.com/library/ios/documentation/AdSupport/Reference/ASIdentifierManager_Ref/index.html
         * 
         * 36 uppercase characters = 32 characters + 4 hyphens
         * 8-4-4-4-12, example: AEBE52E7-03EE-455A-B3C4-E57283966239
         */
        IOS_IDFA,

        /**
         * iOS < 5
         * Every iOS device comes with a Unique Device Identifier (UDID). 
         * With the release of iOS6, the UDID has been replaced with the Identifier for Advertising (IDFA). - See more at: http://blog.getsocialize.com/2013/everything-you-need-to-know-about-ioss-idfa-idv-cookies-overview#sthash.IvHc21kk.QIpbk1y2.dpuf
         * 
         * 40 lowercase characters, example: 2b6f0cc904d137be2e1730235f5664094b831186
         */
        IOS_UDID,

        /**
         * Android Advertising ID (Google Advertising Identifier)
         * 
         * 36 uppercase characters = 32 characters + 4 hyphens
         * 8-4-4-4-12, example: 38400000-8cf0-11bd-b23e-10b96e40000d
         */
        ANDROID_ADID,

        /**
         * Android ID
         * 
         * The ANDROID ID for Android devices is a 64-bit number (as a hex string) 
         * that is randomly generated on the first boot of a device and typically 
         * remains constant for the lifetime of the device. 
         * This value is formatted as lower case.
         * 
         * 16 hex characters (64 bit) If it is shorter, then leading zeroes are omitted
         */
        ANDROID_ID,

        /**
         * Windows AID (Windows Advertising Identifier)
         * 
         * The Windows advertising identifier (AID) is a unique, user and device-specific, and resettable ID for advertising 
         * represented as an alphanumeric string formatted as upper case without colons (for example, “AAAAAABBBBCCCC111122222222222”).
         * When the advertising ID feature is disabled, this value is an empty string.
         * 
         * 0A74DAD344E5A57724EDC0847D1F57B1, Nokia RM 915, Windows Phone 7 - 31c32d8ffeb96223e1ee1a348fba6876
         * 
         * 32 hex characters
         */
        WIN_AID,

        DPID, GGL_UID, ODIN;
    }

    public void eat(String identifier, RtbDidType type, DigestType digest) {
        int length = identifier.length();
        if (type == RtbDidType.IOS_IDFA) {
            if (length == RAW_HEX_LENGTH && identifier.charAt(8) == '-' && identifier.charAt(12) == '-' && identifier.charAt(16) == '-' && identifier.charAt(20) == '-') {
                // really raw ifa
                identifier = identifier.toUpperCase(); // canonical IDFA is uppercase
            } else if (length == MD5_HEX_LENGTH) {
                if (digest == null || digest == DigestType.MD5) {
                    // right
                } else {
                    // wrong 
                }

            } else if (length == SHA1_HEX_LENGTH) {

                if (digest == null || digest == DigestType.SHA1) {
                    // really SHA1(ifa)
                } else {
                    // SHA1(ifa) or UDID    
                }
            } else {
                // wrong
            }
        }
    }

    private String uuid(String identifier, RtbDidType type, DigestType digest) {
        return null;
    }

}
