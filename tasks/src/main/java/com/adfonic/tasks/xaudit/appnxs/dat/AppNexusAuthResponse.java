package com.adfonic.tasks.xaudit.appnxs.dat;

public class AppNexusAuthResponse {

    private String status;
    private String token;
    private String error;
    private String error_id;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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

    @Override
    public String toString() {
        return "AppNexusAuthResponse {status=" + status + ", token=" + token + ", error=" + error + ", error_id=" + error_id + "}";
    }

}
