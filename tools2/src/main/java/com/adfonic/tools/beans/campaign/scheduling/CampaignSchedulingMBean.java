package com.adfonic.tools.beans.campaign.scheduling;

import static com.adfonic.presentation.FacesUtils.addFacesMessage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;

import org.apache.commons.lang.ArrayUtils;
import org.primefaces.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.domain.Campaign;
import com.adfonic.domain.Campaign.BudgetType;
import com.adfonic.dto.campaign.CampaignDto;
import com.adfonic.dto.campaign.enums.PluginType;
import com.adfonic.dto.campaign.enums.WeekDays;
import com.adfonic.dto.campaign.scheduling.CampaignTimePeriodDto;
import com.adfonic.dto.campaign.trigger.CampaignTriggerDto;
import com.adfonic.dto.campaign.trigger.PluginVendorDto;
import com.adfonic.presentation.campaign.CampaignService;
import com.adfonic.presentation.company.CompanyService;
import com.adfonic.presentation.pluginvendor.PluginVendorService;
import com.adfonic.tools.beans.util.Constants;
import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.adfonic.tools.beans.util.Utils;
import com.ibm.icu.util.Calendar;

@Component
@Scope("session")
public class CampaignSchedulingMBean extends GenericAbstractBean implements Serializable {

    private static final long serialVersionUID = 4188436945608530254L;

    private static final Logger LOGGER = LoggerFactory.getLogger(CampaignSchedulingMBean.class);

    private static final List<PluginType> PLUGIN_TYPES = Arrays.asList(PluginType.values());

    private CampaignDto campaignDto;

    @Autowired
    private CampaignService campaignService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private PluginVendorService pluginVendorService;

    private boolean showTimeDayControl = false;
    private boolean displayHourAndMinutesSelector = false;

    private List<CampaignTimePeriodDto> timePeriods = null;

    // Time section
    private Boolean[] hoursOfDay;
    private Boolean[] hoursOfDayWeekend;
    private Boolean[] daysOfWeek;

    private boolean endDateNull = false;

    List<PluginVendorDto> availablePluginVendors = null;
    List<CampaignTriggerDto> campaignTriggers = null;

    @Override
    protected void init() {
    }

    public void doAddPeriod(ActionEvent event) {
        LOGGER.debug("doAddPeriod-->");
        // For adding time periods all have to be completed
        for (CampaignTimePeriodDto tp : timePeriods) {
            if (tp.getStartDate() == null || tp.getEndDate() == null) {
                LOGGER.debug("End or start date from last period null");
                addFacesMessage(FacesMessage.SEVERITY_ERROR, "campaignForm", null, "page.campaign.scheduling.error.emptyperiod");
                return;
            }
        }
        LOGGER.debug("New time period added");
        timePeriods.add(new CampaignTimePeriodDto());
        LOGGER.debug("doAddPeriod<--");
    }

    public void doDisplayHoursAndMinutesEvent(ActionEvent event) {
        LOGGER.debug("doDisplayHoursAndMinutesEvent-->");
        if (displayHourAndMinutesSelector) {
            displayHourAndMinutesSelector = false;
        } else {
            displayHourAndMinutesSelector = true;
        }
        LOGGER.debug("doDisplayHoursAndMinutesEvent<--");
    }

    public void doAddControlTime(ActionEvent event) {
        if (showTimeDayControl) {
            showTimeDayControl = false;
        } else {
            showTimeDayControl = true;
        }
    }

    public void doRemovePeriod(ActionEvent event) {
        Integer index = (Integer) event.getComponent().getAttributes().get("index");
        LOGGER.debug("Remove time period with index " + index);
        if (index != null) {
            int i = index;
            timePeriods.remove(i);
        }
    }

    public void doSave(ActionEvent event) throws Exception {
        LOGGER.debug("doSave-->");
        // save and continue campaign;
        CampaignDto camp = null;
        try {
            camp = campaignService.saveScheduling(prepareDto(campaignDto), this.campaignTriggers);
        } catch (IllegalArgumentException e) {
            // time periods overlap
            addFacesMessage(FacesMessage.SEVERITY_ERROR, "schedulingDiv", null, "page.campaign.scheduling.error.overlap");
            return;
        }

        // update campaignDto to controller bean
        updateCampaignBeans(camp);
        if (getCNavigationBean().isTargetingDisabled()) {
            getCNavigationBean().setTargetingDisabled(false);
            getCNavigationBean().saveCampaignNavigation(campaignDto.getId(), Constants.MENU_TARGETING);
        }

        if (getCampaignMBean().isNewCampaign()) {
            getCNavigationBean().updateMenuStyles(Constants.MENU_NAVIGATE_TO_TARGETING);
            getCNavigationBean().setNavigate("/WEB-INF/jsf/campaign/section_targeting.xhtml");
        } else {
            getCNavigationBean().updateMenuStyles(Constants.MENU_NAVIGATE_TO_CONFIRMATION);
            getCNavigationBean().setNavigate("/WEB-INF/jsf/campaign/section_confirmation.xhtml");
        }
        // timePeriods=null;
        LOGGER.debug("doSave<--");
    }

