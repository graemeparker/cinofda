package com.adfonic.tools.export;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import com.adfonic.domain.OptimisationReportFields;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.reporting.builder.AbstractReportDefinitionBuilder;
import com.adfonic.presentation.reporting.model.CellType;
import com.adfonic.presentation.reporting.model.Column;
import com.adfonic.presentation.reporting.model.Total;

public class RemovedPublicationReportDefinitionBuilder<T> extends AbstractReportDefinitionBuilder<T> {

    private static final String COLUMN_KEY_DATA_REMOVED = "DateRemoved";

    private static final Integer COLUMN_KEY_INDEX = 0;
    private static final Integer COLUMN_VALUE_INDEX = 1;
    private static final Integer COLUMN_TYPE_INDEX = 2;

    protected static final Object[][] columnsInfo = new Object[][] { { COLUMN_KEY_DATA_REMOVED,
            "page.optimisation.labels.table.header.label.removal_unix_timestamp", CellType.DATE } };

    private LivePublicationReportDefinitionBuilder<T> livePublicationReportDefinitionBuilder = null;

    public RemovedPublicationReportDefinitionBuilder(TimeZone userTimezone, Set<OptimisationReportFields> reportFields) {
        super("Removed publications", userTimezone);
        livePublicationReportDefinitionBuilder = new LivePublicationReportDefinitionBuilder<T>(userTimezone, reportFields);
    }

    @Override
    public List<Column> getColumnList() {
        List<Column> livePublicationColumns = livePublicationReportDefinitionBuilder.getColumnList();
        List<Column> columns = new ArrayList<Column>(livePublicationColumns);

        Column column = null;
        for (int cnt = 0; cnt < columnsInfo.length; cnt++) {
            column = new Column(FacesUtils.getFacesMessageForId((String) columnsInfo[cnt][COLUMN_VALUE_INDEX]).getDetail(),
                    (String) columnsInfo[cnt][COLUMN_KEY_INDEX], (CellType) columnsInfo[cnt][COLUMN_TYPE_INDEX], null);
            columns.add(column);
        }

        return columns;
    }

    @Override
    public List<Total> getTotals() {
        return livePublicationReportDefinitionBuilder.getTotals();
    }
}
