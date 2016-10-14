package com.byyd.breaker;

/**
 * 
 * @author martin.vanek
 *
 */
public class CircuitResult<R> {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static final CircuitResult NULL_RESULT = new CircuitResult(null, false, null);

    private final R value;

    private final boolean primary;

    private final Exception exception;

    public CircuitResult(R value, boolean primary, Exception exception) {
        this.value = value;
        this.primary = primary;
        this.exception = exception;
    }

    /**
     * Value returned from either primary or fallback resource
     */
    public R getValue() {
        return value;
    }

    /**
     * Primary execution success
     */
    public boolean isPrimary() {
        return primary;
    }

    /**
     * Primary execution exception
     */
    public Exception getException() {
        return exception;
    }

}
