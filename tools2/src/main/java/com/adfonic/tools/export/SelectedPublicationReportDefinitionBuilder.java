package com.adfonic.tools.export;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import com.adfonic.domain.OptimisationReportFields;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.reporting.builder.AbstractReportDefinitionBuilder;
import com.adfonic.presentation.reporting.model.CellType;
import com.adfonic.presentation.reporting.model.Column;
import com.adfonic.presentation.reporting.model.Total;
import com.adfonic.tools.beans.optimisation.OptimisationFields;

public class SelectedPublicationReportDefinitionBuilder<T> extends AbstractReportDefinitionBuilder<T> {

	private static final Integer COLUMN_KEY_INDEX = 0;
	private static final Integer COLUMN_VALUE_INDEX = 1;
	private static final Integer COLUMN_TYPE_INDEX = 2;

	private Set<OptimisationReportFields> reportFields;
	private boolean isAdmin;

	private static Object[][] columnsInfo = new Object[][] {
			{ OptimisationFields.COLUMN_KEY_PUB_EXT_ID, "page.optimisation.labels.table.header.label.publication_external_id", CellType.STRING },
			{ OptimisationFields.COLUMN_KEY_PUBLICATION_NAME, "page.optimisation.labels.table.header.label.publication", CellType.STRING } };

	public SelectedPublicationReportDefinitionBuilder(TimeZone userTimezone,
			Set<OptimisationReportFields> reportFields, boolean isAdmin) {
		super("Selected Publications", userTimezone);
		this.reportFields = reportFields;
		this.isAdmin = isAdmin;
	}

	@Override
	public List<Column> getColumnList() {
		List<Column> columns = new ArrayList<Column>(columnsInfo.length);

		Column column = null;

		for (int cnt = 0; cnt < columnsInfo.length; cnt++) {
			String columnNameKey = (String) columnsInfo[cnt][COLUMN_KEY_INDEX];
			if (isAdmin || OptimisationFields.isFieldVisible(columnNameKey, reportFields)) {
				column = new Column(
						FacesUtils.getFacesMessageForId((String) columnsInfo[cnt][COLUMN_VALUE_INDEX]).getDetail(),
						columnNameKey, (CellType) columnsInfo[cnt][COLUMN_TYPE_INDEX], null);
				columns.add(column);
			}
		}

		return columns;
	}

	@Override
	public List<Total> getTotals() {
		return null; // No totals are defined within this report
	}
}
