package com.adfonic.tools.export;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.reporting.builder.AbstractReportDefinitionBuilder;
import com.adfonic.presentation.reporting.model.CellType;
import com.adfonic.presentation.reporting.model.Column;
import com.adfonic.presentation.reporting.model.Total;
import com.adfonic.presentation.reporting.model.ValueExpression;
import com.adfonic.presentation.util.Constants;
import com.adfonic.reporting.Metric;
import com.adfonic.reporting.Report;
import com.adfonic.reporting.Report.PercentColumn;
import com.adfonic.util.CurrencyUtils;

public class ReportingReportDefinitionBuilder<T> extends AbstractReportDefinitionBuilder<T> {

    private static final String MESSAGE_PREFIX = "page.reporting.commons.report.header.";

    Report report = null;

    public ReportingReportDefinitionBuilder(String reportName, TimeZone userTimezone, Report report) {
        super(reportName, userTimezone);
        this.report = report;
    }

    @Override
    public List<Column> getColumnList() {
        List<Column> columns = new ArrayList<Column>(report.getColumns().size());

        for (com.adfonic.reporting.Report.Column reportColumn : report.getColumns()) {
            columns.add(new Column(FacesUtils.getFacesMessageForId(MESSAGE_PREFIX + reportColumn.getHeader()).getDetail(), String
                    .valueOf(reportColumn.getIndex()), getType(reportColumn), null));
        }

        return columns;
    }

    private CellType getType(com.adfonic.reporting.Report.Column reportColumn) {
        CellType cellType = CellType.STRING;

        if (reportColumn.isPercentageColumn()) {
            if (reportColumn instanceof PercentColumn && ((PercentColumn) reportColumn).getNumerator().equals(Metric.COST.name())) {
                cellType = CellType.CURRENCY;
            } else {
                cellType = CellType.PERCENTAGE;
            }
        } else if (reportColumn.getDataType() == Integer.class) {
            cellType = CellType.NUMERIC;
        } else if (reportColumn.getDataType() == Long.class) {
            cellType = CellType.DECIMAL;
        } else if ((reportColumn.getDataType() == Float.class) || (reportColumn.getDataType() == Double.class)) {
            if (reportColumn.getFormat() == CurrencyUtils.CURRENCY_FORMAT_USD) {
                cellType = CellType.CURRENCY;
            } else {
                cellType = CellType.DECIMAL;
            }
        } else if (reportColumn.getDataType() == Date.class) {
            cellType = CellType.DATE;
        } else if (reportColumn.getDataType() == Boolean.class) {
            cellType = CellType.BOOLEAN;
        }

        return cellType;
    }

    @Override
    public List<Total> getTotals() {
        String[] reportTotals = report.getTotal().getRawCells();
        List<Total> totals = null;
        if (reportTotals.length > 0) {
            totals = new ArrayList<Total>(reportTotals.length);
            for (int i = 0; i < reportTotals.length; i++) {
                totals.add(new Total(String.valueOf(i), new ValueExpression(reportTotals[i])));
            }
        }

        return totals;
    }

    @Override
    protected String getDateFormat() {
        return Constants.DEFAULT_DATE_FORMAT;
    }
}
