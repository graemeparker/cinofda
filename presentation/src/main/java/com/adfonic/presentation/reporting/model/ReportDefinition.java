package com.adfonic.presentation.reporting.model;

import java.util.List;

/**
 * Class which contains all information needed to create the report
 * 
 * Responsibility: To provide detail information for the report 
 * 
 * @author David Martin
 *
 * @param <T> Row class
 */
public class ReportDefinition<T> {
    
    /** Report name */
    private String reportName;
    
    /** Header information */
    private HeaderDetails headerDetail;
    
    /** Columns information */
    private ColumnsDetails columnsDetail;
    
    /** Totals information */
    private TotalsDetails totalsDetail;
    
    /** Data to show in the report */
    private List<T> data;
    
    /**  Format for the cells which contains percentages values */
    private String percentageFormat;
    
    /**  Format for the cells which contains decimal values */
    private String decimalFormat;
    
    /**  Format for the cells which contains currency values */
    private String currencyFormat;
    
    /**  Format for the cells which contains date values */
    private String dateFormat;
    
    /**  Format for the cells which contains numeric values */
    private String numericFormat;
    
    /**
     * Empty constructor
     */
    public ReportDefinition(){
    }
    
    /**
     * Constructor with all possible values
     * @param reportName Report name
     * @param headerStyle Generic style to use in the header row
     * @param totalStyle Generic style to use in the totals row
     * @param columnsDetail Columns information 
     * @param totals Totals row details
     * @param data Data to show in the report
     * @param percentageFormat Format for the cells which contains percentages values
     * @param decimalFormat Format for the cells which contains decimal values
     * @param currencyFormat Format for the cells which contains currency values
     * @param dateFormat Format for the cells which contains date values
     */
    public ReportDefinition(String reportName, 
                            HeaderDetails headerDetail,
                            ColumnsDetails columnsDetail, 
                            TotalsDetails totalsDetail,
                            List<T> data,
                            String percentageFormat,
                            String decimalFormat,
                            String currencyFormat,
                            String dateFormat,
                            String numericFormat) {
        super();
        this.reportName = reportName;
        this.headerDetail = headerDetail;
        this.totalsDetail = totalsDetail;
        this.columnsDetail = columnsDetail;
        this.data = data;
        this.percentageFormat = percentageFormat;
        this.decimalFormat = decimalFormat;
        this.currencyFormat = currencyFormat;
        this.dateFormat = dateFormat;
        this.numericFormat = numericFormat;
    }

    //Getters & Setters
    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public HeaderDetails getHeaderDetail() {
        return headerDetail;
    }

    public void setHeaderDetail(HeaderDetails headerDetail) {
        this.headerDetail = headerDetail;
    }

    public TotalsDetails getTotalsDetail() {
        return totalsDetail;
    }

    public void setTotalsDetail(TotalsDetails totalsDetail) {
        this.totalsDetail = totalsDetail;
    }

    public ColumnsDetails getColumnsDetail() {
        return columnsDetail;
    }

    public void setColumnsDetail(ColumnsDetails columnsDetail) {
        this.columnsDetail = columnsDetail;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public String getPercentageFormat() {
        return percentageFormat;
    }

    public void setPercentageFormat(String percentageFormat) {
        this.percentageFormat = percentageFormat;
    }

    public String getDecimalFormat() {
        return decimalFormat;
    }

    public void setDecimalFormat(String decimalFormat) {
        this.decimalFormat = decimalFormat;
    }

    public String getCurrencyFormat() {
        return currencyFormat;
    }

    public void setCurrencyFormat(String currencyFormat) {
        this.currencyFormat = currencyFormat;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getNumericFormat() {
        return numericFormat;
    }

    public void setNumericFormat(String numericFormat) {
        this.numericFormat = numericFormat;
    }
    
}
