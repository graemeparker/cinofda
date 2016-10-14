package com.adfonic.tools.util;

import java.util.List;
import java.util.Map;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import com.adfonic.presentation.datamodels.AbstractLazyDataModel;
import com.adfonic.presentation.datamodels.AbstractLazyDataModel.SortDirection;

/**
 * This class is the wrapper to be used to display paginated data of data models
 * extending Presentation2's AbstractLazyDataModel. It will present the data in
 * a manner compatible with the use of PrimeFaces' DataTable.
 *
 * Wrapper instances are constructed around an instance of these data models,
 * instances typically returned by methods of a service class in Presentation2.
 * This wrapper class should make usable with PrimeFaces any data model that
 * extends AbstractLazyDataModel, no matter how its internals work.
 *
 * See OptimisationUserInterfaceMBean.createLivePublicationLazyDataModel() and
 * OptimisationUserInterfaceMBean.createRemovedPublicationLazyDataModel() for a
 * usage example.
 *
 * @author pierre
 *
 * @param <T>
 */
public class AbstractLazyDataModelWrapper<T> extends LazyDataModel<T> {

    private static final long serialVersionUID = 1L;

    protected AbstractLazyDataModel<T> abstractLazyDataModel;

    public AbstractLazyDataModelWrapper(AbstractLazyDataModel<T> abstractLazyDataModel) {
        super();
        this.abstractLazyDataModel = abstractLazyDataModel;
        this.setRowCount(this.abstractLazyDataModel.getTotalRowCount());
    }

    @Override
    public int getRowCount() {
        return abstractLazyDataModel.getTotalRowCount();
    }

    @Override
    public List<T> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters) {
        return abstractLazyDataModel.loadPage(first, pageSize, sortField, getSortDirection(sortOrder), filters);
    }

    @Override
    public T getRowData(String rowKey) {
        return abstractLazyDataModel.getRowData(rowKey);
    }

    @Override
    public Object getRowKey(T object) {
        return abstractLazyDataModel.getRowKey(object);
    }

    /**
     * Converts PrimeFaces' SortOrder into AbstractLazyDataModel.SortDirection
     *
     * @param sortOrder
     * @return
     */
    protected static SortDirection getSortDirection(org.primefaces.model.SortOrder sortOrder) {
        // note that the control has limitations for sortBy and sortOrder so we
        // define the order here
        boolean ascending = false;
        if (sortOrder != null) {
            ascending = sortOrder.equals(SortOrder.DESCENDING) ? true : false;
        }

        if (ascending) {
            return SortDirection.ASC;
        } else {
            return SortDirection.DESC;
        }
    }
}
