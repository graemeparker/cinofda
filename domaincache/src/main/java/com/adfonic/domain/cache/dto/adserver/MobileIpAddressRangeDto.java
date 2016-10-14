package com.adfonic.domain.cache.dto.adserver;

import com.adfonic.domain.cache.dto.BusinessKeyDto;

public class MobileIpAddressRangeDto extends BusinessKeyDto implements Comparable<MobileIpAddressRangeDto> {
    private static final long serialVersionUID = 3L;

    private Long startPoint;
    private Long endPoint;
    private Long operatorId;
    private int priority;

    public Long getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(Long startPoint) {
        this.startPoint = startPoint;
    }

    public Long getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(Long endPoint) {
        this.endPoint = endPoint;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isInRange(Long value) {
        return startPoint <= value && endPoint >= value;
    }

    @Override
    public int compareTo(MobileIpAddressRangeDto other) {
        // Sort highest priority to lowest priority
        // Sort lowest to highest start point
        // Sort lowest to highest end point
        if (this.getPriority() > other.getPriority()) {
            return -1;
        } else if (this.getPriority() < other.getPriority()) {
            return 1;
        } else if (this.getStartPoint() < other.getStartPoint()) {
            return -1;
        } else if (this.getStartPoint() > other.getStartPoint()) {
            return 1;
        } else if (this.getEndPoint() < other.getEndPoint()) {
            return -1;
        } else if (this.getEndPoint() > other.getEndPoint()) {
            return 1;
        } else {
            // If all of the above are equal, then we must have duplicate
            // data in the table.  That's what happened on test.  Tie goes
            // to whatever.  Same prio, same range, they're the same as
            // far as the sort is concerned.
            return 0;
        }
    }

    @Override
    public String toString() {
        return "MobileIpAddressRangeDto {" + getId() + ", startPoint=" + startPoint + ", endPoint=" + endPoint + ", operatorId=" + operatorId + ", priority=" + priority + "}";
    }

}
