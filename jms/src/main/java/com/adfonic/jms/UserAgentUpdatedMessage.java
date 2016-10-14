package com.adfonic.jms;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class UserAgentUpdatedMessage implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    public enum ChangeType { 
        UPDATE, 
        DELETE 
    }

    private ChangeType changeType;
    private long userAgentId;
    private String userAgentHeader;
    private long oldModelId;
    private Long newModelId;

    public ChangeType getChangeType() {
        return changeType;
    }
    public void setChangeType(ChangeType changeType) {
        this.changeType = changeType;
    }

    public long getUserAgentId() {
        return userAgentId;
    }
    public void setUserAgentId(long userAgentId) {
        this.userAgentId = userAgentId;
    }

    public String getUserAgentHeader() {
        return userAgentHeader;
    }
    public void setUserAgentHeader(String userAgentHeader) {
        this.userAgentHeader = userAgentHeader;
    }

    public long getOldModelId() {
        return oldModelId;
    }
    public void setOldModelId(long oldModelId) {
        this.oldModelId = oldModelId;
    }

    public Long getNewModelId() {
        return newModelId;
    }
    public void setNewModelId(Long newModelId) {
        this.newModelId = newModelId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("changeType", changeType)
            .append("userAgentId", userAgentId)
            .append("userAgentHeader", userAgentHeader)
            .append("oldModelId", oldModelId)
            .append("newModelId", newModelId)
            .toString();
    }
}
