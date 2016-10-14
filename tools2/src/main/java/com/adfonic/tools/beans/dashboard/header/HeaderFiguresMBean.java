package com.adfonic.tools.beans.dashboard.header;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.dto.dashboard.BaseDashboardDto;
import com.adfonic.dto.dashboard.DashboardDto;
import com.adfonic.dto.dashboard.PublisherDashboardDto;
import com.adfonic.dto.dashboard.statistic.AdvertiserHeadlineStatsDto;
import com.adfonic.dto.dashboard.statistic.PublisherHeadlineStatsDto;
import com.adfonic.presentation.dashboard.DashboardService;
import com.adfonic.tools.beans.util.GenericAbstractBean;

@Component
@Scope("view")
public class HeaderFiguresMBean extends GenericAbstractBean implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Autowired
    private DashboardService dService;

    /** TODO remove fields when dashboard extends from basedashboarddto **/
    private DashboardDto dashboardDto;

    /**
     * Attribute only to check if we need to go again for the HeadlineStatusDto
     * **/
    private DashboardDto cacheDashboardDto;

    private BaseDashboardDto baseDashboardDto;
    /**
     * Attribute only to check if we need to go again for the HeadlineStatusDto
     * **/
    private BaseDashboardDto baseCacheDashboardDto;

    private AdvertiserHeadlineStatsDto headerDto;

    private PublisherHeadlineStatsDto publisherHeaderDto;

    @Override
    protected void init() {
    }

    private AdvertiserHeadlineStatsDto getHeaderFigures() {
        DashboardDto headerDto = dService.getDashboardHeader(dashboardDto);
        return headerDto.getAdvertiserHeadlineStatsDto();
    }

    private PublisherHeadlineStatsDto getPublisherHeaderFigures() {
        PublisherDashboardDto dto = (PublisherDashboardDto) dService.getDashboardHeader(baseDashboardDto);
        return dto.getPublisherHeadlineStatsDto();
    }

    public DashboardDto getDashboardDto() {
        return dashboardDto;
    }

    public void setDashboardDto(DashboardDto dashboardDto) {
        this.dashboardDto = dashboardDto;
    }

    public DashboardService getdService() {
        return dService;
    }

    public void setdService(DashboardService dService) {
        this.dService = dService;
    }

    public AdvertiserHeadlineStatsDto getHeaderDto() {
        if (headerDto == null || !dashboardDto.equals(cacheDashboardDto)) {
            headerDto = getHeaderFigures();
            cacheDashboardDto = dashboardDto;
        }
        return headerDto;
    }

    public void setHeaderDto(AdvertiserHeadlineStatsDto headerDto) {
        this.headerDto = headerDto;
    }

    public DashboardDto getCacheDashboardDto() {
        return cacheDashboardDto;
    }

    public void setCacheDashboardDto(DashboardDto cacheDashboardDto) {
        this.cacheDashboardDto = cacheDashboardDto;
    }

    public BaseDashboardDto getBaseDashboardDto() {
        return baseDashboardDto;
    }

    public void setBaseDashboardDto(BaseDashboardDto baseDashboardDto) {
        this.baseDashboardDto = baseDashboardDto;
    }

    public BaseDashboardDto getBaseCacheDashboardDto() {
        return baseCacheDashboardDto;
    }

    public void setBaseCacheDashboardDto(BaseDashboardDto baseCacheDashboardDto) {
        this.baseCacheDashboardDto = baseCacheDashboardDto;
    }

    public PublisherHeadlineStatsDto getPublisherHeaderDto() {
        if (publisherHeaderDto == null || !baseDashboardDto.equals(baseCacheDashboardDto)) {
            publisherHeaderDto = getPublisherHeaderFigures();
            baseCacheDashboardDto = baseDashboardDto;
        }
        return publisherHeaderDto;
    }

    public void setPublisherHeaderDto(PublisherHeadlineStatsDto publisherHeaderDto) {
        this.publisherHeaderDto = publisherHeaderDto;
    }

}
