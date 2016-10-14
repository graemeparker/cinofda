package com.byyd.middleware.iface.dao.jpa;


/**
 *
 */
public class NamedQueryParameter extends QueryParameter {

    private final String parameterName;

    /**
     * @param parameterName
     * @param value
     * @param type
     */
    public NamedQueryParameter(String parameterName, Object value,
            TemporalType type) {
        super(value, type);
        this.parameterName = parameterName;
    }

    /**
     * @param parameterName
     * @param value
     */
    public NamedQueryParameter(String parameterName, Object value) {
        super(value);
        this.parameterName = parameterName;
    }

    /**
     * @return
     */
    public String getParameterName() {
        return parameterName;
    }

}
