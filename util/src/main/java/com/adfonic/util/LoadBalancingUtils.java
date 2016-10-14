package com.adfonic.util;

/**
 * Utility class for dynamic assignment of primary/failover server nodes for
 * load balancing purposes. The assumption is that either host numbers (i.e. 23
 * from "rfadserver23") or IP address octets are used to determine a
 * "client index" (i.e. rfadserver12 is clientIndex=11). This class uses an
 * algorithm that balances both primary and failover scenarios.
 */
public final class LoadBalancingUtils {
    private LoadBalancingUtils() {
    }

    /**
     * Calculate the zero-based index of the primary server. This does a simple
     * modulo calculation.
     * 
     * @param clientIndex
     *            the zero-based index of the respective client
     * @param numServers
     *            the number of servers available
     * @return the zero-based index of the respective server that should be used
     *         as the primary server
     */
    public static int getPrimaryServerIndex(int clientIndex, int numServers) {
        return clientIndex % numServers;
    }

    /**
     * Calculate the zero-based index of the failover server. This uses a
     * slightly more complicated algorithm than getPrimaryServerIndex so that
     * failover scenarios are load-balanced as well as non-failover.
     * 
     * @param clientIndex
     *            the zero-based index of the respective client
     * @param numServers
     *            the number of servers available
     * @return the zero-based index of the respective server that should be used
     *         as the failover server
     */
    public static int getFailoverServerIndex(int clientIndex, int numServers) {
        return (1 + clientIndex + (clientIndex / numServers) + (clientIndex / (numServers * (numServers - 1)))) % numServers;
    }
}
