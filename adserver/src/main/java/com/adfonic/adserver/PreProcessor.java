package com.adfonic.adserver;

public interface PreProcessor {
    /**
     * Pre-process a request, applying all configured rules to the IP address
     * and User-Agent.
     * @param targetingContext the active TargetingContext
     * @throws BlacklistedException if the IP address or User-Agent is blacklisted
     */
    void preProcessRequest(TargetingContext targetingContext) throws BlacklistedException;

    /**
     * Apply User-Agent replacements as they match the given User-Agent
     * @param userAgent the supplied User-Agent header
     * @return the modified User-Agent with replacements applied
     */
    String getModifiedUserAgent(String userAgent);

    /**
     * Check to see if a given User-Agent is blacklisted.  This method will
     * throw an exception if the User-Agent is blacklisted, otherwise it will
     * return silently.
     * @throws BlacklistedException if the User-Agent is blacklisted
     */
    void checkUserAgentAgainstBlacklist(String userAgent) throws BlacklistedException;
}
