package com.adfonic.beans.approval;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.model.SelectItem;

import org.apache.commons.lang.ArrayUtils;
import org.primefaces.model.LazyDataModel;

import com.adfonic.beans.BaseBean;
import com.adfonic.domain.AdfonicUser;
import com.byyd.middleware.iface.dao.Sorting;

public abstract class AbstractApprovalsDashboardBean<T> extends BaseBean {
    private static final transient Logger LOG = Logger.getLogger(AbstractApprovalsDashboardBean.class.getName());

    protected static final SelectItem[] ACCOUNT_TYPE_OPTIONS = createFilterOptions(new String[] { "Key", "Standard" });
    
    private final SelectItem[] assignToOptions;
    private final LazyDataModel<T> lazyModel;
    private T[] selectedRows;
    private AdfonicUser selectedAssignTo;

    {
        assignToOptions = createAssignToOptions();
        lazyModel = createLazyModel();
    }

    protected abstract LazyDataModel<T> createLazyModel();

    protected abstract void assign(T dto, AdfonicUser assignTo);

    public final T[] getSelectedRows() {
        return selectedRows;
    }
    public final void setSelectedRows(T[] selectedRows) {
        this.selectedRows = selectedRows;
    }
    
    public final SelectItem[] getAccountTypeOptions() {
        return ACCOUNT_TYPE_OPTIONS;
    }

    public SelectItem[] getAssignToOptions() {
        return assignToOptions;
    }

    public AdfonicUser getSelectedAssignTo() {
        return selectedAssignTo;
    }
    public void setSelectedAssignTo(AdfonicUser selectedAssignTo) {
        this.selectedAssignTo = selectedAssignTo;
    }
    
    public LazyDataModel<T> getLazyModel() {
        return lazyModel;
    }

    public void doBulkAssign() {
        if (ArrayUtils.isEmpty(selectedRows)) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("No rows selected, not changing any assignments");
            }
            return;
        }

        if (LOG.isLoggable(Level.FINE)) {
            if (selectedAssignTo == null) {
                LOG.fine("Bulk unassigning");
            } else {
                LOG.fine("Bulk assigning to: " + selectedAssignTo.getFullName());
            }
        }
        
        for (T dto : selectedRows) {
            assign(dto, selectedAssignTo);
        }
    }

    protected static SelectItem[] createFilterOptions(Object[] values) {
        SelectItem[] options = new SelectItem[values.length + 1];
        int idx = 0;
        options[idx++] = new SelectItem("", "");
        for (Object value : values) {
            options[idx++] = new SelectItem(value.toString(), value.toString());
        }
        return options;
    }

    SelectItem[] createAssignToOptions() {
        List<SelectItem> selectItems = new ArrayList<>();
        selectItems.add(new SelectItem(null, "-- Unassign"));
        for (AdfonicUser adfonicUser : getUserManager().getAllAdfonicUsers(new Sorting("firstName", "lastName"))) {
            selectItems.add(new SelectItem(adfonicUser, adfonicUser.getFullName()));
        }
        return selectItems.toArray(new SelectItem[0]);
    }
}
