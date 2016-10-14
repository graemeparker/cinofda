package com.adfonic.tools.beans.campaign.history;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.dto.audience.CampaignAudienceDto;
import com.adfonic.dto.auditlog.AuditLogDto;
import com.adfonic.dto.auditlog.AuditLogSearchDto;
import com.adfonic.dto.campaign.CampaignDto;
import com.adfonic.dto.campaign.creative.CreativeDto;
import com.adfonic.presentation.auditlog.service.AuditLogService;
import com.adfonic.presentation.campaign.CampaignService;
import com.adfonic.presentation.company.CompanyService;
import com.adfonic.presentation.reporting.model.ReportDefinition;
import com.adfonic.presentation.util.Constants;
import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.adfonic.tools.export.CampaignHistoryReportDefinitionBuilder;

@Component
@Scope("view")
public class CampaignHistoryMBean extends GenericAbstractBean {

    private static String DEFAULT_SORT_FIELD = "id";
    private static Integer LAST_CHANGES_COUNT = 10;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private CampaignService campaignService;

    private LazyDataModel<AuditLogDto> lazyModel;
    private CampaignDto campaignDto;
    private TimeZone userTimezone;

    @Override
    @PostConstruct
    public void init() {

        this.lazyModel = new LazyDataModel<AuditLogDto>() {

            private static final long serialVersionUID = 1L;

            @Override
            public List<AuditLogDto> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters) {

                AuditLogSearchDto searchDto = new AuditLogSearchDto(campaignDto, null, first, pageSize, getSortField(sortField),
                        getSortOrder(sortOrder));

                List<AuditLogDto> campaignHistory = auditLogService.getAuditLogHistoryForCampaign(searchDto);
                setRowCount(campaignHistory.size());
                return campaignHistory;
            }

            private String getSortField(String sortField) {
                String result = sortField;
                if (result == null) {
                    result = DEFAULT_SORT_FIELD;
                }
                return result;
            }

            private Boolean getSortOrder(SortOrder sortOrder) {
                Boolean isAscending = null;
                switch (sortOrder) {
                case ASCENDING:
                    isAscending = true;
                    break;
                case DESCENDING:
                    isAscending = false;
                    break;
                default:
                    break;
                }
                return isAscending;
            }
        };

        userTimezone = companyService.getTimeZoneForAdvertiser(getUser().getAdvertiserDto());
    }

    // Methods used by the view

    /**
     * Generate excel report based on campaign history from a past date.
     */
    public StreamedContent exportCampaignHistoryToExcel() throws IOException {
        // SearchDTO parameter
        AuditLogSearchDto searchDto = new AuditLogSearchDto(campaignDto, null, 0, 0, DEFAULT_SORT_FIELD, false);

        // Getting several month campaign history order by time stamp
        List<AuditLogDto> allRows = auditLogService.getAuditLogHistoryForCampaign(searchDto);

        // Running report
        CampaignHistoryReportDefinitionBuilder<AuditLogDto> builder = new CampaignHistoryReportDefinitionBuilder<AuditLogDto>(userTimezone,
                getCampaignCreatives(), getCampaignAudiences());
        ReportDefinition<AuditLogDto> reportDefinition = builder.build(allRows);
        ByteArrayOutputStream osReport = (ByteArrayOutputStream) builder.getExcelReportingService().createReport(reportDefinition);

        return new DefaultStreamedContent(new ByteArrayInputStream(osReport.toByteArray()), Constants.CONTENT_TYPE_EXCEL_XLSX, "Campaign history "
                + this.campaignDto.getName() + ".xlsx");
    }

    // Setters called by campaign work flow

    public void setCampaignDto(CampaignDto campaignDto) {
        this.campaignDto = campaignDto;
    }

    // Getters used by views

    public LazyDataModel<AuditLogDto> getLazyModel() {
        return lazyModel;
    }

    public Integer getLastChangesCount() {
        return LAST_CHANGES_COUNT;
    }

    public CampaignHistoryValueTransformer getNewValueTransformer() {
        if (campaignDto != null) {
            return new CampaignHistoryValueTransformer(true, userTimezone, false, getCampaignCreatives(), getCampaignAudiences());
        } else {
            return null;
        }
    }

    private List<CreativeDto> getCampaignCreatives() {
        return campaignService.loadCreatives(campaignDto.getId()).getCreatives();
    }

    private List<CampaignAudienceDto> getCampaignAudiences() {
        return campaignService.getCampaignAudiences(campaignDto);
    }
}
