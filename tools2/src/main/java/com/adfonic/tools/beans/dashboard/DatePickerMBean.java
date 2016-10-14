package com.adfonic.tools.beans.dashboard;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import javax.faces.event.AjaxBehaviorEvent;

import org.joda.time.DateTime;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.tools.beans.util.Constants;
import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.adfonic.tools.beans.util.Utils;
import com.ibm.icu.util.GregorianCalendar;
import com.ocpsoft.pretty.faces.annotation.URLAction;
import com.ocpsoft.pretty.faces.annotation.URLActions;

@Component
@Scope("view")
public class DatePickerMBean extends GenericAbstractBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Date from;

    private Date to;

    private String previousSelection = null;

    /***
     * Method invoke everytime the user gets into the session to check if
     * there's any previous cookie with the latest searches. If there are
     * cookies with the searches, we should provide those searches when loading
     * page
     * */
    @Override
    @URLActions(actions = { @URLAction(mappingId = "dashboard-advertiser") })
    public void init() {

    }

    public void processDatePickerValueChange(AjaxBehaviorEvent event) {
        String newValue = previousSelection;
        Date[] dateRange = Utils.getDateRange(newValue);
        if (dateRange != null && dateRange.length == 2) {
            setFrom(dateRange[0]);
            setTo(dateRange[1]);
        }
        addStringCookie(previousSelection, Constants.COOKIE_DATE_SELECTION);
        refreshDates(from, to, previousSelection);
    }

    public String getDateSelection() {
        if (previousSelection != null) {
            Map<String, String> map = getToolsApplicationBean().getDatePickerPresetsMap();
            Iterator<String> bundleKeysIt = getToolsApplicationBean().getDatePickerPresetsMap().keySet().iterator();
            while (bundleKeysIt.hasNext()) {
                String key = bundleKeysIt.next();
                if (map.get(key).equals(previousSelection)) {
                    return key;
                }
            }
        }
        return "";
    }

    public boolean isRenderLastMonth() {
        GregorianCalendar gc = new GregorianCalendar();
        return gc.get(Calendar.DAY_OF_MONTH) <= 7;
    }

    public Date getToday() {
        return returnNow();
    }

    public Date getMinDate() {
        DateTime dt = new DateTime();
        dt = dt.minusDays(14);
        return dt.toDate();
    }

    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public Date getTo() {
        return to;
    }

    public void setTo(Date to) {
        this.to = to;
    }

    public String getPreviousSelection() {
        if (previousSelection == null) {
            previousSelection = "2";
        }
        return previousSelection;
    }

    public void setPreviousSelection(String previousSelection) {
        this.previousSelection = previousSelection;
    }
}
