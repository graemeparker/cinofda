package com.adfonic.dto.operator;

import java.util.Set;

import com.adfonic.dto.BusinessKeyDTO;

public class OperatorGroupDto extends BusinessKeyDTO {
    
    private static final long serialVersionUID = 1L;
    
    private String name;
    private Set<OperatorDto> operatorss;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<OperatorDto> getOperatorss() {
        return operatorss;
    }

    public void setOperatorss(Set<OperatorDto> operators) {
        this.operatorss = operators;
    }

}
