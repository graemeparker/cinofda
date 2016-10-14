package com.byyd.celtra;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.byyd.celtra.CeltraAnalyticsRequest.Spec;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * 
 * @author mvanek
 *
 */
public class CeltraAnalyticsResponse {

    private Date updateTimestamp;

    private List<String> notes;

    private Spec spec;

    private List<Map<?, ?>> rows; // ??????

    public Date getUpdateTimestamp() {
        return updateTimestamp;
    }

    public void setUpdateTimestamp(Date updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
    }

    public List<String> getNotes() {
        return notes;
    }

    public void setNotes(List<String> notes) {
        this.notes = notes;
    }

    public Spec getSpec() {
        return spec;
    }

    public void setSpec(Spec spec) {
        this.spec = spec;
    }

    public List<Map<?, ?>> getRows() {
        return rows;
    }

    public void setRows(List<Map<?, ?>> rows) {
        this.rows = rows;
    }

    @Override
    public String toString() {
        try {
            return CeltraAnalyticsClient.jackson.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return super.toString();
        }
    }

}
