package com.byyd.adx;

import java.util.HashMap;
import java.util.Map;

import com.byyd.ortb.CreativeAttribute;

/**
 * @author mvanek
 * 
 * Compiled from
 * https://commondatastorage.googleapis.com/adx-rtb-dictionaries/creative-attributes.txt
 * 
 * In AdX Bid Request
 * https://storage.googleapis.com/adx-rtb-dictionaries/publisher-excludable-creative-attributes.txt
 * 
 * In AdX Bid Response
 * https://storage.googleapis.com/adx-rtb-dictionaries/buyer-declarable-creative-attributes.txt
 * 
 * In AdX Pretargeting - https://www.google.com/adx/Main.html#PRETARGETING
 * https://storage.googleapis.com/adx-rtb-dictionaries/pretargetable-creative-attributes.txt
 */
public enum AdxCreativeAttribute {

    Text(1, CreativeAttribute.TEXT_ONLY, false, false), // CreativeType
    Image_Rich_Media(2, null, false, false), // CreativeType

    Adobe_Flash_FLV(3, null, false, false), // VideoType
    RealPlayer(4, null, false, false), // VideoType
    QuickTime(5, null, false, false), // VideoType
    WindowsMedia(6, null, false, false), // VideoType

    IsTagged(7, null, true, true), // Tagging
    IsCookieTargeted(8, null, true, true), //  CookieTargeting
    IsUserInterestTargeted(9, null, true, true), // UserInterestTargeting

    ExpandingNone(12, null, true, true), // ExpandingDirection
    ExpandingUp(13, null, true, true), ExpandingDown(14, null, true, true), // ExpandingDirection
    ExpandingLeft(15, null, true, true), ExpandingRight(16, null, true, true), // ExpandingDirection
    ExpandingUpLeft(17, null, true, true), ExpandingUpRight(18, null, true, true), // ExpandingDirection
    ExpandingDownLeft(19, null, true, true), ExpandingDownRight(20, null, true, true), // ExpandingDirection
    ExpandingUpOrDown(25, null, true, true), ExpandingLeftOrRight(26, null, true, true), // ExpandingDirection
    ExpandingAnyDiagonal(27, null, true, true), // ExpandingDirection
    RolloverToExpand(28, null, true, true), // ExpandingAction

    Html(21, null, true, false), // CreativeType
    VastVideo(22, null, true, false), // CreativeType

    VpaidFlash(30, null, true, true), // InstreamVastVideoType
    Mraid_1_0(32, null, true, true), // MraidType

    RichMediaCapabilityHTML4(33, null, false, false), // RichMediaCapabilityType
    RichMediaCapabilityFlash(34, null, true, true), // RichMediaCapabilityType
    RichMediaCapabilityHTML5(39, null, true, false), // RichMediaCapabilityType
    RichMediaCapabilityHTML5BasicSVG(40, null, false, false), // RichMediaCapabilityType
    RichMediaCapabilityHTML5SVGFilters(41, null, false, false), // RichMediaCapabilityType
    RichMediaCapabilityHTML5SVGFonts(42, null, false, false), // RichMediaCapabilityType
    RichMediaCapabilityLargeExpandable(43, null, false, false), //RichMediaCapabilityType

    SkippableInstreamVideo(44, null, false, false), //InstreamVastVideoType
    NonSkippableInstreamVideo(69, null, false, false), //InstreamVastVideoType

    RichMediaCapabilitySSL(47, null, false, true), // RichMediaCapabilityType
    RichMediaCapabilityNonSSL(48, null, true, false), // RichMediaCapabilityType
    RichMediaCapabilityNonFlash(50, null, false, true), // RichMediaCapabilityType
    RichMediaCapabilityInterstitial(51, null, false, false), // RichMediaCapabilityType

    ExpandingGdnApi(52, null, true, false), // ExpandingDirection

    NativeEligible(70, null, true, true), // NativeEligibility
    NativeNotEligible(72, null, true, true); // NativeEligibility

    private final Integer adxId;

    private final CreativeAttribute oRtbAttribute;

    private final boolean excludable; //Can be in AdX Bid Request

    private final boolean declarable; //Can be in AdX Bid Response

    private static final Map<Integer, AdxCreativeAttribute> adxMap = new HashMap<Integer, AdxCreativeAttribute>();
    private static final Map<Integer, AdxCreativeAttribute> ortbMap = new HashMap<Integer, AdxCreativeAttribute>();

    static {
        for (AdxCreativeAttribute adxAttribute : AdxCreativeAttribute.values()) {

            adxMap.put(adxAttribute.adxId, adxAttribute);

            if (adxAttribute.oRtbAttribute != null) {
                Integer oRtbId = adxAttribute.oRtbAttribute.ordinal();
                if (ortbMap.containsKey(oRtbId)) {
                    System.err.println("Duplicit mapping OpenRTB " + oRtbId + " -> AdX " + ortbMap.get(oRtbId) + ", " + adxAttribute);
                } else {
                    ortbMap.put(oRtbId, adxAttribute);
                }
            }
        }
    }

    private AdxCreativeAttribute(int adXcode, CreativeAttribute oRtbAttribute, boolean excludable, boolean declarable) {
        this.adxId = adXcode;
        this.oRtbAttribute = oRtbAttribute;
        this.excludable = excludable;
        this.declarable = declarable;
    }

    public Integer getAdxId() {
        return adxId;
    }

    public CreativeAttribute getoRtbId() {
        return oRtbAttribute;
    }

    public boolean isExcludable() {
        return excludable;
    }

    public boolean isDeclarable() {
        return declarable;
    }

    public static AdxCreativeAttribute getByOrtbId(Integer oRtbCode) {
        return ortbMap.get(oRtbCode);
    }

    public static AdxCreativeAttribute getByAdxId(Integer adxAttributeCode) {
        return adxMap.get(adxAttributeCode);
    }

}
