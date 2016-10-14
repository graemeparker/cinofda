package com.adfonic.tools.beans.dashboard.chart.model;

import java.io.Serializable;

import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.ChartModel;

public class DashboardChartModel implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 9213003800826210139L;

    private ChartModel chartModel = new CartesianChartModel();
    private String extender = new String();

    /**
     * @return the chartModel
     */
    public ChartModel getChartModel() {
        return chartModel;
    }

    /**
     * @param chartModel
     *            the chartModel to set
     */
    public void setChartModel(ChartModel chartModel) {
        this.chartModel = chartModel;
    }

    /**
     * @return the extender
     */
    public String getExtender() {
        return extender;
    }

    /**
     * @param extender
     *            the extender to set
     */
    public void setExtender(String extender) {
        this.extender = extender;
    }

}
