package com.adfonic.tools.beans.js;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.dto.chart.ChartValuesDto;
import com.adfonic.tools.beans.dashboard.ChartMBean;
import com.adfonic.tools.beans.util.Constants;
import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.adfonic.tools.beans.util.Utils;
import com.adfonic.tools.util.FileUtils;

@Component
@Scope("view")
public class ChartExpanderJs extends GenericAbstractBean implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private boolean cfgAxesXaxis = true;
    @SuppressWarnings("unused")
    private boolean cfgAxesYaxis = true;
    @SuppressWarnings("unused")
    private boolean cfgGrid = true;
    @SuppressWarnings("unused")
    private boolean cfgHighLighter = true;
    @SuppressWarnings("unused")
    private boolean cfgSeries = true;

    private Map<String, Map<String, String>> chartMap = new HashMap<String, Map<String, String>>(0);

    public String getExtender(String chartId) {
        return buildExternalString(chartId);
    }

    private String buildExternalString(String chartId) {
        String result = "function() {";
        String end = " }";
        if (isCfgAxesYaxis(chartId)) {
            result = result
                    + "\n "
                    + FileUtils.getFile(FileUtils.FileUtilsEnum.DASHBOARD_TABLE_CFG_YAXIS_JS,
                            updateChartValues(chartId, chartMap.get(chartId)));
        }
        if (isCfgAxesXaxis(chartId)) {
            result = result
                    + "\n "
                    + FileUtils.getFile(FileUtils.FileUtilsEnum.DASHBOARD_TABLE_CFG_XAXIS_JS,
                            updateChartValues(chartId, chartMap.get(chartId)));
        }
        if (isCfgSeries(chartId)) {
            result = result + "\n " + FileUtils.getFile(FileUtils.FileUtilsEnum.DASHBOARD_TABLE_CFG_SERIES_JS, chartMap.get(chartId));
        }
        if (isCfgHighLighter(chartId)) {
            result = result + "\n " + FileUtils.getFile(FileUtils.FileUtilsEnum.DASHBOARD_TABLE_CFG_HIGHLIGHTER_JS, chartMap.get(chartId));
        }
        if (isCfgGrid(chartId)) {
            result = result + "\n " + FileUtils.getFile(FileUtils.FileUtilsEnum.DASHBOARD_TABLE_CFG_GRID_JS, chartMap.get(chartId));
        }
        return result + end;
    }

    public boolean isCfgAxesXaxis(String chartId) {

        if (chartMap.get(chartId) != null && chartMap.get(chartId).containsKey("cfgAxesXaxis")
                && Boolean.valueOf(chartMap.get(chartId).get("cfgAxesXaxis"))) {
            return true;
        } else {
            return false;
        }

    }

    public void setCfgAxesXaxis(boolean cfgAxesXaxis) {
        this.cfgAxesXaxis = cfgAxesXaxis;
    }

    public boolean isCfgAxesYaxis(String chartId) {
        if (chartMap.get(chartId) != null && chartMap.get(chartId).containsKey("cfgAxesYaxis")
                && Boolean.valueOf(chartMap.get(chartId).get("cfgAxesYaxis"))) {
            return true;
        } else {
            return false;
        }
    }

    public void setCfgAxesYaxis(boolean cfgAxesYaxis) {
        this.cfgAxesYaxis = cfgAxesYaxis;
    }

    public boolean isCfgGrid(String chartId) {
        if (chartMap.get(chartId) != null && chartMap.get(chartId).containsKey("cfgGrid")
                && Boolean.valueOf(chartMap.get(chartId).get("cfgGrid"))) {
            return true;
        } else {
            return false;
        }
    }

    public void setCfgGrid(boolean cfgGrid) {
        this.cfgGrid = cfgGrid;
    }

    public boolean isCfgHighLighter(String chartId) {
        if (chartMap.get(chartId) != null && chartMap.get(chartId).containsKey("cfgHighLighter")
                && Boolean.valueOf(chartMap.get(chartId).get("cfgHighLighter"))) {
            return true;
        } else {
            return false;
        }
    }

    public void setCfgHighLighter(boolean cfgHighLighter) {
        this.cfgHighLighter = cfgHighLighter;
    }

    public boolean isCfgSeries(String chartId) {
        if (chartMap.get(chartId) != null && chartMap.get(chartId).containsKey("cfgSeries")
                && Boolean.valueOf(chartMap.get(chartId).get("cfgSeries"))) {
            return true;
        } else {
            return false;
        }
    }

    public void setCfgSeries(boolean cfgSeries) {
        this.cfgSeries = cfgSeries;
    }

    @Override
    public void init() {
    }

    public Map<String, Map<String, String>> getChartMap() {
        return chartMap;
    }

    public void setChartMap(Map<String, Map<String, String>> chartMap) {
        this.chartMap = chartMap;
    }

    public Map<String, String> updateChartValues(String chartId, Map<String, String> mapToUpdate) {
        ChartMBean chartBean = Utils.findBean(FacesContext.getCurrentInstance(), Constants.CHART_BEAN);
        ChartValuesDto dto = chartBean.getChartValuesMap().get(chartId);
        if (dto != null) {
            mapToUpdate.put("xaxisMin", Long.toString(dto.getMinTimeRef()));
            mapToUpdate.put("xaxisMax", Long.toString(dto.getMaxTimeRef()));
            mapToUpdate.put("yaxisMin", Double.toString(dto.getMinDoubleRef()));
            mapToUpdate.put("yaxisMax", Double.toString(dto.getMaxDoubleRef()));
            mapToUpdate.put("xaxisTickAngle", Long.toString(dto.getXaxisTickAngle()));

            if (!StringUtils.isEmpty(dto.getXaxisTickInterval())) {
                mapToUpdate.put("xaxisTickInterval", dto.getXaxisTickInterval());
            }

            if (!StringUtils.isEmpty(dto.getYaxisTickInterval())) {
                mapToUpdate.put("yaxisTickInterval", dto.getYaxisTickInterval());
            }
            if (!StringUtils.isEmpty(dto.getyAxisTicketOptionsFormatString())) {
                mapToUpdate.put("yaxisTicketOptionsFormatString", dto.getyAxisTicketOptionsFormatString());
            }
            if (!StringUtils.isEmpty(dto.getxAxisTicketOptionsFormatString())) {
                mapToUpdate.put("xaxisTicketOptionsFormatString", dto.getxAxisTicketOptionsFormatString());
            }
            if (!StringUtils.isEmpty(dto.getFormatDateFormat())) {
                mapToUpdate.put("formatDateFormat", dto.getFormatDateFormat());
            }

        }
        // in case we change the interval in x axis:
        return mapToUpdate;
    }

}