    // If end date is set to null and even distribution was true, it will be set
    // to false and user must confirm
    public void checkEvenDistribution(ActionEvent event) throws Exception {
        // Remove empty periods
        if (timePeriods.size() > 1 && timePeriods.get(timePeriods.size() - 1).getStartDate() == null
                && timePeriods.get(timePeriods.size() - 1).getEndDate() == null) {
            LOGGER.debug("Last time period empty deleted");
            timePeriods.remove(timePeriods.size() - 1);
        }

        boolean validpluginTypes = true;
        if (campaignTriggers != null && campaignTriggers.size() > 0 && campaignTriggers.get(0).getPluginType() == null) {
            validpluginTypes = false;
            addFacesMessage(FacesMessage.SEVERITY_ERROR, "schedulingAddTrigger", null, "page.campaign.scheduling.error.emptyplugintype");
        }

        if (validTimePeriods(timePeriods) && validpluginTypes) {
            endDateNull = isEndDateNull();
            if (endDateNull && campaignDto.isEvenDistributionOverallBudget()) {
                RequestContext context = RequestContext.getCurrentInstance();
                context.execute("confirmationSave.show()");
            } else {
                doSave(event);
            }
        }
    }

    public CampaignDto prepareDto(CampaignDto dto) {
        LOGGER.debug("prepareDto-->");
        dto.setAdvertiser(getUser().getAdvertiserDto());
        campaignDto.getScheduleDto().setHoursOfDay(hoursOfDay);
        campaignDto.getScheduleDto().setDaysOfWeek(daysOfWeek);
        campaignDto.getScheduleDto().setHoursOfDayWeekend(hoursOfDayWeekend);
        Collections.sort(timePeriods);
        prepareTimePeriods();
        if (endDateNull) {
            campaignDto.setEvenDistributionOverallBudget(false);
            if(campaignDto.isEvenDistributionDailyBudget() 
                    && ((campaignDto.getBudgetType().equals(BudgetType.MONETARY) && campaignDto.getDailyBudget() == null)
                            || (campaignDto.getBudgetType().equals(BudgetType.CLICKS) && campaignDto.getDailyBudgetClicks() == null)
                            || (campaignDto.getBudgetType().equals(BudgetType.IMPRESSIONS) && campaignDto.getDailyBudgetImpressions() == null))){
                campaignDto.setEvenDistributionDailyBudget(false);
            }
        }
        LOGGER.debug("prepareDto<--");
        return dto;
    }

    private void prepareTimePeriods() {
        LOGGER.debug("prepareTimePeriods-->");
        campaignDto.getTimePeriods().clear();
        if (timePeriods.size() > 1 && (timePeriods.get(timePeriods.size() - 1).getStartDate() == null)) {
            timePeriods.remove(timePeriods.size() - 1);
            LOGGER.debug("Last time period removed");
        }
        LOGGER.debug("Adding " + timePeriods.size() + " periods");
        campaignDto.getTimePeriods().addAll(timePeriods);
        LOGGER.debug("prepareTimePeriods<--");
    }

    public boolean isScheduleEditable() {
        return (campaignDto.getStatus() != null && campaignDto.getStatus() != Campaign.Status.COMPLETED && campaignDto.getStatus() != Campaign.Status.STOPPED);
    }

    // Note: <t:columns> won't work with a primitive int[]
    public Integer[] getHoursList() {
        return Constants.getHours();
    }

    public CampaignDto getCampaignDto() {
        return campaignDto;
    }

    public void setCampaignDto(CampaignDto campaignDto) {
        this.campaignDto = campaignDto;
    }

    public boolean getShowTimeDayControl() {
        return showTimeDayControl;
    }

    public void setShowTimeDayControl(boolean showTimeDayControl) {
        this.showTimeDayControl = showTimeDayControl;
    }

