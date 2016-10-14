package com.adfonic.domainserializer;

import java.util.Date;

public class CacheBuildStats {

    private Date dbSelectionStartedAt;

    private Date eligibilityStartedAt;

    private Date serializationStartedAt;

    private Date distributionStartedAt;

    private Date distributionCompletedAt;

    private Exception exception;

    private String contectStatsString;

    private String stopWatchString;

    private String label;

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public Date getDbSelectionStartedAt() {
        return dbSelectionStartedAt;
    }

    public void setDbSelectionStartedAt(Date dbSelectionStartedAt) {
        this.dbSelectionStartedAt = dbSelectionStartedAt;
    }

    public Date getEligibilityStartedAt() {
        return eligibilityStartedAt;
    }

    public void setEligibilityStartedAt(Date eligibilityStartedAt) {
        this.eligibilityStartedAt = eligibilityStartedAt;
    }

    public Date getSerializationStartedAt() {
        return serializationStartedAt;
    }

    public void setSerializationStartedAt(Date serializationStartedAt) {
        this.serializationStartedAt = serializationStartedAt;
    }

    public Date getDistributionStartedAt() {
        return distributionStartedAt;
    }

    public void setDistributionStartedAt(Date distributionStartedAt) {
        this.distributionStartedAt = distributionStartedAt;
    }

    public Date getDistributionCompletedAt() {
        return distributionCompletedAt;
    }

    public void setDistributionCompletedAt(Date distributionCompletedAt) {
        this.distributionCompletedAt = distributionCompletedAt;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public String getContectStatsString() {
        return contectStatsString;
    }

    public void setContectStatsString(String statsString) {
        this.contectStatsString = statsString;
    }

    public void setStopWatchString(String stopWatchString) {
        this.stopWatchString = stopWatchString;
    }

    public String getStopWatchString() {
        return stopWatchString;
    }

}
