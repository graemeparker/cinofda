package com.adfonic.presentation.publication.model;

public class PublicationHistoryModel {
    private String eventTime;
    private String loggedBy;
    private String assignedTo;
    private String status;
    private String adOpsStatus;
    private String comment;

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public String getLoggedBy() {
        return loggedBy;
    }

    public void setLoggedBy(String loggedBy) {
        this.loggedBy = loggedBy;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAdOpsStatus() {
        return adOpsStatus;
    }

    public void setAdOpsStatus(String adOpsStatus) {
        this.adOpsStatus = adOpsStatus;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

}
