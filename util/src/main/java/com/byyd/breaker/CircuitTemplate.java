package com.byyd.breaker;

/**
 * 
 * @author martin.vanek
 *
 */
public abstract class CircuitTemplate<R> {

    private final CircuitBreaker breaker;

    public CircuitTemplate(CircuitBreaker breaker) {
        this.breaker = breaker;
    }

    /**
     * @return primary result 
     */
    protected abstract R primary();

    /**
     * @return fallback result because primary is not available or on primary failure
     */
    protected abstract R fallback(Exception x);

    public R execute() {
        if (breaker.check()) {
            try {
                R result = primary();
                breaker.success();
                return result;
            } catch (Exception x) {
                breaker.error(x);
                return fallback(x);
            }
        } else {
            return fallback(null);
        }

    }

}