    public Integer[] getWeekdayIndices() {
        return Constants.getWeeklyIndices();
    }

    public WeekDays[] getWeekDaysStr() {
        return WeekDays.values();
    }

    public Boolean[] getHoursOfDay() {
        if (hoursOfDay == null) {
            this.hoursOfDay = ArrayUtils.toObject(campaignDto.getCurrentSegment().getHoursOfDayAsArray());
        }
        return hoursOfDay;
    }

    public void setHoursOfDay(Boolean[] hoursOfDay) {
        this.hoursOfDay = hoursOfDay;
    }

    public Boolean[] getHoursOfDayWeekend() {
        if (hoursOfDayWeekend == null) {
            this.hoursOfDayWeekend = ArrayUtils.toObject(campaignDto.getCurrentSegment().getHoursOfDayWeekendAsArray());
        }
        return hoursOfDayWeekend;
    }

    public void setHoursOfDayWeekend(Boolean[] hoursOfDayWeekend) {
        this.hoursOfDayWeekend = hoursOfDayWeekend;
    }

    public Boolean[] getDaysOfWeek() {
        if (daysOfWeek == null) {
            this.daysOfWeek = ArrayUtils.toObject(campaignDto.getCurrentSegment().getDaysOfWeekAsArray());
        }
        return daysOfWeek;
    }

