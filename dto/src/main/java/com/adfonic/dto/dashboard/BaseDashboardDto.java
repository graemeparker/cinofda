package com.adfonic.dto.dashboard;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.adfonic.dto.AbstractPaginationSearch;
import com.adfonic.dto.dashboard.DashboardParameters.Interval;
import com.adfonic.dto.dashboard.DashboardParameters.OrderBy;

public class BaseDashboardDto extends AbstractPaginationSearch implements Serializable {

    private static final long serialVersionUID = 1L;

    protected boolean individualLines;

    protected Date from;

    protected Date to;

    protected String datePickerPresetValue;

    protected Map<String, Object> graphs = new HashMap<String, Object>(0);

    protected Interval interval = Interval.HOURS;

    protected OrderBy orderBy = OrderBy.ASCENDING;

    public Map<String, Object> getGraphs() {
        return graphs;
    }

    public void setGraphs(Map<String, Object> graphs) {
        this.graphs = graphs;
    }

    public boolean isIndividualLines() {
        return individualLines;
    }

    public void setIndividualLines(boolean individualLines) {
        this.individualLines = individualLines;
    }

    public Date getFrom() {
        return from != null ? from : Calendar.getInstance().getTime();
    }

    public void setFrom(Date from) {
        this.from = (from == null ? null : new Date(from.getTime()));
    }

    public Date getTo() {
        return to != null ? to : Calendar.getInstance().getTime();
    }

    public void setTo(Date to) {
        this.to = (to == null ? null : new Date(to.getTime()));
    }

    public String getDatePickerPresetValue() {
        return datePickerPresetValue;
    }

    public void setDatePickerPresetValue(String datePickerPresetValue) {
        this.datePickerPresetValue = datePickerPresetValue;
    }

    public Interval getInterval() {
        return interval;
    }

    public void setInterval(Interval interval) {
        this.interval = interval;
    }

    public OrderBy getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(OrderBy orderBy) {
        this.orderBy = orderBy;
    }
}
