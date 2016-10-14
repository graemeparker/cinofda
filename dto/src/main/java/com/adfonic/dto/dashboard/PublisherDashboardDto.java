package com.adfonic.dto.dashboard;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.adfonic.dto.dashboard.DashboardParameters.PublisherReport;
import com.adfonic.dto.dashboard.DashboardParameters.PublisherSortBy;
import com.adfonic.dto.dashboard.statistic.PublisherHeadlineStatsDto;
import com.adfonic.dto.dashboard.statistic.PublisherStatisticsDto;
import com.adfonic.dto.publication.enums.Approval;
import com.adfonic.dto.publication.enums.Backfill;
import com.adfonic.dto.publication.enums.PublicationStatus;
import com.adfonic.dto.publication.publicationtype.PublicationtypeDto;
import com.adfonic.dto.publication.typeahead.PublicationTypeAheadDto;
import com.adfonic.dto.publisher.PublisherDto;

public class PublisherDashboardDto extends BaseDashboardDto {
    
    private static final long serialVersionUID = 1L;

    private List<PublicationTypeAheadDto> publications = new ArrayList<PublicationTypeAheadDto>();

    private List<PublicationTypeAheadDto> publicationsFiltered = new ArrayList<PublicationTypeAheadDto>(0);

    private List<Long> publicationsIdFiltered = new ArrayList<Long>(0);

    private PublisherDto publisherDto;

    private PublisherHeadlineStatsDto publisherHeadlineStatsDto;

    private PublicationStatus publicationStatusFilter = PublicationStatus.ALL;

    private PublicationtypeDto platformFilter = new PublicationtypeDto();

    private Approval approvalFilter = Approval.ALL;

    private Backfill backfillFilter = Backfill.ALL;

    private List<PublisherStatisticsDto> reportingTable = new ArrayList<PublisherStatisticsDto>(0);

    private PublisherSortBy sortBy = PublisherSortBy.PUBLICATION_NAME;

    private PublisherReport report = PublisherReport.REQUESTS;

    private boolean isStatusFiltered = false;

    private boolean isPlatformFiltered = false;

    private boolean isApprovalFiltered = false;

    private boolean isBackfillFiltered = false;

    private boolean recentlyFiltered = false;

    public boolean isFiltered() {
        boolean anyFilterApplied = isStatusFiltered || isPlatformFiltered || isApprovalFiltered || isBackfillFiltered;
        return anyFilterApplied || !CollectionUtils.isEmpty(publications);
    }

    public List<PublicationTypeAheadDto> getPublications() {
        return publications;
    }

    public void setPublications(List<PublicationTypeAheadDto> publications) {
        this.publications = publications;
    }

    public PublisherDto getPublisherDto() {
        return publisherDto;
    }

    public void setPublisherDto(PublisherDto publisherDto) {
        this.publisherDto = publisherDto;
    }

    public PublisherHeadlineStatsDto getPublisherHeadlineStatsDto() {
        return publisherHeadlineStatsDto;
    }

    public void setPublisherHeadlineStatsDto(PublisherHeadlineStatsDto publisherHeadlineStatsDto) {
        this.publisherHeadlineStatsDto = publisherHeadlineStatsDto;
    }

    public PublicationStatus getPublicationStatusFilter() {
        return publicationStatusFilter;
    }

    public void setPublicationStatusFilter(PublicationStatus publicationStatusFilter) {
        if (publicationStatusFilter != null) {
            this.publicationStatusFilter = publicationStatusFilter;
        }
    }

    public PublicationtypeDto getPlatformFilter() {
        return platformFilter;
    }

    public void setPlatformFilter(PublicationtypeDto platformFilter) {
        if (platformFilter != null) {
            this.platformFilter = platformFilter;
        }
    }

    public Approval getApprovalFilter() {
        return approvalFilter;
    }

    public void setApprovalFilter(Approval approvalFilter) {
        if (approvalFilter != null) {
            this.approvalFilter = approvalFilter;
        }
    }

    public Backfill getBackfillFilter() {
        return backfillFilter;
    }

    public void setBackfillFilter(Backfill backfillFilter) {
        if (backfillFilter != null) {
            this.backfillFilter = backfillFilter;
        }
    }

    public PublisherSortBy getSortBy() {
        return sortBy;
    }

    public void setSortBy(PublisherSortBy sortBy) {
        this.sortBy = sortBy;
    }

    public PublisherReport getReport() {
        return report;
    }

    public void setReport(PublisherReport report) {
        this.report = report;
    }

    public List<PublicationTypeAheadDto> getPublicationsFiltered() {
        return publicationsFiltered;
    }

    public void setPublicationsFiltered(List<PublicationTypeAheadDto> publicationsFiltered) {
        this.publicationsFiltered = publicationsFiltered;
    }

    public List<Long> getPublicationsIdFiltered() {
        return publicationsIdFiltered;
    }

    public void setPublicationsIdFiltered(List<Long> publicationsIdFiltered) {
        this.publicationsIdFiltered = publicationsIdFiltered;
    }

    public List<PublisherStatisticsDto> getReportingTable() {
        return reportingTable;
    }

    public void setReportingTable(List<PublisherStatisticsDto> reportingTable) {
        this.reportingTable = reportingTable;
    }

    public boolean isStatusFiltered() {
        return isStatusFiltered;
    }

    public void setStatusFiltered(boolean isStatusFiltered) {
        this.isStatusFiltered = isStatusFiltered;
    }

    public boolean isPlatformFiltered() {
        return isPlatformFiltered;
    }

    public void setPlatformFiltered(boolean isPlatformFiltered) {
        this.isPlatformFiltered = isPlatformFiltered;
    }

    public boolean isApprovalFiltered() {
        return isApprovalFiltered;
    }

    public void setApprovalFiltered(boolean isApprovalFiltered) {
        this.isApprovalFiltered = isApprovalFiltered;
    }

    public boolean isBackfillFiltered() {
        return isBackfillFiltered;
    }

    public void setBackfillFiltered(boolean isBackfillFiltered) {
        this.isBackfillFiltered = isBackfillFiltered;
    }

    public boolean isRecentlyFiltered() {
        return recentlyFiltered;
    }

    public void setRecentlyFiltered(boolean recentlyFiltered) {
        this.recentlyFiltered = recentlyFiltered;
    }
}
