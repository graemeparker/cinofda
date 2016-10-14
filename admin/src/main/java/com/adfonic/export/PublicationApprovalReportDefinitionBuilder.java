package com.adfonic.export;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import com.adfonic.beans.approval.publication.PublicationApprovalModelFields;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.reporting.builder.AbstractReportDefinitionBuilder;
import com.adfonic.presentation.reporting.model.CellType;
import com.adfonic.presentation.reporting.model.Column;
import com.adfonic.presentation.reporting.model.Total;

public class PublicationApprovalReportDefinitionBuilder<T> extends AbstractReportDefinitionBuilder<T> {

    private static final Integer COLUMN_KEY_INDEX = 0;
    private static final Integer COLUMN_VALUE_INDEX = 1;
    private static final Integer COLUMN_TYPE_INDEX = 2;

    private static Object[][] columnsInfo = new Object[][] {
        { PublicationApprovalModelFields.COLUMN_KEY_INTERNAL_ID, "page.approval.publication.internalid.label", CellType.STRING },
        { PublicationApprovalModelFields.COLUMN_KEY_PUBLICATION_NAME, "page.approval.publication.name.label", CellType.STRING },
        { PublicationApprovalModelFields.COLUMN_KEY_FRIENDLY_NAME, "page.approval.publication.friendlyname.label", CellType.STRING },
        { PublicationApprovalModelFields.COLUMN_KEY_SUPPLIER_NAME, "page.approval.publication.supplier.name.label", CellType.STRING },
        { PublicationApprovalModelFields.COLUMN_KEY_SUPPLIER_USER_NAME, "page.approval.publication.supplier.username.label", CellType.STRING },
        { PublicationApprovalModelFields.COLUMN_KEY_EXTERNAL_ID, "page.approval.publication.externalid.label", CellType.STRING },
        { PublicationApprovalModelFields.COLUMN_KEY_PUBLICATION_TYPE, "page.approval.publication.type.label", CellType.STRING },
        { PublicationApprovalModelFields.COLUMN_KEY_PUBLICATION_STATUS, "page.approval.publication.status.label", CellType.STRING },
        { PublicationApprovalModelFields.COLUMN_KEY_ASSIGNED_TO, "page.approval.publication.assignedto.label", CellType.STRING },
        { PublicationApprovalModelFields.COLUMN_KEY_ACCOUNT_TYPE, "page.approval.publication.accounttype.label", CellType.STRING },
        { PublicationApprovalModelFields.COLUMN_KEY_RTB_ID, "page.approval.publication.rtbid.label", CellType.STRING },
        { PublicationApprovalModelFields.COLUMN_KEY_SELLER_NETWORK_ID, "page.approval.publication.sellernetworkid.label", CellType.STRING },
        { PublicationApprovalModelFields.COLUMN_KEY_ALGORITHM_STATUS, "page.approval.publication.algorithm.status.label", CellType.STRING },
        { PublicationApprovalModelFields.COLUMN_KEY_DEAD_ZONE_STATUS, "page.approval.publication.deadzone.status.label", CellType.STRING },
    };

    public PublicationApprovalReportDefinitionBuilder(TimeZone userTimezone) {
        super("Publication Approvals", userTimezone);
    }

    @Override
    public List<Column> getColumnList() {
        List<Column> columns = new ArrayList<Column>(columnsInfo.length);

        Column column = null;
        for (int cnt = 0; cnt < columnsInfo.length; cnt++) {
            column = new Column(FacesUtils.getFacesMessageForId((String) columnsInfo[cnt][COLUMN_VALUE_INDEX]).getDetail(),
                    (String) columnsInfo[cnt][COLUMN_KEY_INDEX], (CellType) columnsInfo[cnt][COLUMN_TYPE_INDEX], null);
            columns.add(column);
        }

        return columns;
    }

    @Override
    public List<Total> getTotals() {
        return null; // No totals are defined within this report
    }
}
