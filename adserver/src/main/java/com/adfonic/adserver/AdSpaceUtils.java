package com.adfonic.adserver;

/**
 * Encapsulated AdSpace utility logic
 */
public interface AdSpaceUtils {
    /**
     * Queue a previously DORMANT AdSpace for reactivation.  This method will
     * typically be called when an AdSpace is attempted to be used, but that
     * AdSpace is not in cache and has been marked DORMANT.  This method queues
     * a JMS message indicating that the AdSpace should be reactivated.
     * @param adSpaceExternalId the externalId of the DORMANT AdSpace being reactivated
     */
    void reactivateDormantAdSpace(String adSpaceExternalId);
}
