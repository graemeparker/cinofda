package com.adfonic.domain.cache.dto.adserver.adspace;

import com.adfonic.domain.RtbConfig;
import com.adfonic.domain.RtbConfig.RtbAuctionType;
import com.adfonic.domain.RtbConfig.RtbImpTrackMode;
import com.adfonic.domain.cache.dto.BusinessKeyDto;

public class RtbConfigDto extends BusinessKeyDto {

    private static final long serialVersionUID = 13L;

    private RtbConfig.RtbAdMode adMode;
    private RtbConfig.RtbWinNoticeMode winNoticeMode;
    private RtbConfig.AdmProfile admProfile;

    private RtbConfig.DecryptionScheme decryptionScheme;

    private String spMacro;
    private String escapedClickForwardURL;
    private String clickForwardValidationPattern;
    private String dpidFallback;
    private String prefixonEscapedURLs;
    private String integrationTypePrefix;
    private String bidCurrency;
    private RtbAuctionType auctionType;

    private long rtbLostTimeDuration;

    private boolean sslRequired;

    private String securityAlias;

    private RtbImpTrackMode impTrackMode;

    public RtbConfig.DecryptionScheme getDecryptionScheme() {
        return decryptionScheme;
    }

    public void setDecryptionScheme(RtbConfig.DecryptionScheme decryptionScheme) {
        this.decryptionScheme = decryptionScheme;
    }

    public RtbConfig.RtbAdMode getAdMode() {
        return adMode;
    }

    public void setAdMode(RtbConfig.RtbAdMode adMode) {
        this.adMode = adMode;
    }

    public RtbConfig.RtbWinNoticeMode getWinNoticeMode() {
        return winNoticeMode;
    }

    public void setWinNoticeMode(RtbConfig.RtbWinNoticeMode winNoticeMode) {
        this.winNoticeMode = winNoticeMode;
    }

    public RtbConfig.AdmProfile getAdmProfile() {
        return admProfile;
    }

    public void setAdmProfile(RtbConfig.AdmProfile admProfile) {
        this.admProfile = admProfile;
    }

    public String getSpMacro() {
        return spMacro;
    }

    public void setSpMacro(String spMacro) {
        this.spMacro = spMacro;
    }

    public String getEscapedClickForwardURL() {
        return escapedClickForwardURL;
    }

    public void setEscapedClickForwardURL(String escapedClickForwardURL) {
        this.escapedClickForwardURL = escapedClickForwardURL;
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

    public String getPrefixonEscapedURLs() {
        return prefixonEscapedURLs;
    }

    public void setPrefixonEscapedURLs(String prefixonEscapedURLs) {
        this.prefixonEscapedURLs = prefixonEscapedURLs;
    }

    public String getIntegrationTypePrefix() {
        return integrationTypePrefix;
    }

    public void setIntegrationTypePrefix(String integrationTypePrefix) {
        this.integrationTypePrefix = integrationTypePrefix;
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

    public long getRtbLostTimeDuration() {
        return rtbLostTimeDuration;
    }

    public void setRtbLostTimeDuration(long rtbLostTimeDuration) {
        this.rtbLostTimeDuration = rtbLostTimeDuration;
    }

    public boolean isSslRequired() {
        return sslRequired;
    }

    public void setSslRequired(boolean sslRequired) {
        this.sslRequired = sslRequired;
    }

    public String getSecurityAlias() {
        return securityAlias;
    }

    public void setSecurityAlias(String securityAlias) {
        this.securityAlias = securityAlias;
    }

    public void setImpTrackMode(RtbImpTrackMode impTrackMode) {
        this.impTrackMode = impTrackMode;
    }

    public RtbImpTrackMode getImpTrackMode() {
        return impTrackMode;
    }

    @Override
    public String toString() {
        return "RtbConfigDto { " + getId() + ", adMode=" + adMode + ", winNoticeMode=" + winNoticeMode + ", admProfile=" + admProfile + ", decryptionScheme=" + decryptionScheme
                + ", spMacro=" + spMacro + ", escapedClickForwardURL=" + escapedClickForwardURL + ", clickForwardValidationPattern=" + clickForwardValidationPattern
                + ", dpidFallback=" + dpidFallback + ", prefixonEscapedURLs=" + prefixonEscapedURLs + ", integrationTypePrefix=" + integrationTypePrefix + ", bidCurrency="
                + bidCurrency + ", auctionType=" + auctionType + ", rtbLostTimeDuration=" + rtbLostTimeDuration + "}";
    }

}
