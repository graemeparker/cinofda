package com.byyd.breaker;

/**
 * 
 * @author martin.vanek
 *
 */
public interface ManagedBreaker extends CircuitBreaker {

    /**
     * 
     */
    public boolean isBroken();

    /**
     * reset into initial state
     */
    public void reset();

    /**
     * inactive = allow everything (ignore breaking logic)
     */
    public void setInactive(boolean inactive);

    public boolean isInactive();

    /**
     * blockade = allow nothing (ignore breaking logic) and allways fallback
     */
    public void setBlockade(boolean blockade);

    public boolean isBlockade();
}
