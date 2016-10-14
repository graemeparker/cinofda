package com.adfonic.datacollector;


/**
 * Stored in the multimap useragent cache for the datacollector.
 * Stores the userAgentId and lastseen date
 * @author Anuj.Saboo
 *
 */
public class UserAgent {

    private Long userAgentId;
    private int lastSeen;

    public UserAgent(Long userAgentId, int lastSeen) {
        this.userAgentId = userAgentId;
        this.lastSeen = lastSeen;
    }

    public Long getUserAgentId() {
        return userAgentId;
    }

    public void setUserAgentId(Long userAgentId) {
        this.userAgentId = userAgentId;
    }

    public int getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(int lastSeen) {
        this.lastSeen = lastSeen;
    }
}