    public void setDaysOfWeek(Boolean[] daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    public List<CampaignTimePeriodDto> getTimePeriods() {
        if (timePeriods == null) {
            timePeriods = new ArrayList<CampaignTimePeriodDto>();
        }
        return timePeriods;
    }

    public void setTimePeriods(List<CampaignTimePeriodDto> timePeriods) {
        this.timePeriods = timePeriods;
    }

    public void loadCampaignDto(CampaignDto dto) {
        LOGGER.debug("loadCampaignDto-->");
        // somehow the campaingDto even though we equal the object to dto, still
        // has references to old timeperiods thats why we
        // clear the timperiods first and add the rest of them later.
        setCampaignDto(dto);

        if (dto != null) {
            this.hoursOfDay = ArrayUtils.toObject(campaignDto.getCurrentSegment().getHoursOfDayAsArray());
            this.hoursOfDayWeekend = ArrayUtils.toObject(campaignDto.getCurrentSegment().getHoursOfDayWeekendAsArray());
            this.daysOfWeek = ArrayUtils.toObject(campaignDto.getCurrentSegment().getDaysOfWeekAsArray());
            timePeriods = new ArrayList<CampaignTimePeriodDto>();
            TimeZone companyTimeZone = getCompanyTimeZone(companyService);
            LOGGER.debug("Timezone: " + companyTimeZone.getID());
            boolean outOfTime = false;
            for (CampaignTimePeriodDto ct : campaignDto.getTimePeriods()) {
                CampaignTimePeriodDto period = new CampaignTimePeriodDto();
                period.setCampaign(ct.getCampaign());
                period.setId(ct.getId());
                if (ct.getStartDate() != null) {
                    period = Utils.setDateAndHourDetails(period, ct.getStartDate(), companyTimeZone, true);
                    // period.setStartDate(Utils.getTimezoneDate(ct.getStartDate(),
                    // companyTimeZone));
                    // period.setStartTimeOffset(Utils.getMinuteOffset(ct.getStartDate(),
                    // companyTimeZone));
                    GregorianCalendar gc = new GregorianCalendar();
                    gc.setTime(period.getStartDate());
                    if (gc.get(Calendar.HOUR_OF_DAY) != 0 || gc.get(Calendar.MINUTE) != 0) {
                        outOfTime = true;
                    }
                }
                if (ct.getEndDate() != null) {
                    // period.setEndDate(Utils.getTimezoneDate(ct.getEndDate(),
                    // companyTimeZone));
                    // period.setEndTimeOffset(Utils.getMinuteOffset(ct.getEndDate(),
                    // companyTimeZone));
                    period = Utils.setDateAndHourDetails(period, ct.getEndDate(), companyTimeZone, false);
                    GregorianCalendar gc = new GregorianCalendar();
                    gc.setTime(period.getEndDate());
                    if (gc.get(Calendar.HOUR_OF_DAY) != 23 || gc.get(Calendar.MINUTE) != 59) {
                        outOfTime = true;
                    }
                }
                timePeriods.add(period);
            }
            if (!displayHourAndMinutesSelector) {
                displayHourAndMinutesSelector = outOfTime;
            }
            // update timeperiods everywhere
            this.availablePluginVendors = null;
            this.campaignTriggers = null;
        }
        LOGGER.debug("loadCampaignDto<--");
    }

    public void changeStartDate(ValueChangeEvent event) {
        Integer index = (Integer) event.getComponent().getAttributes().get("index");
        if (event.getNewValue() == null) {
            timePeriods.get(index).setStartDate(null);
            ((org.primefaces.component.calendar.Calendar) event.getComponent()).resetValue();
        }
    }

    public void changeEndDate(ValueChangeEvent event) {
        Integer index = (Integer) event.getComponent().getAttributes().get("index");
        if (event.getNewValue() == null) {
            timePeriods.get(index).setEndDate(null);
            ((org.primefaces.component.calendar.Calendar) event.getComponent()).resetValue();
        }
    }

    public void cancel(ActionEvent event) {
        LOGGER.debug("cancel-->");
        loadCampaignDto(campaignDto);
        getCNavigationBean().updateMenuStyles(Constants.MENU_NAVIGATE_TO_CONFIRMATION);
        getCNavigationBean().setNavigate("/WEB-INF/jsf/campaign/section_confirmation.xhtml");
        LOGGER.debug("cancel<--");
    }

    public Date getToday() {
        return returnNow();
    }

    public Date getTodayLate() {
        GregorianCalendar gc = new GregorianCalendar();
        gc.set(Calendar.HOUR_OF_DAY, 23);
        gc.set(Calendar.MINUTE, 59);
        return gc.getTime();
    }

    /** VALIDATORS **/
    private boolean validTimePeriods(List<CampaignTimePeriodDto> periods) {
        LOGGER.debug("validTimePeriods-->");
        for (int i = 0; i < periods.size(); i++) {
            if (!validTimePeriod(periods.get(i))) {
                LOGGER.debug("Time period wrong, start Date: " + periods.get(i).getStartDate().toString() + " end date: "
                        + periods.get(i).getEndDate().toString());
                UIComponent comp = FacesContext.getCurrentInstance().getViewRoot().findComponent("campaignForm:schedulingListDiv");
                addFacesMessage(FacesMessage.SEVERITY_ERROR, comp.getChildren().get(0).getId() + ":" + i + ":end-date", null,
                        "page.campaign.scheduling.error.wrong");
                LOGGER.debug("validTimePeriods<--");
                return false;
            }
        }
        LOGGER.debug("validTimePeriods<--");
        return true;
    }

    private boolean validTimePeriod(CampaignTimePeriodDto timePeriod) {
        if (timePeriod.getStartDate() == null || timePeriod.getEndDate() == null) {
            return true;
        } else if (Utils.validateTimePeriod(timePeriod.getStartDate(), timePeriod.getStartTimeOffset(), timePeriod.getEndDate(),
                timePeriod.getEndTimeOffset())) {
            LOGGER.debug("End date: " + timePeriod.getEndDate() + " before start date: " + timePeriod.getStartDate());
            return false;
        }
        return true;
    }

    public boolean getDisplayHourAndMinutesSelector() {
        return displayHourAndMinutesSelector;
    }

    public void setDisplayHourAndMinutesSelector(boolean displayHourAndMinutesSelector) {
        this.displayHourAndMinutesSelector = displayHourAndMinutesSelector;
    }

    private boolean isEndDateNull() {
        boolean isNull = false;
        for (CampaignTimePeriodDto tp : timePeriods) {
            if (tp.getEndDate() == null) {
                isNull = true;
                break;
            }
        }
        return isNull;
    }

    public List<PluginType> getPluginTypes() {
        return PLUGIN_TYPES;
    }

    public List<PluginVendorDto> getPluginVendors() {
        if (availablePluginVendors == null) {
            availablePluginVendors = pluginVendorService.getPluginVendors();
        }
        return availablePluginVendors;
    }

    public List<CampaignTriggerDto> getCampaignTriggers() {
        if (campaignDto != null && campaignTriggers == null) {
            campaignTriggers = campaignService.getCampaignTriggers(campaignDto);
        }
        return campaignTriggers;
    }

    public void doAddTrigger(ActionEvent event) {
        LOGGER.debug("doAddTrigger-->");
        this.campaignTriggers.add(new CampaignTriggerDto());
        LOGGER.debug("New time period added");
        LOGGER.debug("doAddTrigger<--");
    }

    public void doRemoveTrigger(ActionEvent event) {
        Integer index = (Integer) event.getComponent().getAttributes().get("index");
        this.campaignTriggers.remove(index.intValue());
    }
}
