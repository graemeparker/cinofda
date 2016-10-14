package com.adfonic.dto.devicegroup;

import org.jdto.annotation.Source;

import com.adfonic.dto.BusinessKeyDTO;

public class DeviceGroupDto extends BusinessKeyDTO {
    
    private static final long serialVersionUID = 1L;
    
    @Source(value = "systemName")
    private String systemName;
    @Source(value = "constraints")
    private String constraints;

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getConstraints() {
        return constraints;
    }

    public void setConstraints(String constraints) {
        this.constraints = constraints;
    }

}
