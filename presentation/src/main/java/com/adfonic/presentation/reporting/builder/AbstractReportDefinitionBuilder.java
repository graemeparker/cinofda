package com.adfonic.presentation.reporting.builder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.adfonic.presentation.reporting.FileReportingServiceFactory;
import com.adfonic.presentation.reporting.FileReportingServiceFactory.FileServiceType;
import com.adfonic.presentation.reporting.model.Color;
import com.adfonic.presentation.reporting.model.Column;
import com.adfonic.presentation.reporting.model.ColumnsDetails;
import com.adfonic.presentation.reporting.model.FileReportingService;
import com.adfonic.presentation.reporting.model.HeaderDetails;
import com.adfonic.presentation.reporting.model.ReportDefinition;
import com.adfonic.presentation.reporting.model.Style;
import com.adfonic.presentation.reporting.model.Style.FontName;
import com.adfonic.presentation.reporting.model.Style.FontStyle;
import com.adfonic.presentation.reporting.model.Style.HorizontalAlignment;
import com.adfonic.presentation.reporting.model.Style.VerticalAlignment;
import com.adfonic.presentation.reporting.model.Total;
import com.adfonic.presentation.reporting.model.TotalsDetails;
import com.adfonic.presentation.reporting.model.ValueTransformer;
import com.adfonic.presentation.util.DateUtils;
import com.adfonic.presentation.util.FileUtils;
import com.ibm.icu.util.Calendar;

public abstract class AbstractReportDefinitionBuilder<T> {

    private static final String REPORT_NAME_DATEFORMAT = "yyyy-MM-dd hh:mm";
    private static String FORMAT_NUMERIC = "#,##0";
    private static String FORMAT_PERCENTAGE = "0.00 %";
    private static String FORMAT_DECIMAL = "#,##0.00";
    private static String FORMAT_CURRENCY = "$ #,##0.00";
    private static String FORMAT_DATE = DateUtils.getLongDateFormat();
    private static final String BRAND_LOGO_PATH = "resources/images/logo_excel.png";
    private static byte[] brandLogoImage = FileUtils.getResourceBytes(BRAND_LOGO_PATH);
    private static int HEADER_TITLE_COLUMN = 1;
    private static final float HEADER_HEIGHT = 45.0f;
    private static final int HEADER_FONT_SIZE = 28;
    private static final int COLUMN_FONT_SIZE = 11;

    private static final Color COLOUR_SKY_BLUE = new Color(40, 174, 204); // Sky blue
    private static final Color COLOUR_DARK_BLUE = new Color(23, 44, 66); // Dark blue
    
    private String reportName = "Report";
    protected TimeZone userTimezone = null;

    public AbstractReportDefinitionBuilder(String reportName, TimeZone userTimezone) {
        this.reportName = reportName;
        this.userTimezone = userTimezone;
    }

    public FileReportingService<T> getExcelReportingService() {
        FileReportingServiceFactory<T> factory = new FileReportingServiceFactory<T>();
        return factory.getFileService(FileServiceType.EXCEL_FILE_REPORTING_SERVICE);
    }

    public ReportDefinition<T> build(List<T> data) {

        // Header style
        HeaderDetails headerDetails = getHeaderDetails();

        // Creating reporting service
        ColumnsDetails columnsDetails = getColumnsDetails();

        // Totals info
        TotalsDetails totalsDetails = getTotalsDetails();

        return new ReportDefinition<T>(getReportName(), headerDetails, columnsDetails, totalsDetails, data, FORMAT_PERCENTAGE,
                FORMAT_DECIMAL, FORMAT_CURRENCY, getDateFormat(), FORMAT_NUMERIC);
    }

    private String getReportName() {
        Date date = Calendar.getInstance().getTime();
        if (userTimezone != null) {
            date = DateUtils.getTimezoneDate(Calendar.getInstance().getTime(), userTimezone);
        }
        return reportName + " " + new SimpleDateFormat(REPORT_NAME_DATEFORMAT).format(date);
    }

    private HeaderDetails getHeaderDetails() {
        // Column name style
        Style headerStyle = getHeaderStyle();

        // Column name style
        Style columnNameStyle = getColumnNameStyle();

        return new HeaderDetails(headerStyle, columnNameStyle, brandLogoImage, HEADER_TITLE_COLUMN, HEADER_HEIGHT);
    }

    private ColumnsDetails getColumnsDetails() {
        // Column list
        List<Column> columnList = getColumnList();

        // Columns style
        Style columnsStyle = getColumnsStyle();

        // Value transformer
        ValueTransformer valueTransformer = getValueTransformer();

        return new ColumnsDetails(columnList, columnsStyle, valueTransformer);
    }

    private TotalsDetails getTotalsDetails() {
        // Totals style
        Style totalsStyle = getTotalsStyle();

        // Totals
        List<Total> totals = getTotals();

        return new TotalsDetails(totalsStyle, totals);
    }

    protected String getDateFormat() {
        return FORMAT_DATE;
    }

    protected Style getHeaderStyle() {
        Style columnsStyle = new Style();
        columnsStyle.setCellColor(COLOUR_DARK_BLUE);
        columnsStyle.setFontName(FontName.TAHOMA);
        columnsStyle.setFontSize((short) HEADER_FONT_SIZE);
        columnsStyle.setFontColor(Color.WHITE);
        columnsStyle.setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT);
        columnsStyle.setVerticalAlignment(VerticalAlignment.VERTICAL_CENTER);
        return columnsStyle;
    }

    protected Style getColumnNameStyle() {
        Style columnNameStyle = new Style();
        columnNameStyle.setCellColor(COLOUR_SKY_BLUE);
        columnNameStyle.setFontName(FontName.TAHOMA);
        columnNameStyle.setFontSize((short) COLUMN_FONT_SIZE);
        columnNameStyle.setFontColor(Color.WHITE);
        columnNameStyle.setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT);
        return columnNameStyle;
    }

    protected Style getTotalsStyle() {
        Style totalsStyle = new Style();
        totalsStyle.setFontName(FontName.HELVETICA);
        totalsStyle.setFontSize((short) COLUMN_FONT_SIZE);
        totalsStyle.setFontStyle(FontStyle.BOLD);
        totalsStyle.setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT);
        return totalsStyle;
    }

    protected Style getColumnsStyle() {
        Style totalsStyle = new Style();
        totalsStyle.setFontName(FontName.HELVETICA);
        totalsStyle.setFontSize((short) COLUMN_FONT_SIZE);
        totalsStyle.setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT);
        return totalsStyle;
    }

    protected ValueTransformer getValueTransformer() {
        return null;
    }

    public abstract List<Column> getColumnList();

    public abstract List<Total> getTotals();
}
