package com.adfonic.tasks.xaudit.appnxs.dat;

import com.adfonic.tasks.xaudit.BaseExternalCreative;

/**
 * AppNexus creative object, most of this information is what/how they tell us 
 * to set it. AuditStatus is the most import status for byyd.
 * 
 * https://wiki.appnexus.com/display/adnexusdocumentation/Creative+Service#CreativeService-JSONstructure
 * 
 * @author graemeparker
 *
 */
public class AppNexusCreativeRecord implements BaseExternalCreative {

    public enum AuditStatus {
        no_audit, pending, rejected, unauditable, audited
    };

    /**
     * internal
     * The ID of the creative; used for internal matching purposes only.
     */
    private Integer id;

    /**
     * client
     * The member code of the creative; used for external ID mapping purposes only (see Bid Response).
     */
    private String code;

    /**
     * client
     * The ID of the member this creative belongs to.
     * This is specified in the URI of the API call and does not need to also be in the JSON.
     */
    private Integer member_id;

    /**
     * client
     * Information needed for mobile creatives to pass the creative audit.
     */
    private CreativeMobile mobile;

    /**
     * client
     * The URL of the creative - can be image, flash, html, javascript (see format). URL must exist and should be on a CDN or equivalent.
     */
    private String media_url;

    /**
     * client
     * The raw javascript or html content of the creative used instead of a media_url.
     */
    private String content;

    /**
     * audit team and client
     * 
     * The status of the audit. This field is set by the Appnexus creative auditing team. 
     * A creative that does not have audit_status "audited" may be resubmitted for audit by setting the audit_status of the creative to "pending."
     * 
     */
    private AuditStatus audit_status;

    /**
     * internal
     * If the creative has failed the creative audit for AppNexus, this includes the audit team's reasoning.
     */
    private String audit_feedback;

    /**
     * client
     * Set to true if you would like to opt the creative into the audit process.
     */
    private Boolean allow_audit;

    /**
     * client
     * The creative template for the creative's format and media type (i.e., flash and expandable). 
     * The template includes code to control how the creative renders on web pages.
     * 
     */
    private CreativeTemplate template;

    /**
     * client - The width of the creative in pixels.
     */
    private int width;

    /**
     * client - The height of the creative in pixels.
     */
    private int height;

    /**
     * When creative.format is image
     * Does not work for Bidder clients.
     */
    private String click_url;

    /**
     * Deprecated. Please use the pixels array instead. The (optional) URL of an impression pixel to be served along with the media URL.
     */
    private String pixel_url;

    /**
     * internal
     * Indicates whether the creative has been served or modified in the past 45 days
     */
    private Boolean is_expired;

    /**
     * Deprecated.
     */
    @Deprecated
    private boolean no_iframes;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getMedia_url() {
        return media_url;
    }

    public void setMedia_url(String media_url) {
        this.media_url = media_url;
    }

    public AuditStatus getAudit_status() {
        return audit_status;
    }

    public void setAudit_status(AuditStatus audit_status) {
        this.audit_status = audit_status;
    }

    public Boolean getAllow_audit() {
        return allow_audit;
    }

    public void setAllow_audit(Boolean allow_audit) {
        this.allow_audit = allow_audit;
    }

    public String getAudit_feedback() {
        return audit_feedback;
    }

    public void setAudit_feedback(String audit_feedback) {
        this.audit_feedback = audit_feedback;
    }

    public CreativeTemplate getTemplate() {
        return template;
    }

    public void setTemplate(CreativeTemplate template) {
        this.template = template;
    }

    public String getClick_url() {
        return click_url;
    }

    public void setClick_url(String click_url) {
        this.click_url = click_url;
    }

    public Boolean getIs_expired() {
        return is_expired;
    }

    public void setIs_expired(Boolean is_expired) {
        this.is_expired = is_expired;
    }

    public String getPixel_url() {
        return pixel_url;
    }

    public void setPixel_url(String pixel_url) {
        this.pixel_url = pixel_url;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getMember_id() {
        return member_id;
    }

    public void setMember_id(Integer member_id) {
        this.member_id = member_id;
    }

    public boolean isNo_iframes() {
        return no_iframes;
    }

    public void setNo_iframes(boolean no_iframes) {
        this.no_iframes = no_iframes;
    }

    public CreativeMobile getMobile() {
        return mobile;
    }

    public void setMobile(CreativeMobile mobile) {
        this.mobile = mobile;
    }

    /**
     * String representation of the object. Please keep this updated. 
     */
    @Override
    public String toString() {
        return "AppNexusCreativeRecord [id=" + id + ", width=" + width + ", height=" + height + ", media_url=" + media_url + ", audit_status=" + audit_status + ", allow_audit="
                + allow_audit + ", audit_feedback=" + audit_feedback + ", template=" + template + ", click_url=" + click_url + ", is_expired=" + is_expired + ", pixel_url="
                + pixel_url + ", content=" + content + ", member_id=" + member_id + ", no_iframes=" + no_iframes + ", mobile=" + mobile + "]";
    }
}
