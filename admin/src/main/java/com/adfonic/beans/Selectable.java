package com.adfonic.beans;

/**
 * Interface used to place items in summary tables where multiple
 * can be selected via checkboxes in order to perform various actions.
 */
public interface Selectable<T> {
    T getObject();
    boolean isChecked();
    void setChecked(boolean checked);
}
