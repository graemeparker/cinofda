package com.adfonic.presentation.reporting.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class that groups the information detail of all columns
 * 
 * Responsibility: To provide information regarding the report columns
 * 
 * @author David Martin
 *
 * @param <T> Row class
 */
public class ColumnsDetails {

    /** Collection with all report columns */
    private List<Column> columnList = null;
    
    /** General style for all reports data columns */
    private Style columnsStyle;
    
    /** Internal map to index columns positions in the report */
    private Map<String, Integer> columnIndex = null;
    
    /** Value Transformer for this data collection*/
    private ValueTransformer valueTransformer;
    
    public ColumnsDetails(List<Column> columnList, Style columnsStyle, ValueTransformer valueTransformer){
        this.valueTransformer = valueTransformer;
        this.columnList = columnList;
        this.columnsStyle = columnsStyle;
        columnIndex = new HashMap<String, Integer>();
        for(int cnt=0;cnt<columnList.size();cnt++){
            columnIndex.put(columnList.get(cnt).getColumnNameKey(), cnt);
        }
    }
    
    /**
     * Gets all columns information
     * 
     * @return List of Column instances
     */
    public List<Column> getColumns(){
        return this.columnList;
    }
    
    /**
     * Get general style for all columns
     * 
     * @return Style
     */
    public Style getColumnsStyle() {
        return columnsStyle;
    }
    
    /**
     * Return the column position in the report
     * 
     * @param columnName Column name
     * 
     * @return Integer which represents the position of the 
     * column in the report
     */
    public Integer getColumnIndex(String columnName){
        return columnIndex.get(columnName);
    }

    /**
     * Return the ValueTransformer implementation for this column details
     * 
     * @return object  which implements ValueTransformer interface
     */
    public ValueTransformer getValueTransformer() {
        return valueTransformer;
    }
}
