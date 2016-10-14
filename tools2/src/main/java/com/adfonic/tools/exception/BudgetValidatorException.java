package com.adfonic.tools.exception;

import javax.faces.application.FacesMessage;
import javax.faces.validator.ValidatorException;

import com.adfonic.presentation.FacesUtils;

public class BudgetValidatorException extends ValidatorException {
    
    private static final long serialVersionUID = 1L;
    
    private final String componentId;
    private final String detailKey;
    private final String[] params;
    
    public BudgetValidatorException(String componentId, String detailKey, String... params) {
        super(FacesUtils.getFacesMessageById(FacesMessage.SEVERITY_ERROR, null, detailKey, params));
        this.componentId = componentId;
        this.detailKey = detailKey;
        this.params = params;
    }
    
    public String getComponentId() {
        return componentId;
    }

    public String getDetailKey() {
        return detailKey;
    }

    public String[] getParams() {
        return params;
    }
    
}
