package com.adfonic.presentation;

public class ColumnModel {
    private String key;
    protected boolean sortable;

    public ColumnModel(String key) {
        super();
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public boolean isSortable() {
        return sortable;
    }

}
