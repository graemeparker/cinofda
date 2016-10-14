package com.adfonic.dto.report;

import java.io.Serializable;
import java.util.List;

public class PublicationReportDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    List<PublicationReportRowDTO> rows;

    /**
     * @return the rows
     */
    public List<PublicationReportRowDTO> getRows() {
        return rows;
    }

    /**
     * @param rows
     *            the rows to set
     */
    public void setRows(List<PublicationReportRowDTO> rows) {
        this.rows = rows;
    }

}
