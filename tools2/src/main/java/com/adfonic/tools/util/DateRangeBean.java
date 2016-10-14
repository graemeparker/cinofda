package com.adfonic.tools.util;

import java.io.Serializable;
import java.util.Date;
import java.util.TimeZone;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import com.adfonic.presentation.FacesUtils;
import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.adfonic.util.DateUtils;
import com.adfonic.util.Range;

public class DateRangeBean extends GenericAbstractBean implements Serializable {

    private static final long serialVersionUID = -9139114500540924726L;

    public enum Option {
        NO_FUTURE, NO_PAST;
    }

    private transient UIInput binding;

    protected Option option;
    protected Date start;
    protected Date end;
    protected TimeZone timeZone;

    @Override
    protected void init() throws Exception {
    }

    public DateRangeBean() {
        option = Option.NO_FUTURE;
    }

    public DateRangeBean(Range<Date> range) {
        this(range, Option.NO_FUTURE);
    }

    public DateRangeBean(Range<Date> range, Option option) {
        this.option = option;
        setRange(range);
    }

    public UIInput getBinding() {
        return binding;
    }

    public void setBinding(UIInput binding) {
        this.binding = binding;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public Range<Date> getRange() {
        return new Range<Date>(DateUtils.getStartOfDay(start, getTimeZone()), DateUtils.getEndOfDay(end, getTimeZone()));
    }

    public void setRange(Range<Date> range) {
        start = range.getStart();
        end = range.getEnd();
    }

    public void validate(FacesContext context, UIComponent component, Object value) {
        Date endDate = (Date) value;
        Date startDate = (Date) getBinding().getLocalValue();

        // End must not be before start
        if (startDate != null && endDate != null && endDate.before(startDate)) {
            FacesMessage fm = FacesUtils.getFacesMessageById(FacesMessage.SEVERITY_ERROR, null, "error.dateRange.endBeforeStart");
            throw new ValidatorException(fm);
        }
        // NO_PAST: Start must not be in the past
        if (startDate != null && option == Option.NO_PAST) {
            // TODO check against company time zone?
            if (startDate.before(DateUtils.getStartOfDay(new Date(), getTimeZone()))) {
                FacesMessage fm = FacesUtils.getFacesMessageById(FacesMessage.SEVERITY_ERROR, null, "error.dateRange.startBeforeToday");
                throw new ValidatorException(fm);
            }
        }

        // NO_FUTURE: End must not be in the future
        if (endDate != null && option == Option.NO_FUTURE) {
            // TODO check against company time zone?
            if (endDate.after(new Date())) {
                FacesMessage fm = FacesUtils.getFacesMessageById(FacesMessage.SEVERITY_ERROR, null, "error.dateRange.endAfterToday");
                throw new ValidatorException(fm);
            }
        }
    }

    // if no timezone has been set use the default
    public TimeZone getTimeZone() {
        if (timeZone == null) {
            timeZone = getUser().getCompany().getTimeZone();
        }
        return timeZone;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }
}
