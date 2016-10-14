package com.adfonic.tasks.xaudit.appnxs.dat;

import java.util.Map;

/**
 * AppNexus response object, holds the creative and return status codes.
 * @author graemeparker
 *
 */
public class AppNexusResponse {

    private String status;
    private Integer id;
    private AppNexusCreativeRecord creative;
    private String error;
    private String error_id;
    private String error_code;

    private Map<String, Object> dbg;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public AppNexusCreativeRecord getCreative() {
        return creative;
    }

    public void setCreative(AppNexusCreativeRecord creative) {
        this.creative = creative;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getError_id() {
        return error_id;
    }

    public void setError_id(String error_id) {
        this.error_id = error_id;
    }

    public Map<String, Object> getDbg() {
        return dbg;
    }

    public void setDbg(Map<String, Object> dbg) {
        this.dbg = dbg;
    }

    public String getError_code() {
        return error_code;
    }

    public void setError_code(String error_code) {
        this.error_code = error_code;
    }

    @Override
    public String toString() {
        return "AppNexusResponse {status=" + status + ", id=" + id + ", creative=" + creative + ", error=" + error + ", error_id=" + error_id + ", error_code=" + error_code + "}";
    }

}
