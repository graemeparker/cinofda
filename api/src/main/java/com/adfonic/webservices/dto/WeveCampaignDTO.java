package com.adfonic.webservices.dto;

import static com.adfonic.domain.Campaign.Status.ACTIVE;
import static com.adfonic.domain.Campaign.Status.COMPLETED;
import static com.adfonic.domain.Campaign.Status.PAUSED;
import static com.adfonic.domain.Campaign.Status.PENDING;
import static com.adfonic.domain.Campaign.Status.STOPPED;

import java.math.BigDecimal;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.adfonic.domain.Campaign;
import com.adfonic.domain.Campaign.BudgetType;
import com.adfonic.webservices.annotations.BlockIfCampaignIn;

@XmlRootElement(name = "campaign")
public class WeveCampaignDTO {

    private String id;// externalID

    @BlockIfCampaignIn
    private String name;

    @BlockIfCampaignIn
    private String defaultLanguage;// defaultLanguage.isoCode

    @BlockIfCampaignIn
    private Campaign.Status status;

    @BlockIfCampaignIn({PENDING, ACTIVE, PAUSED, STOPPED, COMPLETED})
    private String description;

    private Set<CampaignTimePeriodDTO> timePeriods;

    private BigDecimal dailyBudget; // null if none

    private BigDecimal dailyBudgetWeekday; // null if none

    private BigDecimal dailyBudgetWeekend; // null if none

    private BigDecimal overallBudget; // null if none

    @BlockIfCampaignIn
    private BigDecimal overallSpend;// Effectively overallSpend.amount. But can only map directly

    private Boolean overallBudgetAlertEnabled;

    private Boolean dailyBudgetAlertEnabled;

    private Integer capImpressions;

    private Integer capPeriodSeconds;
    
    private Set<CampaignAudienceDTO> audiences;

    private Boolean installTrackingEnabled;

    @BlockIfCampaignIn
    private Boolean installTrackingVerified;

    private String applicationID;

    private String reference;

    private Boolean conversionTrackingEnabled;

    @BlockIfCampaignIn
    private Boolean conversionTrackingVerified;

    private Boolean houseAd;

    private CampaignBidDTO bid;// currentBid

    // Weve badness
    private String segment="Missing field";
    
    //inventory targeting AI-249
    private String publicationList;

    // TODO - reconsider
    // private List<CreativeDTO> creatives;

    @XmlTransient
    @JsonIgnore
    private BigDecimal overallBudgetImpressions; // null if none

    @XmlTransient
    @JsonIgnore
    private BigDecimal overallBudgetClicks; // null if none

    @XmlTransient
    @JsonIgnore
    private BigDecimal dailyBudgetImpressions; // null if none

    @XmlTransient
    @JsonIgnore
    private BigDecimal dailyBudgetClicks; // null if none

    private Boolean evenDistributionOverallBudget;
    private Boolean evenDistributionDailyBudget;
    private BudgetType budgetType;

    public Boolean getEvenDistributionOverallBudget() {
        return evenDistributionOverallBudget;
    }


    public void setEvenDistributionOverallBudget(Boolean evenDistributionOverallBudget) {
        this.evenDistributionOverallBudget = evenDistributionOverallBudget;
    }


    public Boolean getEvenDistributionDailyBudget() {
        return evenDistributionDailyBudget;
    }


    public void setEvenDistributionDailyBudget(Boolean evenDistributionDailyBudget) {
        this.evenDistributionDailyBudget = evenDistributionDailyBudget;
    }


    public BudgetType getBudgetType() {
        return budgetType;
    }


    public void setBudgetType(BudgetType budgetType) {
        this.budgetType = budgetType;
    }


    @XmlTransient
    @JsonIgnore
    public BigDecimal getOverallBudgetImpressions() {
        return overallBudgetImpressions;
    }


    public void setOverallBudgetImpressions(BigDecimal overallBudgetImpressions) {
        this.overallBudgetImpressions = overallBudgetImpressions;
    }


    @XmlTransient
    @JsonIgnore
    public BigDecimal getOverallBudgetClicks() {
        return overallBudgetClicks;
    }


    public void setOverallBudgetClicks(BigDecimal overallBudgetClicks) {
        this.overallBudgetClicks = overallBudgetClicks;
    }


    @XmlTransient
    @JsonIgnore
    public BigDecimal getDailyBudgetImpressions() {
        return dailyBudgetImpressions;
    }


    public void setDailyBudgetImpressions(BigDecimal dailyBudgetImpressions) {
        this.dailyBudgetImpressions = dailyBudgetImpressions;
    }


    @XmlTransient
    @JsonIgnore
    public BigDecimal getDailyBudgetClicks() {
        return dailyBudgetClicks;
    }


    public void setDailyBudgetClicks(BigDecimal dailyBudgetClicks) {
        this.dailyBudgetClicks = dailyBudgetClicks;
    }


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public String getDefaultLanguage() {
        return defaultLanguage;
    }


