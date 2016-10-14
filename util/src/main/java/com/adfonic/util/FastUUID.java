package com.adfonic.util;

import java.util.Random;
import java.util.UUID;

/**
 * Produces random UUIDs that are less cryptographically strong than what
 * UUID.randomUUID() produces, but does it in considerably less time. This
 * should be used as an alternative to UUID.randomUUID() when randomness is
 * desired but cryptographic strength is not important.
 * 
 * @author Dan Checkoway
 */
public class FastUUID {
    private static final Random RANDOM;
    
    private FastUUID(){
    }
    
    static {
        Random random;
        try {
            // Seed the randomizer with the current nanoTime + the IP address
            // value
            random = new Random(System.nanoTime() + IpAddressUtils.ipAddressToLong(HostUtils.getHostAddress()));
        } catch (java.net.UnknownHostException e) {
            // If the IP address value can't be determined, just use nanoTime
            random = new Random(System.nanoTime());
        }
        RANDOM = random;
    }

    public static UUID randomUUID() {
        return new UUID(RANDOM.nextLong(), RANDOM.nextLong());
    }
}
