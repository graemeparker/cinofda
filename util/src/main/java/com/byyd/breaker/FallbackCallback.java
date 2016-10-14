package com.byyd.breaker;

/**
 * 
 * @author martin.vanek
 *
 */
public interface FallbackCallback<R> {

    /**
     * On primary disconnected (tripped)
     */
    public R call();

    /**
     * On primary execution exception
     */
    public R call(Exception exception);

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static FallbackCallback NULL_FALLBACK = new ValueFallbackResource(null);

    /**
     * Simple implementation returning value passed in constructor
     */
    public static class ValueFallbackResource<R> implements FallbackCallback<R> {

        private final R value;

        public ValueFallbackResource(R value) {
            this.value = value;
        }

        @Override
        public R call(Exception x) {
            return value;
        }

        @Override
        public R call() {
            return value;
        }

    }
}