    public void setDefaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }


    public Campaign.Status getStatus() {
        return status;
    }


    public void setStatus(Campaign.Status status) {
        this.status = status;
    }


    public String getDescription() {
        return description;
    }


    public void setDescription(String description) {
        this.description = description;
    }


    @XmlElementWrapper(name="timePeriods")
    @XmlElement(name="timePeriod")
    public Set<CampaignTimePeriodDTO> getTimePeriods() {
        return timePeriods;
    }


    public void setTimePeriods(Set<CampaignTimePeriodDTO> timePeriods) {
        this.timePeriods = timePeriods;
    }


    public BigDecimal getDailyBudget() {
        if (budgetType == null) {
            return dailyBudget;
        }

        switch (budgetType) {
        case CLICKS:
            return scaled0(dailyBudgetClicks);
        case IMPRESSIONS:
            return scaled0(dailyBudgetImpressions);
        case MONETARY:
        default:
            return dailyBudget;
        }
    }

    public void setDailyBudget(BigDecimal dailyBudget) {
        this.dailyBudget = dailyBudget;
    }


    public BigDecimal getDailyBudgetWeekday() {
        return dailyBudgetWeekday;
    }


    public void setDailyBudgetWeekday(BigDecimal dailyBudgetWeekday) {
        this.dailyBudgetWeekday = dailyBudgetWeekday;
    }


    public BigDecimal getDailyBudgetWeekend() {
        return dailyBudgetWeekend;
    }


    public void setDailyBudgetWeekend(BigDecimal dailyBudgetWeekend) {
        this.dailyBudgetWeekend = dailyBudgetWeekend;
    }


    public BigDecimal getOverallBudget() {
        if (budgetType == null) {
            return overallBudget;
        }

        switch (budgetType) {
        case CLICKS:
            return scaled0(overallBudgetClicks);
        case IMPRESSIONS:
            return scaled0(overallBudgetImpressions);
        case MONETARY:
        default:
            return overallBudget;
        }
    }

    
    private static BigDecimal scaled0(BigDecimal bd) {
        return bd == null ? null : bd.setScale(0);
    }


    public void setOverallBudget(BigDecimal overallBudget) {
        this.overallBudget = overallBudget;
    }


    public BigDecimal getOverallSpend() {
        return overallSpend;
    }


    public void setOverallSpend(BigDecimal overallSpend) {
        this.overallSpend = overallSpend;
    }


    public Boolean getOverallBudgetAlertEnabled() {
        return overallBudgetAlertEnabled;
    }


    public void setOverallBudgetAlertEnabled(Boolean overallBudgetAlertEnabled) {
        this.overallBudgetAlertEnabled = overallBudgetAlertEnabled;
    }


    public Boolean getDailyBudgetAlertEnabled() {
        return dailyBudgetAlertEnabled;
    }


    public void setDailyBudgetAlertEnabled(Boolean dailyBudgetAlertEnabled) {
        this.dailyBudgetAlertEnabled = dailyBudgetAlertEnabled;
    }


    public Integer getCapImpressions() {
        return capImpressions;
    }


    public void setCapImpressions(Integer capImpressions) {
        this.capImpressions = capImpressions;
    }


    public Integer getCapPeriodSeconds() {
        return capPeriodSeconds;
    }


    public void setCapPeriodSeconds(Integer capPeriodSeconds) {
        this.capPeriodSeconds = capPeriodSeconds;
    }


    public Boolean getInstallTrackingEnabled() {
        return installTrackingEnabled;
    }


    public void setInstallTrackingEnabled(Boolean installTrackingEnabled) {
        this.installTrackingEnabled = installTrackingEnabled;
    }


    public Boolean getInstallTrackingVerified() {
        return installTrackingVerified;
    }


    public void setInstallTrackingVerified(Boolean installTrackingVerified) {
        this.installTrackingVerified = installTrackingVerified;
    }


    public String getApplicationID() {
        return applicationID;
    }


    public void setApplicationID(String applicationID) {
        this.applicationID = applicationID;
    }


    public Boolean getConversionTrackingEnabled() {
        return conversionTrackingEnabled;
    }


    public void setConversionTrackingEnabled(Boolean conversionTrackingEnabled) {
        this.conversionTrackingEnabled = conversionTrackingEnabled;
    }


    public Boolean getConversionTrackingVerified() {
        return conversionTrackingVerified;
    }


    public void setConversionTrackingVerified(Boolean conversionTrackingVerified) {
        this.conversionTrackingVerified = conversionTrackingVerified;
    }


    public Boolean getHouseAd() {
        return houseAd;
    }


    public void setHouseAd(Boolean houseAd) {
        this.houseAd = houseAd;
    }


    public CampaignBidDTO getBid() {
        return bid;
    }


    public void setBid(CampaignBidDTO bid) {
        this.bid = bid;
    }


    public String getSegment() {
        return segment;
    }


    public void setSegment(String segment) {
    }

    public void overrideSeg(String segOverride) {
        this.segment = segOverride;
    }

    public String getPublicationList() {
        return publicationList;
    }


    public void setPublicationList(String publicationList) {
        this.publicationList = publicationList;
    }


    public String getReference() {
        return reference;
    }


    public void setReference(String reference) {
        this.reference = reference;
    }


    @XmlElementWrapper(name="audiences")
    @XmlElement(name="audience")
    public Set<CampaignAudienceDTO> getAudiences() {
        return audiences;
    }


    public void setAudiences(Set<CampaignAudienceDTO> audiences) {
        this.audiences = audiences;
    }


}
