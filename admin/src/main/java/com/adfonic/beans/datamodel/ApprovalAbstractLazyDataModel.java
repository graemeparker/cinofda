package com.adfonic.beans.datamodel;

public abstract class ApprovalAbstractLazyDataModel<T, E> extends AbstractLazyDataModel<T> {

    private static final long serialVersionUID = 1L;
    
    private final E manager;
    
    protected ApprovalAbstractLazyDataModel(E manager) {
        this.manager = manager;
    }

    protected final E getManager() {
        return manager;
    }
}
