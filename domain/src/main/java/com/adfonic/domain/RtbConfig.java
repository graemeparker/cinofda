package com.adfonic.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Represents a Rtb Config for a Publisher.
 */
@Entity
@Table(name = "RTB_CONFIG")
public class RtbConfig extends BusinessKey {
    private static final long serialVersionUID = 17L;

    public enum RtbAdMode {
        WIN_NOTICE, BID
    };

    public enum RtbWinNoticeMode {
        OPEN_RTB, BEACON
    };

    public enum RtbAuctionType {
        FIRST_PRICE, SECOND_PRICE
    };

    public enum RtbImpTrackMode {
        AD_MARKUP, // classic ad markup pixel
        RTB_RESPONSE // custom field in rtb response
    };

    public enum AdmProfile {
        STANDARD("markupGeneratorImpl"), SMAATO_ADM("smaatoAdMarkupGenerator"), ADX_ADM("adXAdMarkupGenerator"), APPNXS_ADM("appNexusMarkupGenerator");

        private final String implClass;

        private AdmProfile(String implClass) {
            this.implClass = implClass;
        }

        public String impl() {
            return implClass;
        }
    };

    public enum DecryptionScheme {
        ADX, OPENX, RBCN_BF
    }

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private long id;

    @Column(name = "AD_MODE", nullable = false)
    @Enumerated(EnumType.STRING)
    private RtbAdMode adMode;

    @Column(name = "WIN_NOTICE_MODE", nullable = false)
    @Enumerated(EnumType.STRING)
    private RtbWinNoticeMode winNoticeMode;

    @Column(name = "ADM_PROFILE", nullable = false)
    @Enumerated(EnumType.STRING)
    private AdmProfile admProfile;

    @Column(name = "SP_MACRO", length = 255, nullable = false)
    private String settlementPriceMacro;

    @Column(name = "ESCD_CLICK_FORWARD_URL", length = 255, nullable = true)
    private String escapedClickForwardUrl;

    @Column(name = "CLCK_FWD_VALDN_PATTERN", length = 255, nullable = true)
    private String clickForwardValidationPattern;

    @Column(name = "DPID_FALLBACK", length = 32, nullable = true)
    private String dpidFallback;

    @Column(name = "BID_CURRENCY")
    private String bidCurrency;

    @Column(name = "DECRYPTION_SCHEME", length = 255, nullable = true)
    @Enumerated(EnumType.STRING)
    private DecryptionScheme decryptionScheme;

    @Column(name = "AUCTION_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private RtbAuctionType auctionType;

    @Column(name = "SSL_REQUIRED", nullable = false)
    private boolean sslRequired; // MAD-1738

    @Column(name = "INTEGRATION_TYPE_PREFIX", length = 64, nullable = true)
    private String integrationTypePrefix;

    @Column(name = "ESCAPED_URL_PREFIX", length = 255, nullable = true)
    private String escapedUrlPrefix;

    @Column(name = "SEC_ALIAS", length = 255, nullable = true)
    private String secAlias;

    @Column(name = "BID_EXPIRY_TIME_SECONDS", nullable = false)
    private Integer rtbExpirySeconds;

    @Column(name = "IMP_TRACK_MODE", nullable = false)
    @Enumerated(EnumType.STRING)
    private RtbImpTrackMode impTrackMode;

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public RtbAdMode getAdMode() {
        return adMode;
    }

    public void setAdMode(RtbAdMode adMode) {
        this.adMode = adMode;
    }

    public RtbWinNoticeMode getWinNoticeMode() {
        return winNoticeMode;
    }

    public void setWinNoticeMode(RtbWinNoticeMode winNoticeMode) {
        this.winNoticeMode = winNoticeMode;
    }

    public AdmProfile getAdmProfile() {
        return admProfile;
    }

    public void setAdmProfile(AdmProfile admProfile) {
        this.admProfile = admProfile;
    }

    public String getSettlementPriceMacro() {
        return settlementPriceMacro;
    }

    public void setSettlementPriceMacro(String settlementPriceMacro) {
        this.settlementPriceMacro = settlementPriceMacro;
    }

    public String getEscapedClickForwardUrl() {
        return escapedClickForwardUrl;
    }

    public void setEscapedClickForwardUrl(String escapedClickForwardUrl) {
        this.escapedClickForwardUrl = escapedClickForwardUrl;
    }

    public String getClickForwardValidationPattern() {
        return clickForwardValidationPattern;
    }

    public void setClickForwardValidationPattern(String clickForwardValidationPattern) {
        this.clickForwardValidationPattern = clickForwardValidationPattern;
    }

    public String getDpidFallback() {
        return dpidFallback;
    }

    public void setDpidFallback(String dpidFallback) {
        this.dpidFallback = dpidFallback;
    }

    public String getBidCurrency() {
        return bidCurrency;
    }

    public void setBidCurrency(String bidCurrency) {
        this.bidCurrency = bidCurrency;
    }

    public RtbAuctionType getAuctionType() {
        return auctionType;
    }

    public void setAuctionType(RtbAuctionType auctionType) {
        this.auctionType = auctionType;
    }

    public boolean isSslRequired() {
        return sslRequired;
    }

    public void setSslRequired(boolean httpsRequired) {
        this.sslRequired = httpsRequired;
    }

    public DecryptionScheme getDecryptionScheme() {
        return decryptionScheme;
    }

    public void setDecryptionScheme(DecryptionScheme decryptionScheme) {
        this.decryptionScheme = decryptionScheme;
    }

    public String getIntegrationTypePrefix() {
        return integrationTypePrefix;
    }

    public void setIntegrationTypePrefix(String integrationTypePrefix) {
        this.integrationTypePrefix = integrationTypePrefix;
    }

    public String getEscapedUrlPrefix() {
        return escapedUrlPrefix;
    }

    public void setEscapedUrlPrefix(String escapedUrlPrefix) {
        this.escapedUrlPrefix = escapedUrlPrefix;
    }

    public String getSecAlias() {
        return secAlias;
    }

    public void setSecAlias(String secAlias) {
        this.secAlias = secAlias;
    }

    public Integer getRtbExpirySeconds() {
        return rtbExpirySeconds;
    }

    public void setRtbExpirySeconds(Integer rtbExpirySeconds) {
        this.rtbExpirySeconds = rtbExpirySeconds;
    }

    public RtbImpTrackMode getImpTrackMode() {
        return impTrackMode;
    }

    public void setImpTrackMode(RtbImpTrackMode impTrackMode) {
        this.impTrackMode = impTrackMode;
    }
}
