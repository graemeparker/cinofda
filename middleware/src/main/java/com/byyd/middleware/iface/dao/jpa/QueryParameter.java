package com.byyd.middleware.iface.dao.jpa;


public class QueryParameter {

    public enum TemporalType {
        NONE, 
        DATE, 
        TIME, 
        TIMESTAMP
    }

    private final Object value; // this class should be a generic type
    private final TemporalType type; // optional

    /**
     * @param value
     * @param type
     */
    public QueryParameter(final Object value, final TemporalType type) {
        super();

        this.value = value;
        this.type = type;
    }

    /**
     * @param value
     */
    public QueryParameter(final Object value) {
        super();

        this.value = value;
        this.type = TemporalType.NONE;
    }

    /**
     * @return
     */
    public TemporalType getType() {
        return type;
    }

    /**
     * @return
     */
    public Object getValue() {
        return value;
    }
}
