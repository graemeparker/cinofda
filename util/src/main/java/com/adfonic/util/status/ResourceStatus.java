package com.adfonic.util.status;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @author mvanek
 *
 */
public class ResourceStatus implements Serializable {

    private static final long serialVersionUID = 1L;

    private final ResourceId resource;

    private final Date checkStarted;

    private final Date checkFinished;

    private final String message;

    private final Exception exception;

    public ResourceStatus(ResourceId resource, Date checkStarted) {
        this(resource, checkStarted, new Date(), null, null);
    }

    public ResourceStatus(ResourceId resource, Date checkStarted, String message) {
        this(resource, checkStarted, new Date(), message, null);
    }

    public ResourceStatus(ResourceId resource, Date checkStarted, Exception exception) {
        this(resource, checkStarted, new Date(), null, exception);
    }

    public ResourceStatus(ResourceId resource, Date checkStarted, Date checkFinished, String message, Exception exception) {
        if (resource == null) {
            throw new IllegalArgumentException("Null resource");
        }
        this.resource = resource;
        if (checkStarted == null) {
            throw new IllegalArgumentException("Null checkStarted");
        }
        this.checkStarted = checkStarted;
        if (checkFinished == null) {
            throw new IllegalArgumentException("Null checkFinished");
        }
        this.checkFinished = checkFinished;

        this.message = message; //nullable
        this.exception = exception; //nullable
    }

    public long getCheckMillis() {
        return checkFinished.getTime() - checkStarted.getTime();
    }

    public boolean isFine() {
        return exception == null;
    }

    public ResourceId getResource() {
        return resource;
    }

    public Date getCheckStarted() {
        return checkStarted;
    }

    public Date getCheckFinished() {
        return checkFinished;
    }

    public Exception getException() {
        return exception;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ResourceStatus {" + resource.getId() + ", checkStarted=" + checkStarted + ", checkFinished=" + checkFinished + ", message=" + message + ", exception=" + exception
                + "}";
    }
}
