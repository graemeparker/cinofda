package com.adfonic.dto.chart;

import java.io.Serializable;

public class ChartValuesDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private long minTimeRef = -1;
    private long maxTimeRef = -1;

    private Double minDoubleRef = -1D;
    private Double maxDoubleRef = -1D;

    private String xaxisTickInterval;
    private String yaxisTickInterval;

    private String yAxisTicketOptionsFormatString;
    private String xAxisTicketOptionsFormatString;

    private long xaxisTickAngle;

    private String formatDateFormat;

    public long getMinTimeRef() {
        return minTimeRef;
    }

    public void setMinTimeRef(long minTimeRef) {
        this.minTimeRef = minTimeRef;
    }

    public long getMaxTimeRef() {
        return maxTimeRef;
    }

    public void setMaxTimeRef(long maxTimeRef) {
        this.maxTimeRef = maxTimeRef;
    }

    public Double getMinDoubleRef() {
        return minDoubleRef;
    }

    public void setMinDoubleRef(Double minDoubleRef) {
        this.minDoubleRef = minDoubleRef;
    }

    public Double getMaxDoubleRef() {
        return maxDoubleRef;
    }

    public void setMaxDoubleRef(Double maxDoubleRef) {
        this.maxDoubleRef = maxDoubleRef;
    }

    public String getXaxisTickInterval() {
        return xaxisTickInterval;
    }

    public void setXaxisTickInterval(String xaxisTickInterval) {
        this.xaxisTickInterval = xaxisTickInterval;
    }

    public String getYaxisTickInterval() {
        return yaxisTickInterval;
    }

    public void setYaxisTickInterval(String yaxisTickInterval) {
        this.yaxisTickInterval = yaxisTickInterval;
    }

    public String getyAxisTicketOptionsFormatString() {
        return yAxisTicketOptionsFormatString;
    }

    public void setyAxisTicketOptionsFormatString(String yAxisTicketOptionsFormatString) {
        this.yAxisTicketOptionsFormatString = yAxisTicketOptionsFormatString;
    }

    public String getxAxisTicketOptionsFormatString() {
        return xAxisTicketOptionsFormatString;
    }

    public void setxAxisTicketOptionsFormatString(String xAxisTicketOptionsFormatString) {
        this.xAxisTicketOptionsFormatString = xAxisTicketOptionsFormatString;
    }

    public long getXaxisTickAngle() {
        return xaxisTickAngle;
    }

    public void setXaxisTickAngle(long xaxisTickAngle) {
        this.xaxisTickAngle = xaxisTickAngle;
    }

    public String getFormatDateFormat() {
        return formatDateFormat;
    }

    public void setFormatDateFormat(String formatDateFormat) {
        this.formatDateFormat = formatDateFormat;
    }
}
