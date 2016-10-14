package com.adfonic.jms;

public class CreativeApprovalMessage implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private final Long creativeId;
    private final Long publisherId;

    public CreativeApprovalMessage(long creativeId, long publisherId) {
        this.creativeId = creativeId;
        this.publisherId = publisherId;
    }

    public CreativeApprovalMessage(long creativeId) {
        this.creativeId = creativeId;
        this.publisherId = null;
    }

    public long getCreativeId() {
        return creativeId;
    }

    public Long getPublisherId() {
        return publisherId;
    }

    @Override
    public String toString() {
        return "CreativeApprovalMessage[creativeId=" + creativeId + ",publisherId=" + publisherId + "]";
    }

}
