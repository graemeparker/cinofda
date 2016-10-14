package com.adfonic.tools.beans.campaign.targeting;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.ElasticsearchException;
import org.primefaces.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.dto.audience.AudienceDto;
import com.adfonic.dto.audience.CampaignAudienceDto;
import com.adfonic.dto.audience.enums.AudienceRecencyType;
import com.adfonic.dto.audience.enums.AudienceType;
import com.adfonic.dto.campaign.CampaignDto;
import com.adfonic.dto.user.UserDTO;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.audience.enums.FileType;
import com.adfonic.presentation.audience.service.AudienceFileService;
import com.adfonic.presentation.audience.service.AudienceService;
import com.adfonic.presentation.location.model.GeoLocationModel;
import com.adfonic.presentation.util.DateUtils;
import com.adfonic.tools.beans.application.ToolsApplicationBean;
import com.adfonic.tools.beans.audience.source.AudienceSourceMBean;
import com.adfonic.tools.beans.util.Constants;
import com.adfonic.tools.beans.util.GenericAbstractBean;

@Component
@Scope("view")
public class CampaignTargetingAudienceMBean extends GenericAbstractBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(CampaignTargetingAudienceMBean.class);

    @Autowired
    private AudienceService aService;
    
    @Autowired
    private AudienceFileService audienceFileService;

    @Autowired
    private ToolsApplicationBean toolsApplicationBean;

    private CampaignDto campaignDto;

    private List<AudienceDto> audiences = null;

    private List<CampaignAudienceDto> campaignAudiences = new ArrayList<CampaignAudienceDto>();

    private Double campaignDataFee = null;

    private Integer audienceIndexToRemove = null;
    
    private Boolean elasticHealthy = Boolean.TRUE;
    
    @Override
    public void init() {
        // empty
    }

    public CampaignDto prepareDto(CampaignDto dto) {
        LOGGER.debug("prepareDto-->");

        List<CampaignAudienceDto> audiencesList = new ArrayList<CampaignAudienceDto>();
        for (CampaignAudienceDto ca : campaignAudiences) {
            if (ca.getAudience() != null) {
                resolveRecencyFields(ca, true);
                audiencesList.add(ca);
            }
        }
        dto.setCampaignAudiences(audiencesList);

        LOGGER.debug("prepareDto<--");
        return dto;
    }

    public void loadCampaignDto(CampaignDto dto) {
        LOGGER.debug("loadCampaignDto-->");
        this.campaignDto = dto;

        // Retrieving audiences
        if (campaignDto != null) {
            if (CollectionUtils.isEmpty(campaignDto.getCampaignAudiences())) {
                this.campaignAudiences = new ArrayList<CampaignAudienceDto>();
            } else {
                this.campaignAudiences = campaignDto.getCampaignAudiences();
                for (CampaignAudienceDto ca : campaignAudiences) {
                    resolveRecencyFields(ca, false);
                    resolveAudienceSize(ca);
                }
            }
        }

        // Retrieving datafee
        if ((campaignDto != null) && (campaignDto.getCurrentDataFee() != null)) {
            this.campaignDataFee = campaignDto.getCurrentDataFee().getDataFee().doubleValue();
        } else {
            this.campaignDataFee = null;
        }

        LOGGER.debug("loadCampaignDto<--");
    }

    private void resolveRecencyFields(CampaignAudienceDto ca, boolean fromUI) {
        // Save from UI to DB
        if (fromUI) {

            // Set recency type NONE if null
            if (ca.getAudienceRecencyType() == null) {
                ca.setAudienceRecencyType(AudienceRecencyType.NONE);
            }

            // Set either recency range or window
            switch (ca.getAudienceRecencyType()) {
            case RANGE:
                ca.setRecencyDaysFrom(null);
                ca.setRecencyDaysTo(null);
                break;
            case WINDOW:
                ca.setRecencyDateFrom(null);
                ca.setRecencyDateTo(null);
                break;
            case NONE:
            case NA:
            default:
                ca.setRecencyDaysFrom(null);
                ca.setRecencyDaysTo(null);
                ca.setRecencyDateFrom(null);
                ca.setRecencyDateTo(null);
                break;
            }

            // Load from DB to UI
        } else {

            // Set recency type field
            AudienceDto audienceDto = ca.getAudience();
            if (audienceDto != null && audienceDto.resolveAudienceType(audienceDto) != null) {
                switch (audienceDto.resolveAudienceType(audienceDto)) {
                case DMP:
                case DEVICE:
                    ca.setAudienceRecencyType(AudienceRecencyType.NA);
                    break;
                default:
                    if (ca.getRecencyDateFrom() == null && ca.getRecencyDaysFrom() == null) {
                        ca.setAudienceRecencyType(AudienceRecencyType.NONE);
                    } else {
                        ca.setAudienceRecencyType((ca.getRecencyDateFrom() != null) ? AudienceRecencyType.RANGE
                                : AudienceRecencyType.WINDOW);
                    }

                    break;
                }
            }

            // Set default recency window if not specified
            if (ca.getRecencyDaysFrom() == null) {
                ca.setRecencyDaysFrom(0);
                ca.setRecencyDaysTo(getDefaultRecencyDaysTo());
            }

            // Set default recency range if not specified
            if (ca.getRecencyDateFrom() == null) {
                ca.setRecencyDateFrom(getDefaultRecencyDateFrom());
                ca.setRecencyDateTo(returnNow());
            }
        }
    }
    
    private void resolveAudienceSize(CampaignAudienceDto campaignAudience) {
        AudienceDto selectedAudienceDto = campaignAudience.getAudience();

        // Populates Audience size for Location audiences
        if (selectedAudienceDto != null  && AudienceType.LOCATION.equals(selectedAudienceDto.resolveAudienceType(selectedAudienceDto))) {
            try {
                campaignAudience.setAudienceSize(new BigDecimal(audienceFileService.getAudienceSize(FileType.GEOPOINTS, campaignAudience.getAudience().getId())));
                elasticHealthy = Boolean.TRUE;
            } catch (ElasticsearchException ese) {
                LOGGER.error("Found an exception accessing elasticsearch data.", ese);
                elasticHealthy = Boolean.FALSE;
            }
        }
    }

    public void addAudience(/*ActionEvent event*/) {
        CampaignAudienceDto audience = new CampaignAudienceDto();
        audience.setInclude(true);
        resolveRecencyFields(audience, false);
        campaignAudiences.add(audience);
    }

    public void removeAudience(/*ActionEvent event*/) {
        if (audienceIndexToRemove != null) {
            campaignAudiences.remove(audienceIndexToRemove.intValue());
            audienceIndexToRemove = null;
        }
    }

    public boolean isAudienceAddable() {
        if (CollectionUtils.isEmpty(campaignAudiences)) {
            return true;
        }
        CampaignAudienceDto last = campaignAudiences.get(campaignAudiences.size() - 1);
        if (last.getAudience() != null) {
            return true;
        }
        return false;
    }

    public String getAudienceSummary(boolean isLong) {
        if (campaignDto != null && CollectionUtils.isNotEmpty(campaignDto.getCampaignAudiences())) {
            StringBuilder sb = new StringBuilder();
            String sep = StringUtils.EMPTY;
            for (CampaignAudienceDto ca : campaignDto.getCampaignAudiences()) {
                sb.append(sep);
                if (isLong) {
                    String recencyDetail = StringUtils.EMPTY;
                    String recencyTypeLabel = FacesUtils.getBundleMessage(ca.getAudienceRecencyType().getLabel());
                    switch (ca.getAudienceRecencyType()) {
                    case RANGE:
                        DateFormat df = new SimpleDateFormat(DateUtils.getDateFormat());
                        recencyDetail = FacesUtils.getBundleMessage("page.campaign.targeting.audience.recency.range.summary", recencyTypeLabel,
                                df.format(ca.getRecencyDateFrom()), df.format(ca.getRecencyDateTo()));
                        break;
                    case WINDOW:
                        recencyDetail = FacesUtils.getBundleMessage("page.campaign.targeting.audience.recency.window.summary", recencyTypeLabel,
                                String.valueOf(ca.getRecencyDaysFrom()), String.valueOf(ca.getRecencyDaysTo()));
                        break;
                    default:
                        recencyDetail = FacesUtils.getBundleMessage("page.campaign.targeting.audience.recency.summary", recencyTypeLabel);
                        break;
                    }

                    sb.append(FacesUtils.getBundleMessage(
                            "page.campaign.targeting.audience.summary",
                            (ca.isInclude() ? FacesUtils.getBundleMessage("page.campaign.targeting.audience.type.include.label") : FacesUtils
                                    .getBundleMessage("page.campaign.targeting.audience.type.exclude.label")), ca.getAudience().getName(), recencyDetail));
                    sep = "<br />";
                } else if (ca.getAudience() != null) {
                    sb.append(ca.getAudience().getName());
                    sep = ", ";
                }
            }
            return sb.toString();
        }
        return notSet();
    }
    
    public boolean hasLocationAudience() {
        for (CampaignAudienceDto campaignAudienceDto : campaignAudiences) {
            AudienceDto audienceDto = campaignAudienceDto.getAudience();
            if (audienceDto != null && AudienceType.LOCATION.equals(audienceDto.resolveAudienceType(audienceDto))) {
                return true;
            }
        }
        return false;
    }
    
    public boolean hasFactualAudience() {
        for (CampaignAudienceDto campaignAudienceDto : campaignAudiences) {
            AudienceDto audienceDto = campaignAudienceDto.getAudience();
            if (audienceDto != null && AudienceType.DMP.equals(audienceDto.resolveAudienceType(audienceDto)) &&
            		AudienceSourceMBean.FACTUAL.equals(audienceDto.getDmpAudience().getDmpVendor().getName())) {
                return true;
            }
        }
        return false;
    }
    
    public void onSegmentChangeListener(/*AjaxBehaviorEvent event*/) {
        resolveAudienceSize(getSelectedCampaignAudienceDto());
    }
    
    public void updateMap(ActionEvent event) {
        LOGGER.debug("updateMap event: " + event);
        RequestContext requestContext = RequestContext.getCurrentInstance();
        
        requestContext.execute("gMapInitialize()");
        requestContext.execute("deleteOverlays()");
        List<GeoLocationModel> locations = getAudienceLocationsToShow(getSelectedCampaignAudienceDto().getAudience().getId());

        for (GeoLocationModel lt : locations) {
            double radiusInMeters = lt.getRadiusMiles().doubleValue() * Constants.METERS_IN_MILE;
            requestContext.execute("addMarker(" + lt.getLatitude().doubleValue() + "," + lt.getLongitude().doubleValue() + ",\"" + lt.getName()
                    + "\"," + radiusInMeters + ")");
        }
    }
    
    private CampaignAudienceDto getSelectedCampaignAudienceDto() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        return campaignAudiences.get(facesContext.getApplication().evaluateExpressionGet(facesContext, "#{status.index}", Integer.class));
    }

    private List<GeoLocationModel> getAudienceLocationsToShow(Long audienceId) {
        List<GeoLocationModel> locations = new ArrayList<GeoLocationModel>();

        try {
            locations = audienceFileService.getGeopointsFromAudience(audienceId, toolsApplicationBean.getLocationAudienceCoordsLimit());
            elasticHealthy = Boolean.TRUE;
        } catch (ElasticsearchException ese) {
            LOGGER.error("Found an exception accessing elasticsearch data.", ese);
            elasticHealthy = Boolean.FALSE;
        }

        return locations;
    }
    
    public CampaignDto getCampaignDto() {
        return campaignDto;
    }

    public void setCampaignDto(CampaignDto campaignDto) {
        this.campaignDto = campaignDto;
    }

    public List<AudienceDto> getAudiences() {
        if (audiences == null) {
            UserDTO userDto = (UserDTO) getUserSessionBean().getMap().get(Constants.USERDTO);
            audiences = aService.getAudiencesForAdvertiser(userDto.getAdvertiserDto().getId(), com.adfonic.domain.Audience.Status.ACTIVE);
        }
        return audiences;
    }

    public void setAudiences(List<AudienceDto> audiences) {
        this.audiences = audiences;
    }

    public List<CampaignAudienceDto> getCampaignAudiences() {
        return campaignAudiences;
    }

    public void setCampaignAudiences(List<CampaignAudienceDto> campaignAudiences) {
        this.campaignAudiences = campaignAudiences;
    }

    public Double getCampaignDataFee() {
        return campaignDataFee;
    }

    public Integer getAudienceIndexToRemove() {
        return audienceIndexToRemove;
    }

    public void setAudienceIndexToRemove(Integer audienceIndexToRemove) {
        this.audienceIndexToRemove = audienceIndexToRemove;
    }
    
    // ////////////////////
    // Getters
    // ////////////////////

    public Date getToday() {
        return returnNow();
    }

    public int getDefaultRecencyDaysTo() {
        return toolsApplicationBean.getDefaultAudienceRecency();
    }

    public Date getDefaultRecencyDateFrom() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(returnNow());
        cal.add(Calendar.DATE, -getDefaultRecencyDaysTo());
        return cal.getTime();
    }
    
    /**
     * Healthy check weather elastic is healthy so it can provide data
     */
    public Boolean isElasticHealthy() {
        return elasticHealthy;
    }
}