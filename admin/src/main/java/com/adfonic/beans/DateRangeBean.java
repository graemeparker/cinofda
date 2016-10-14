package com.adfonic.beans;

import java.util.Date;
import java.util.TimeZone;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.joda.time.DateTime;
import org.joda.time.Days;

import com.adfonic.util.DateUtils;
import com.adfonic.util.Range;
import com.adfonic.util.TimeZoneUtils;

/**
 * Encapsulates a date range with a start and end date.  Whether the
 * start and/or end dates are required should be controlled using the
 * tags on the front end.
 *
 * By default, this validates that the end date is not in the future.
 * You can construct it with a null Option if you don't want this behaviour,
 * or with NO_PAST if you don't want to allow the start date to be in the past.
 *
 * Example:
 *   <h:inputDate value="#{someBean.dateRange.start}"
 *          binding="#{someBean.dateRange.binding}" />
 *   <h:inputDate value="#{someBean.dateRange.end}"
 *          validator="#{someBean.dateRange.validate}" />
 *
 * Pattern documented at http://matt-stine.blogspot.com/2007/06/how-to-implement-form-level-validation.html
 */
public class DateRangeBean extends BaseBean {
    public enum Option {
    NO_FUTURE, NO_PAST;
    }

	private static final int MAX_DAYS_BETWEEN = 31;

    private transient UIInput binding;

    protected Option option;
    protected Date start;
    protected Date end;
    protected TimeZone timeZone;

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

    public Date getStart() { return start; }
    public void setStart(Date start) { this.start = start; }

    public Date getEnd() { return end; }
    public void setEnd(Date end) { this.end = end; }

    public Range<Date> getRange() {
        return new Range<Date>(
                DateUtils.getStartOfDay(start, getTimeZone()),
                DateUtils.getEndOfDay(end, getTimeZone()));
    }

    public void setRange(Range<Date> range) {
    start = range.getStart();
    end = range.getEnd();
    }

    /** This validator should be bound to the end date field. */
    public void validate(FacesContext context, UIComponent component, Object value) {
	    Date endDate = (Date) value;
	    Date startDate = (Date) getBinding().getLocalValue();
	
	    // End must not be before start
	    if (startDate != null && endDate != null && endDate.before(startDate)) {
	        throw new ValidatorException(messageForId("error.dateRange.endBeforeStart"));
	    }
	    // NO_PAST: Start must not be in the past
	    if (startDate != null && option == Option.NO_PAST) {
	        // TODO check against company time zone?
	        if (startDate.before(DateUtils.getStartOfDay(new Date(), getTimeZone()))) {
	        throw new ValidatorException(messageForId("error.dateRange.startBeforeToday"));
	        }
	    }
	
	    // NO_FUTURE: End must not be in the future
	    if (endDate != null && option == Option.NO_FUTURE) {
	        // TODO check against company time zone?
	        if (endDate.after(new Date())) {
	        throw new ValidatorException(messageForId("error.dateRange.endAfterToday"));
	        }
	    }
	    
	    // MAX ONE MONTH: The difference between dates can not be greater than one month
	    int daysBetween = Days.daysBetween(new DateTime(startDate), new DateTime(endDate)).getDays();
	    if (daysBetween>MAX_DAYS_BETWEEN){
	    	throw new ValidatorException(messageForId("error.dateRange.maxdaysbetween"));
	    }
	    
    }

    // if no timezone has been set use the default
    public TimeZone getTimeZone() {
        if (timeZone == null) {
            timeZone = TimeZoneUtils.getDefaultTimeZone();
        }
        return timeZone;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

}
