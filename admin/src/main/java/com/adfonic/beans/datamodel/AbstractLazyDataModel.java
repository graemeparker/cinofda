package com.adfonic.beans.datamodel;

import java.util.Map;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

public abstract class AbstractLazyDataModel<T> extends LazyDataModel<T> {
	
	private static final long serialVersionUID = 1L;

    private int totalRowCount;
    private Map<String,String> currentFilters;

    @Override
    public int getRowCount() {
        return totalRowCount;
    }

    @Override
    public void setRowCount(int rowCount) {
        this.totalRowCount = rowCount;
        super.setRowCount(rowCount);
    }
    
    /**
     * Overriding this method because a known bug:
     * https://code.google.com/p/primefaces/issues/detail?id=1544
     */
    @Override
    public void setRowIndex(int rowIndex) {
        /*
         * The following is in ancestor (LazyDataModel):
         * this.rowIndex = rowIndex == -1 ? rowIndex : (rowIndex % pageSize);
         */
        if (rowIndex == -1 || getPageSize() == 0) {
            super.setRowIndex(-1);
        }
        else {
            super.setRowIndex(rowIndex % getPageSize());
        }      
    }

    protected Map<String,String> getCurrentFilters() {
        return currentFilters;
    }
    protected void setCurrentFilters(Map<String,String> currentFilters) {
        this.currentFilters = currentFilters;
    }

    protected static String getSortOrderString(SortOrder sortOrder) {
    	if(sortOrder == null || sortOrder == SortOrder.ASCENDING) {
    		return "ASC";
    	} else if(sortOrder == SortOrder.DESCENDING) {
    		return "DESC";
    	} else {
    		return null;
    	}
    }
}
