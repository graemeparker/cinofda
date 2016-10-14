package com.adfonic.presentation.publication.model;

import com.adfonic.presentation.NameIdModel;

public class PublicationWatcherModel extends NameIdModel {

    private Boolean isWatcher;

    public Boolean getIsWatcher() {
        return isWatcher;
    }

    public void setIsWatcher(Boolean isWatcher) {
        this.isWatcher = isWatcher;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PublicationWatcherModel [isWatcher=").append(isWatcher).append("]");
        return builder.append("\n").append(super.toString()).toString();
    }

}
