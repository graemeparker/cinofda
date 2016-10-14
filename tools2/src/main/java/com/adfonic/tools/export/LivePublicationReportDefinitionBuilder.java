package com.adfonic.tools.export;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import com.adfonic.domain.OptimisationReportFields;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.reporting.builder.AbstractReportDefinitionBuilder;
import com.adfonic.presentation.reporting.model.BinaryExpression;
import com.adfonic.presentation.reporting.model.BinaryExpression.Operator;
import com.adfonic.presentation.reporting.model.CellType;
import com.adfonic.presentation.reporting.model.Column;
import com.adfonic.presentation.reporting.model.Expression;
import com.adfonic.presentation.reporting.model.FunctionExpression;
import com.adfonic.presentation.reporting.model.FunctionExpression.Function;
import com.adfonic.presentation.reporting.model.Total;
import com.adfonic.tools.beans.optimisation.OptimisationFields;

public class LivePublicationReportDefinitionBuilder<T> extends AbstractReportDefinitionBuilder<T> {

    private static final Integer COLUMN_KEY_INDEX = 0;
    private static final Integer COLUMN_VALUE_INDEX = 1;
    private static final Integer COLUMN_TYPE_INDEX = 2;

    private static Object[][] columnsInfo = new Object[][] {
            { OptimisationFields.COLUMN_KEY_PARTIALY_REMOVED, "page.optimisation.labels.table.header.label.part_removed_flag",
                    CellType.BOOLEAN },
            { OptimisationFields.COLUMN_KEY_PUBLICATION_NAME, "page.optimisation.labels.table.header.label.publication", CellType.STRING },
            { OptimisationFields.COLUMN_KEY_PUBLICATION_TYPE, "page.optimisation.labels.table.header.label.publication_type",
                    CellType.STRING },
            { OptimisationFields.COLUMN_KEY_CREATIVE_NAME, "page.optimisation.labels.table.header.label.creative", CellType.STRING },
            { OptimisationFields.COLUMN_KEY_PUB_EXT_ID, "page.optimisation.labels.table.header.label.publication_external_id",
                    CellType.STRING },
            { OptimisationFields.COLUMN_KEY_PUB_BUNDLE, "page.optimisation.labels.table.header.label.publication_bundle",
                    CellType.STRING },
            { OptimisationFields.COLUMN_KEY_IAB_CATEGORY, "page.optimisation.labels.table.header.label.iab_category", CellType.STRING },
            { OptimisationFields.COLUMN_KEY_INVENTORY_SOURCE, "page.optimisation.labels.table.header.label.inventory_source",
                    CellType.STRING },
            { OptimisationFields.COLUMN_KEY_BIDS, "page.optimisation.labels.table.header.label.bids", CellType.NUMERIC },
            { OptimisationFields.COLUMN_KEY_IMPRESSIONS, "page.optimisation.labels.table.header.label.impressions", CellType.NUMERIC },
            { OptimisationFields.COLUMN_KEY_WINRATE, "page.optimisation.labels.table.header.label.win_rate", CellType.PERCENTAGE },
            { OptimisationFields.COLUMN_KEY_CLICKS, "page.optimisation.labels.table.header.label.clicks", CellType.NUMERIC },
            { OptimisationFields.COLUMN_KEY_CTR, "page.optimisation.labels.table.header.label.ctr", CellType.PERCENTAGE },
            { OptimisationFields.COLUMN_KEY_CONVERSIONS, "page.optimisation.labels.table.header.label.conversions", CellType.NUMERIC },
            { OptimisationFields.COLUMN_KEY_CVR, "page.optimisation.labels.table.header.label.cvr", CellType.PERCENTAGE },
            { OptimisationFields.COLUMN_KEY_SPEND, "page.optimisation.labels.table.header.label.spend", CellType.CURRENCY },
            { OptimisationFields.COLUMN_KEY_ECPM, "page.optimisation.labels.table.header.label.ecpm", CellType.CURRENCY },
            { OptimisationFields.COLUMN_KEY_ECPC, "page.optimisation.labels.table.header.label.ecpc", CellType.CURRENCY },
            { OptimisationFields.COLUMN_KEY_ECPA, "page.optimisation.labels.table.header.label.ecpa", CellType.CURRENCY } };

