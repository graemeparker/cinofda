package com.adfonic.presentation.publication.model;

import com.adfonic.presentation.NameIdModel;

public class PublicationAssignedToUserModel extends NameIdModel {

    private Boolean assignedToCurrent;
    private Boolean assignedToAny;

    public Boolean getAssignedToCurrent() {
        return assignedToCurrent;
    }

    public void setAssignedToCurrent(Boolean assignedToCurrent) {
        this.assignedToCurrent = assignedToCurrent;
    }

    public Boolean getAssignedToAny() {
        return assignedToAny;
    }

    public void setAssignedToAny(Boolean assignedToAny) {
        this.assignedToAny = assignedToAny;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PublicationAssignedToUserModel [assignedToCurrent=").append(assignedToCurrent).append(", assignedToAny" + "=").append(assignedToAny).append("]");
        return builder.append("\n").append(super.toString()).toString();
    }
}
