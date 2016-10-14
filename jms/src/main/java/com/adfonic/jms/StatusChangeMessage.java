package com.adfonic.jms;

public class StatusChangeMessage implements java.io.Serializable {
    public static final long serialVersionUID = 1L;

    private final String entityType;
    private final long entityId;
    private final String newStatus;

    public StatusChangeMessage(String entityType, long entityId, String newStatus) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.newStatus = newStatus;
    }

    public String getEntityType() {
        return entityType;
    }

    public long getEntityId() {
        return entityId;
    }

    public String getNewStatus() {
        return newStatus;
    }

    @Override
    public String toString() {
        return "StatusChangeMessage[entityType=" + entityType + ",entityId=" + entityId + ",newStatus=" + newStatus + "]";
    }
}
