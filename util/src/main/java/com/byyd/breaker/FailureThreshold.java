package com.byyd.breaker;

/**
 * 
 * @author mvanek
 *
 */
public interface FailureThreshold {

    /**
     * Check before performing guarded operation
     */
    public boolean check();

    /**
     * callback when success
     */
    public void success();

    /**
     * callback when error
     */
    public void failure();

    public static class ErrorCountThreshold implements FailureThreshold {

        @Override
        public boolean check() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void success() {
            // TODO Auto-generated method stub

        }

        @Override
        public void failure() {
            // TODO Auto-generated method stub

        }

    }
}