    private static final Expression EXP_BIDS = new FunctionExpression(Function.SUM, OptimisationFields.COLUMN_KEY_BIDS);
    private static final Expression EXP_IMPRESSIONS = new FunctionExpression(Function.SUM, OptimisationFields.COLUMN_KEY_IMPRESSIONS);
    private static final Expression EXP_WINRATE = new BinaryExpression(EXP_IMPRESSIONS, Operator.DIV, EXP_BIDS);
    private static final Expression EXP_CLICKS = new FunctionExpression(Function.SUM, OptimisationFields.COLUMN_KEY_CLICKS);
    private static final Expression EXP_CTR = new BinaryExpression(EXP_CLICKS, Operator.DIV, EXP_IMPRESSIONS);
    private static final Expression EXP_CONVERSIONS = new FunctionExpression(Function.SUM, OptimisationFields.COLUMN_KEY_CONVERSIONS);
    private static final Expression EXP_CVR = new BinaryExpression(EXP_CONVERSIONS, Operator.DIV, EXP_IMPRESSIONS);
    private static final Expression EXP_SPEND = new FunctionExpression(Function.SUM, OptimisationFields.COLUMN_KEY_SPEND);
    private static final Expression EXP_ECPM = new BinaryExpression(EXP_SPEND, Operator.DIV, EXP_IMPRESSIONS);
    private static final Expression EXP_ECPC = new BinaryExpression(EXP_SPEND, Operator.DIV, EXP_CLICKS);
    private static final Expression EXP_ECPA = new BinaryExpression(EXP_SPEND, Operator.DIV, EXP_CONVERSIONS);

    private static final List<Total> TOTALS = new ArrayList<Total>();
    static {
        TOTALS.add(new Total(OptimisationFields.COLUMN_KEY_BIDS, EXP_BIDS));
        TOTALS.add(new Total(OptimisationFields.COLUMN_KEY_IMPRESSIONS, EXP_IMPRESSIONS));
        TOTALS.add(new Total(OptimisationFields.COLUMN_KEY_WINRATE, EXP_WINRATE));
        TOTALS.add(new Total(OptimisationFields.COLUMN_KEY_CLICKS, EXP_CLICKS));
        TOTALS.add(new Total(OptimisationFields.COLUMN_KEY_CTR, EXP_CTR));
        TOTALS.add(new Total(OptimisationFields.COLUMN_KEY_CONVERSIONS, EXP_CONVERSIONS));
        TOTALS.add(new Total(OptimisationFields.COLUMN_KEY_CVR, EXP_CVR));
        TOTALS.add(new Total(OptimisationFields.COLUMN_KEY_SPEND, EXP_SPEND));
        TOTALS.add(new Total(OptimisationFields.COLUMN_KEY_ECPM, EXP_ECPM));
        TOTALS.add(new Total(OptimisationFields.COLUMN_KEY_ECPC, EXP_ECPC));
        TOTALS.add(new Total(OptimisationFields.COLUMN_KEY_ECPA, EXP_ECPA));
    }

    private Set<OptimisationReportFields> reportFields;

    public LivePublicationReportDefinitionBuilder(TimeZone userTimezone, Set<OptimisationReportFields> reportFields) {
        super("Live Publications", userTimezone);
        this.reportFields = reportFields;
    }

    @Override
    public List<Column> getColumnList() {
        List<Column> columns = new ArrayList<Column>(columnsInfo.length);

        Column column = null;

        for (int cnt = 0; cnt < columnsInfo.length; cnt++) {
            String columnNameKey = (String) columnsInfo[cnt][COLUMN_KEY_INDEX];
            if (OptimisationFields.isFieldVisible(columnNameKey, reportFields)) {
                column = new Column(FacesUtils.getFacesMessageForId((String) columnsInfo[cnt][COLUMN_VALUE_INDEX]).getDetail(), columnNameKey,
                        (CellType) columnsInfo[cnt][COLUMN_TYPE_INDEX], null);
                columns.add(column);
            }
        }

        return columns;
    }

    @Override
    public List<Total> getTotals() {
        List<Total> filteredTotals = new ArrayList<Total>();
        for (Total total : TOTALS) {
            if (OptimisationFields.isFieldVisible(total.getColumnName(), reportFields)) {
                filteredTotals.add(total);
            }
        }
        return filteredTotals;
    }
}
