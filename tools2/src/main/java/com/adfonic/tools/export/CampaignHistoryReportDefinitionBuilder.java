package com.adfonic.tools.export;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import com.adfonic.dto.audience.CampaignAudienceDto;
import com.adfonic.dto.campaign.creative.CreativeDto;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.reporting.builder.AbstractReportDefinitionBuilder;
import com.adfonic.presentation.reporting.model.CellType;
import com.adfonic.presentation.reporting.model.Column;
import com.adfonic.presentation.reporting.model.Total;
import com.adfonic.presentation.reporting.model.ValueTransformer;
import com.adfonic.presentation.util.Constants;
import com.adfonic.tools.beans.campaign.history.CampaignHistoryValueTransformer;

public class CampaignHistoryReportDefinitionBuilder<T> extends AbstractReportDefinitionBuilder<T> {

    private static final String FORMAT_DATE = Constants.DEFAULT_DATE_FORMAT + Constants.DEFAULT_TIMESTAMP_FORMAT;
    private static final Integer OLD_AND_NEW_VALUES_COLUMN_WIDTH = 50;

    private static final Integer COLUMN_KEY_INDEX = 0;
    private static final Integer COLUMN_VALUE_INDEX = 1;
    private static final Integer COLUMN_TYPE_INDEX = 2;
    private static final Integer COLUMN_WIDTH_INDEX = 3;

    private static Object[][] columnsInfo = new Object[][] {
            { CampaignHistoryValueTransformer.COLUMN_KEY_TIMESTAMP, "page.campaign.history.table.header.time", CellType.STRING, null },
            { CampaignHistoryValueTransformer.COLUMN_KEY_USER_NAME, "page.campaign.history.table.header.user.name", CellType.STRING, null },
            { CampaignHistoryValueTransformer.COLUMN_KEY_USER_EMAIL, "page.campaign.history.table.header.user.email", CellType.STRING, null },
            { CampaignHistoryValueTransformer.COLUMN_KEY_NAME, "page.campaign.history.table.header.field", CellType.STRING, null },
            { CampaignHistoryValueTransformer.COLUMN_KEY_OLD_VALUE, "page.campaign.history.table.header.from", CellType.STRING,
                    OLD_AND_NEW_VALUES_COLUMN_WIDTH },
            { CampaignHistoryValueTransformer.COLUMN_KEY_NEW_VALUE, "page.campaign.history.table.header.to", CellType.STRING,
                    OLD_AND_NEW_VALUES_COLUMN_WIDTH },
            { CampaignHistoryValueTransformer.COLUMN_KEY_TOOLTIP, "page.campaign.history.table.header.tooltip", CellType.STRING,
                    OLD_AND_NEW_VALUES_COLUMN_WIDTH } };

    private List<CreativeDto> creatives = null;
    private List<CampaignAudienceDto> audiences = null;

    public CampaignHistoryReportDefinitionBuilder(TimeZone userTimezone, List<CreativeDto> creatives, List<CampaignAudienceDto> audiences) {
        super("Campaign history", userTimezone);
        this.creatives = creatives;
        this.audiences = audiences;
    }

    @Override
    public List<Column> getColumnList() {
        List<Column> columns = new ArrayList<Column>(columnsInfo.length);

        Column column = null;
        for (int cnt = 0; cnt < columnsInfo.length; cnt++) {
            column = new Column(FacesUtils.getFacesMessageForId((String) columnsInfo[cnt][COLUMN_VALUE_INDEX]).getDetail(),
                    (String) columnsInfo[cnt][COLUMN_KEY_INDEX], (CellType) columnsInfo[cnt][COLUMN_TYPE_INDEX],
                    (Integer) columnsInfo[cnt][COLUMN_WIDTH_INDEX]);
            columns.add(column);
        }

        return columns;
    }

    @Override
    protected ValueTransformer getValueTransformer() {
        return new CampaignHistoryValueTransformer(false, userTimezone, true, creatives, audiences);
    }

    @Override
    public List<Total> getTotals() {
        return null; // No totals are defined within this report
    }

    @Override
    protected String getDateFormat() {
        return FORMAT_DATE;
    }

}
