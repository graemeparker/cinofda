package com.adfonic.tools.beans.dashboard;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.event.ActionEvent;

import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.LineChartSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.adfonic.dto.chart.ChartValuesDto;
import com.adfonic.dto.dashboard.AgencyConsoleDashboardDto;
import com.adfonic.dto.dashboard.DashboardDto;
import com.adfonic.dto.dashboard.DashboardParameters.Interval;
import com.adfonic.dto.dashboard.DashboardParameters.PublisherReport;
import com.adfonic.dto.dashboard.DashboardParameters.Report;
import com.adfonic.dto.dashboard.PublisherDashboardDto;
import com.adfonic.dto.user.UserDTO;
import com.adfonic.presentation.dashboard.DashboardService;
import com.adfonic.tools.beans.util.Constants;
import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.adfonic.tools.beans.util.Utils;

@Component
@Scope("view")
public class ChartMBean extends GenericAbstractBean implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Autowired
    private DashboardService dService;

    private static Logger LOGGER = LoggerFactory.getLogger(ChartMBean.class);

    private CartesianChartModel linearModel;
    private DashboardDto dashboardDto;
    private DashboardDto cacheDashboardDto;
    private PublisherDashboardDto publisherDashboardDto;
    private PublisherDashboardDto publisherCacheDashboardDto;
    private AgencyConsoleDashboardDto agencyDashboardDto;
    private AgencyConsoleDashboardDto cacheAgencyDashboardDto;
    private String filterBy;

    private Map<String, CartesianChartModel> chartMap = new HashMap<String, CartesianChartModel>(0);
    private Map<String, ChartValuesDto> chartValuesMap = new HashMap<String, ChartValuesDto>(0);

    private boolean renderChart1 = true;
    private boolean renderChart2 = false;
    private boolean renderChart3 = false;
    private boolean renderChart4 = false;
    private boolean renderChart6 = false;

    private long minTimeRef;
    private long maxTimeRef;
    private Double maxDoubleRef;

    public CartesianChartModel getLinearModel(String chartId) {
        if (dashboardDto == null || !dashboardDto.equals(cacheDashboardDto) || chartMap.get(chartId) == null) {
            CartesianChartModel linearModel = createLinearModel(chartId);
            cacheDashboardDto = dashboardDto;
            chartMap.put(chartId, linearModel);
        }
        return chartMap.get(chartId);
    }

    public CartesianChartModel getPublisherLinearModel(String chartId) {
        if (publisherDashboardDto == null || !publisherDashboardDto.equals(publisherCacheDashboardDto) || chartMap.get(chartId) == null) {
            CartesianChartModel linearModel = createLinearModel(chartId);
            publisherCacheDashboardDto = publisherDashboardDto;
            chartMap.put(chartId, linearModel);
        }
        return chartMap.get(chartId);
    }

    public CartesianChartModel getAgencyLinearModel(String chartId) {
        if (agencyDashboardDto == null || !agencyDashboardDto.equals(cacheAgencyDashboardDto) || chartMap.get(chartId) == null) {
            CartesianChartModel linearModel = createLinearModel(chartId);
            cacheAgencyDashboardDto = agencyDashboardDto;
            chartMap.put(chartId, linearModel);
        }
        return chartMap.get(chartId);
    }

    private CartesianChartModel createLinearModel(String chartId) {
        linearModel = new CartesianChartModel();

        minTimeRef = -1;
        maxTimeRef = -1;

        maxDoubleRef = -1D;

        Interval interval;
        if (getDatePickerMBean().getPreviousSelection().equals("1") || getDatePickerMBean().getPreviousSelection().equals("2")) {
            interval = Interval.HOURS;
        } else {
            interval = Interval.DAY;
        }

        List<Map<Object, Number>> seriesList = getChartData(chartId, interval);
        int i = 0;
        for (Map<Object, Number> serie : seriesList) {

            if (serie != null && !serie.isEmpty()) {
                LineChartSeries series1 = fillSerie(serie, "Serie" + i);
                linearModel.addSeries(series1);
                i++;
            } else {
                Map<Object, Number> m = new HashMap<Object, Number>();
                Timestamp tm = new Timestamp(getFrom().getTime());
                m.put(tm, 0.0);
                tm = new Timestamp(getTo().getTime());
                m.put(tm, 0.0);
                LineChartSeries emptySerie = fillSerie(m, "Serie" + i);
                linearModel.addSeries(emptySerie);
            }
        }

        return linearModel;
    }

    private LineChartSeries fillSerie(Map<Object, Number> seriesMap, String name) {
        LineChartSeries serie = new LineChartSeries();
        serie.setLabel(name);
        Iterator<Object> it = seriesMap.keySet().iterator();

        while (it.hasNext()) {
            java.sql.Timestamp object = (java.sql.Timestamp) it.next();
            Double dob = (Double) seriesMap.get(object);
            long time = object.getTime();
            if (minTimeRef == -1 || time < minTimeRef) {
                minTimeRef = time;
            }
            if (maxTimeRef == -1 || time > maxTimeRef) {
                maxTimeRef = time;
            }
            if (maxDoubleRef == -1 || dob > maxDoubleRef) {
                maxDoubleRef = dob;
            }
            serie.set(time, dob);
        }

        return serie;
    }

    private List<Map<Object, Number>> getChartData(String chartId, Interval interval) {
        List<Map<Object, Number>> seriesMap = null;
        if ("small1".equals(chartId)) {
            // impressions
            seriesMap = dService.getChartData(dashboardDto, Report.IMPRESSIONS, false);
        } else if ("small2".equals(chartId)) {
            // ctr
            seriesMap = dService.getChartData(dashboardDto, Report.CTR, false);
        } else if ("small3".equals(chartId)) {
            // clicks
            seriesMap = dService.getChartData(dashboardDto, Report.CLICKS, false);
        } else if ("small4".equals(chartId)) {
            // conversions
            seriesMap = dService.getChartData(dashboardDto, Report.CONVERSION, false);
        } else if ("small5".equals(chartId)) {
            // costconversion
            seriesMap = dService.getChartData(dashboardDto, Report.COST_PER_CONVERSION_MOCKED, false);
        } else if ("small6".equals(chartId)) {
            // spend
            seriesMap = dService.getChartData(dashboardDto, Report.SPEND, false);
        } else if ("linear".equals(chartId)) {
            // spend
            seriesMap = dService.getChartData(dashboardDto, Report.IMPRESSIONS, true);
        } else if ("linear1".equals(chartId)) {
            // spend
            seriesMap = dService.getChartData(dashboardDto, Report.CTR, true);
        } else if ("linear2".equals(chartId)) {
            // spend
            seriesMap = dService.getChartData(dashboardDto, Report.CLICKS, true);

        } else if ("linear3".equals(chartId)) {
            // spend
            seriesMap = dService.getChartData(dashboardDto, Report.CONVERSION, true);

        } else if ("linear4".equals(chartId)) {
            // spend
            seriesMap = dService.getChartData(dashboardDto, Report.COST_PER_CONVERSION_MOCKED, true);
        } else if ("linear5".equals(chartId)) {
            // spend
            seriesMap = dService.getChartData(dashboardDto, Report.SPEND, true);
        } else // Publisher charts
            if ("small11".equals(chartId)) {
                // requests
                seriesMap = dService.getChartData(publisherDashboardDto, null, PublisherReport.REQUESTS,
                        interval == null ? Utils.getChartsInterval(publisherDashboardDto.getFrom(), publisherDashboardDto.getTo(), chartId)
                            : interval, false);
            } else if ("small12".equals(chartId)) {
                // impressions
                seriesMap = dService.getChartData(publisherDashboardDto, null, PublisherReport.IMPRESSIONS,
                        interval == null ? Utils.getChartsInterval(publisherDashboardDto.getFrom(), publisherDashboardDto.getTo(), chartId)
                            : interval, false);
            } else if ("small13".equals(chartId)) {
                // fill rate
                seriesMap = dService.getChartData(publisherDashboardDto, null, PublisherReport.FILL_RATE,
                        interval == null ? Utils.getChartsInterval(publisherDashboardDto.getFrom(), publisherDashboardDto.getTo(), chartId)
                            : interval, false);
            } else if ("small14".equals(chartId)) {
                // revenue
                seriesMap = dService.getChartData(publisherDashboardDto, null, PublisherReport.REVENUE,
                        interval == null ? Utils.getChartsInterval(publisherDashboardDto.getFrom(), publisherDashboardDto.getTo(), chartId)
                            : interval, false);
            } else if ("small16".equals(chartId)) {
                // ecpm
                seriesMap = dService.getChartData(publisherDashboardDto, null, PublisherReport.ECPM,
                        interval == null ? Utils.getChartsInterval(publisherDashboardDto.getFrom(), publisherDashboardDto.getTo(), chartId)
                            : interval, false);
            } else if ("linear11".equals(chartId)) {
                // requests
                seriesMap = dService.getChartData(publisherDashboardDto, null, PublisherReport.REQUESTS,
                        interval == null ? Utils.getChartsInterval(publisherDashboardDto.getFrom(), publisherDashboardDto.getTo(), chartId)
                            : interval, true);
            } else if ("linear12".equals(chartId)) {
                // impressions
                seriesMap = dService.getChartData(publisherDashboardDto, null, PublisherReport.IMPRESSIONS,
                        interval == null ? Utils.getChartsInterval(publisherDashboardDto.getFrom(), publisherDashboardDto.getTo(), chartId)
                            : interval, true);
            } else if ("linear13".equals(chartId)) {
                // fill rate
                seriesMap = dService.getChartData(publisherDashboardDto, null, PublisherReport.FILL_RATE,
                        interval == null ? Utils.getChartsInterval(publisherDashboardDto.getFrom(), publisherDashboardDto.getTo(), chartId)
                            : interval, true);

            } else if ("linear14".equals(chartId)) {
                // revenue
                seriesMap = dService.getChartData(publisherDashboardDto, null, PublisherReport.REVENUE,
                        interval == null ? Utils.getChartsInterval(publisherDashboardDto.getFrom(), publisherDashboardDto.getTo(), chartId)
                            : interval, true);

            } else if ("linear15".equals(chartId)) {
                // ecpm
                seriesMap = dService.getChartData(publisherDashboardDto, null, PublisherReport.ECPM,
                        interval == null ? Utils.getChartsInterval(publisherDashboardDto.getFrom(), publisherDashboardDto.getTo(), chartId)
                            : interval, true);
            } else if ("linear21".equals(chartId)) {
                // ecpm
                if (CollectionUtils.isEmpty(agencyDashboardDto.getAdvertisers())) {
                    UserDTO userDto = (UserDTO) getUserSessionBean().getMap().get(Constants.USERDTO);
                    agencyDashboardDto.getAdvertisersRequested().addAll(userDto.getAdvertiserListDto());
                } else {
                    agencyDashboardDto.getAdvertisersRequested().addAll(agencyDashboardDto.getAdvertisers());
                }
                seriesMap = dService.getChartData(agencyDashboardDto, null, null,
                        interval == null ? Utils.getChartsInterval(agencyDashboardDto.getFrom(), agencyDashboardDto.getTo(), chartId)
                            : interval, true);
            }
        return seriesMap;
    }

    public void doChartToggle(ActionEvent event) {
        String comp = (String) event.getComponent().getAttributes().get("chart");
        renderChart1 = false;
        renderChart2 = false;
        renderChart3 = false;
        renderChart4 = false;
        renderChart6 = false;
        if ("renderChart1".equals(comp)) {
            renderChart1 = true;
        } else if ("renderChart2".equals(comp)) {
            renderChart2 = true;
        } else if ("renderChart3".equals(comp)) {
            renderChart3 = true;
        } else if ("renderChart4".equals(comp)) {
            renderChart4 = true;
        } else if ("renderChart6".equals(comp)) {
            renderChart6 = true;
        } else // Publisher charts
            if ("renderChart11".equals(comp)) {
                renderChart1 = true;
            } else if ("renderChart12".equals(comp)) {
                renderChart2 = true;
            } else if ("renderChart13".equals(comp)) {
                renderChart3 = true;
            } else if ("renderChart14".equals(comp)) {
                renderChart4 = true;
            } else if ("renderChart16".equals(comp)) {
                renderChart6 = true;
            }
    }

    private Date getFrom() {
        if (dashboardDto != null) {
            return dashboardDto.getFrom();
        } else if (publisherDashboardDto != null) {
            return publisherDashboardDto.getFrom();
        } else if (agencyDashboardDto != null) {
            return agencyDashboardDto.getFrom();
        } else {
            LOGGER.error("dashboardDto not initialized");
            return null;
        }
    }

    private Date getTo() {
        if (dashboardDto != null) {
            return dashboardDto.getTo();
        } else if (publisherDashboardDto != null) {
            return publisherDashboardDto.getTo();
        } else if (agencyDashboardDto != null) {
            return agencyDashboardDto.getTo();
        } else {
            LOGGER.error("dashboardDto not initialized");
            return null;
        }
    }

    public boolean getRenderChart1() {
        return renderChart1;
    }

    public void setRenderChart1(boolean renderChart1) {
        this.renderChart1 = renderChart1;
    }

    public boolean getRenderChart2() {
        return renderChart2;
    }

    public void setRenderChart2(boolean renderChart2) {
        this.renderChart2 = renderChart2;
    }

    public boolean getRenderChart3() {
        return renderChart3;
    }

    public void setRenderChart3(boolean renderChart3) {
        this.renderChart3 = renderChart3;
    }

    public boolean getRenderChart4() {
        return renderChart4;
    }

    public void setRenderChart4(boolean renderChart4) {
        this.renderChart4 = renderChart4;
    }

    public boolean getRenderChart6() {
        return renderChart6;
    }

    public void setRenderChart6(boolean renderChart6) {
        this.renderChart6 = renderChart6;
    }

    public DashboardDto getDashboardDto() {
        return dashboardDto;
    }

    public void setDashboardDto(DashboardDto dashboardDto) {
        this.dashboardDto = dashboardDto;
    }

    @Override
    protected void init() {
    }

    public Map<String, CartesianChartModel> getChartMap() {
        return chartMap;
    }

    public void setChartMap(Map<String, CartesianChartModel> chartMap) {
        this.chartMap = chartMap;
    }

    public Map<String, ChartValuesDto> getChartValuesMap() {
        return chartValuesMap;
    }

    public void setChartValuesMap(Map<String, ChartValuesDto> chartValuesMap) {
        this.chartValuesMap = chartValuesMap;
    }

    public DashboardDto getCacheDashboardDto() {
        return cacheDashboardDto;
    }

    public void setCacheDashboardDto(DashboardDto cacheDashboardDto) {
        this.cacheDashboardDto = cacheDashboardDto;
    }

    public String getFilterBy() {
        return filterBy;

    }

    public void setFilterBy(String filterBy) {
        this.filterBy = filterBy;
    }

    public PublisherDashboardDto getPublisherDashboardDto() {
        return publisherDashboardDto;
    }

    public void setPublisherDashboardDto(PublisherDashboardDto publisherDashboardDto) {
        this.publisherDashboardDto = publisherDashboardDto;
    }

    public PublisherDashboardDto getPublisherCacheDashboardDto() {
        return publisherCacheDashboardDto;
    }

    public void setPublisherCacheDashboardDto(PublisherDashboardDto publisherCacheDashboardDto) {
        this.publisherCacheDashboardDto = publisherCacheDashboardDto;
    }

    public AgencyConsoleDashboardDto getAgencyDashboardDto() {
        return agencyDashboardDto;
    }

    public void setAgencyDashboardDto(AgencyConsoleDashboardDto agencyDashboardDto) {
        this.agencyDashboardDto = agencyDashboardDto;
    }

    public AgencyConsoleDashboardDto getCacheAgencyDashboardDto() {
        return cacheAgencyDashboardDto;
    }

    public void setCacheAgencyDashboardDto(AgencyConsoleDashboardDto cacheAgencyDashboardDto) {
        this.cacheAgencyDashboardDto = cacheAgencyDashboardDto;
    }
}
