package com.adfonic.tracker;

import java.util.List;

import com.adfonic.adserver.Click;

public interface InstallService {
    /** This method tracks at most one install per click.  It indicates to
        the caller whether or not it was unique.
        @return true if this was the first install for the given click, or
        false if this was a duplicate
    */
    boolean trackInstall(Click click);

    /**
     * Schedule a retry of an install tracking request.  This should be called
     * when the tracker fails to find the respective click.
     */
    void scheduleInstallRetry(String applicationId, long deviceIdentifierTypeId, String deviceIdentifier, boolean claim);

    /**
     * Schedule a retry of an install tracking request.  This should be called
     * when the tracker fails to find the respective click.
     */
    void scheduleRetry(PendingInstall pendingInstall);

    /**
     * Deletes a scheduled retry of an install tracking request from the db.
     * This should be called once the retry was successfully processed.
     */
    void deleteScheduledInstallRetry(PendingInstall pendingInstall);

    /**
     * Get a list of pending installs to retry.  This is intended to be used by
     * the app that will periodically reattempt to track installs that have been
     * scheduled for retry.
     */
    List<PendingInstall> getPendingInstallsToRetry(int maxRows);

    /**
     * Schedule a retry of an authenticated install tracking request.  This
     * should be called when the tracker fails to find the respective click.
     */
    void scheduleAuthenticatedInstallRetry(String clickExternalID);

    /**
     * Schedule a retry of an authenticated install tracking request.  This
     * should be called when the tracker fails to find the respective click.
     */
    void scheduleRetry(PendingAuthenticatedInstall pendingAuthenticatedInstall);

    /**
     * Deletes a scheduled retry of an authenticated install tracking request
     * from the db.  This should be called once the retry was successful.
     */
    void deleteScheduledAuthenticatedInstallRetry(PendingAuthenticatedInstall pendingAuthenticatedInstall);

    /**
     * Get a list of pending authenticated installs to retry.  This is intended
     * to be used by the app that will periodically reattempt to track
     * authenticated installs that have been scheduled for retry.
     */
    List<PendingAuthenticatedInstall> getPendingAuthenticatedInstallsToRetry(int maxRows);
}
