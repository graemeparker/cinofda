package com.adfonic.beans.approval;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.model.SelectItem;

import org.apache.commons.collections.CollectionUtils;

import com.adfonic.util.LogUtils;

public abstract class AbstractApprovalDashboardMBean<T> extends ApprovalMBean {
    private static final transient Logger LOG = Logger.getLogger(AbstractApprovalDashboardMBean.class.getName());

    /** Store the selected rows from the dash board */
    private List<T> filteredRows;

    protected abstract void assignToUser(T dto, Long assignToUserId);

    /** Assign the chosen user to each selected rows */
    public void bulkAssignToUser() {

        String logTitle = "Publication Bulk User Assign";

        if (CollectionUtils.isEmpty(filteredRows)) {
            LogUtils.logWithTitle(LOG, Level.FINE, logTitle, "No rows selected, not changing any bulk assignments");
            return;
        }

        LogUtils.logWithTitle(LOG, Level.INFO, logTitle, (getBulkAssignedToUserId() > 0) ? "Bulk assigning to: " + getBulkAssignedToUserId() : "Bulk unassigning");

        for (T dto : filteredRows) {
            assignToUser(dto, getBulkAssignedToUserId());
        }
        
        refreshView();
    }

    /** Produce filter options for filter drop down */
    protected static SelectItem[] createFilterOptions(Object[] values) {
        SelectItem[] options = new SelectItem[values.length + 1];
        int idx = 0;
        options[idx++] = new SelectItem("", "");
        for (Object value : values) {
            options[idx++] = new SelectItem(value.toString(), value.toString());
        }
        return options;
    }

    // Getter / setters

    public List<T> getFilteredRows() {
        return filteredRows;
    }

    public void setFilteredRows(List<T> filteredRows) {
        this.filteredRows = filteredRows;
    }
}
