package com.adfonic.presentation.reporting.model;

/**
 * Class which reflects the information needed for a report column
 * 
 * Responsibility: To provide detail information for a report column
 * 
 * @author David Martin
 */
public class Column {
    /** Value to use as column name in the report */
    private String columnNameValue;
    
    /** Key to identify the column. 
     *  It is not the same that columnNameValue, the key is used to look up its poistion in the  */
    private String columnNameKey;
    
    /** 
     * Cell type 
     * @see CellType */
    private CellType type;
    
    /** 
     * Style to apply on the header of this column, override HeaderDetails options
     * @see Style */
    private Style headerStyle;
    
    /** 
     * Style to apply on the data of this column
     * @see Style */
    private Style dataStyle;
    
    /** 
     * Style to apply on the total cell of this column, override TotalsDetails options
     * @see Style */
    private Style totalStyle;
    
    /**
     * Max width for this column
     */
    private Integer columnWidth;
    
    /**
     * Constructor
     * 
     * @param columnNameValue Value to use as column name in the report
     * @param columnNameKey Key to identify the column
     * @param type Cell type 
     */
    public Column(String columnNameValue, String columnNameKey, CellType type, Integer columnWidth) {
        super();
        this.columnNameValue = columnNameValue;
        this.columnNameKey = columnNameKey;
        this.type = type;
        this.columnWidth = columnWidth;
    }
    
    /**
     * Constructor
     * 
     * @param columnNameValue Value to use as column name in the report
     * @param columnNameKey Key to identify the column
     * @param type Cell type 
     * @param headerStyle Style to apply on the header of this column
     * @param dataStyle Style to apply on the data of this column
     * @param totalStyle Style to apply on the total cell of this column
     */
    public Column(String columnNameValue, String columnNameKey, CellType type,
                  Style headerStyle, Style dataStyle, Style totalStyle, Integer columnWidth) {
        super();
        this.columnNameValue = columnNameValue;
        this.columnNameKey = columnNameKey;
        this.type = type;
        this.headerStyle = headerStyle;
        this.dataStyle = dataStyle;
        this.totalStyle = totalStyle;
        this.columnWidth = columnWidth;
    }

    // Getters
    public String getColumnNameValue() {
        return columnNameValue;
    }

    public String getColumnNameKey() {
        return columnNameKey;
    }

    public CellType getType() {
        return type;
    }

    public Style getHeaderStyle() {
        return headerStyle;
    }

    public Style getDataStyle() {
        return dataStyle;
    }

    public Style getTotalStyle() {
        return totalStyle;
    }

    public Integer getColumnWidth() {
        return columnWidth;
    }
    
}
