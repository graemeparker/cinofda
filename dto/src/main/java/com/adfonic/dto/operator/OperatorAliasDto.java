package com.adfonic.dto.operator;

import com.adfonic.dto.BusinessKeyDTO;

public class OperatorAliasDto extends BusinessKeyDTO {

    private static final long serialVersionUID = 1L;

    private OperatorDto operator = new OperatorDto();

    @Override
    public Long getId() {
        return operator.getId();
    }

    @Override
    public int hashCode() {
        return operator.hashCode();
    }

    @Override
    public void setId(Long id) {
        operator.setId(id);
    }

    @Override
    public String toString() {
        return operator.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        boolean result = false;
        if (obj instanceof OperatorAliasDto){
            result = operator.equals(((OperatorAliasDto) obj).operator);
        }
        return result;
    }
}
