package com.adfonic.presentation.datamodels;

import java.util.List;
import java.util.Map;

/**
 * This class is meant to functionally encapsulate a pagination model based on PrimeFaces's LazyDataModel.
 * It should be used as the base class for any paginated data model, be it based on stored procedures
 * or on DB queries. Its use allows pagination to be coded in Presentation2 without a dependency on PrimeFaces.
 * 
 * Note the comment above loadPage(). It is important to remember what convention is followed. Particular data models
 * may need to perform some arithmetic transformations within they implementation of loadPage(). Refer to 
 * OptimisationUserInterfaceLivePublicationLazyDataModel and OptimisationUserInterfaceRemovedPublicationLazyDataModel
 * for an example of this.
 * 
 * DataModels implemented by extending this class will need to be wrapped in an instance of something specific to the 
 * presentation layer used in the application to be usable with the graphical controls of said presentation layer.
 * 
 * Refer to Tools2's AbstractLazyDataModelWrapper for an example. Typically, a single wrapper class should suffice,
 * as long as it is designed to wrap instances of this base class and not classes that extend it.
 * 
 * The Map<String,String> currentFilters mimics PrimeFace's query parameters mechanism. It is not a necessary piece
 * to use, it's there for compatibility when wrapping instances. Other variables of this type might get introduced
 * later for use with other presentation layer libraries.
 * 
 * If support for other presentation layer libraries were to be required, only a wrapper class needs to be written. 
 * As long as the DataModels extend this class, they shouldnt need to be modified in any way.
 * 
 * @author pierre
 *
 * @param <T> the type of object thet will be returned by the DataModel class extending this class
 */
public abstract class AbstractLazyDataModel<T>  {
	
	public enum SortDirection { ASC, DESC };

    private int totalRowCount;
    private Map<String,String> currentFilters;

    public int getTotalRowCount() {
        return totalRowCount;
    }

    public void setTotalRowCount(int totalRowCount) {
        this.totalRowCount = totalRowCount;
    }

    protected Map<String,String> getCurrentFilters() {
        return currentFilters;
    }
    protected void setCurrentFilters(Map<String,String> currentFilters) {
        this.currentFilters = currentFilters;
    }
    
    /**
     * Determines a row's primary key and returns is
     * @param t the target row
     * @return the target row's primary key
     */
    public abstract String getRowKey(T t);
    
    /**
     * Locates a row based on its primary key
     * @param rowKey the primary key
     * @return the corresponding row
     */
    public abstract T getRowData(String rowKey);
    
    /**
     * 
     * @param firstRowIndex: 0-based index of the first row to return
     * @param pageSize: the max number of rows to return
     * @param sortField: the field to sort by
     * @param sortDirection: ASC or DESC
     * @param filters: optional query refinement elements
     * @return
     */
    public abstract List<T> loadPage(
			int firstRowIndex,
			int pageSize, 
			String sortField, 
			SortDirection sortDirection,
			Map<String, String> filters);

}
