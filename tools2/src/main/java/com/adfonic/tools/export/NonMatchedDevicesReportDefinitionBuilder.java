package com.adfonic.tools.export;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.reporting.builder.AbstractReportDefinitionBuilder;
import com.adfonic.presentation.reporting.model.CellType;
import com.adfonic.presentation.reporting.model.Column;
import com.adfonic.presentation.reporting.model.Total;

public class NonMatchedDevicesReportDefinitionBuilder<T> extends AbstractReportDefinitionBuilder<T> {

    private static final String COLUMN_KEY_NAME = "Name";
    private static final Integer COLUMN_WIDTH_NAME = 20;

    private static final Integer COLUMN_KEY_INDEX = 0;
    private static final Integer COLUMN_VALUE_INDEX = 1;
    private static final Integer COLUMN_TYPE_INDEX = 2;
    private static final Integer COLUMN_WIDTH_INDEX = 3;

    private static Object[][] columnsInfo = new Object[][] { { COLUMN_KEY_NAME, "page.campaign.targeting.labels.table.header.label.name",
            CellType.STRING, COLUMN_WIDTH_NAME } };

    public NonMatchedDevicesReportDefinitionBuilder(TimeZone userTimezone) {
        super("Non Matched Devices", userTimezone);
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
    public List<Total> getTotals() {
        return null; // No totals are defined within this report
    }
}
