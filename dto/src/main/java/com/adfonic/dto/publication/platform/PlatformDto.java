package com.adfonic.dto.publication.platform;

import org.jdto.annotation.Source;

import com.adfonic.dto.BusinessKeyDTO;

public class PlatformDto extends BusinessKeyDTO {
    
    private static final long serialVersionUID = 1L;

    public static final long ALL = -1L;
    
    @Source(value = "name")
    private String name;
    @Source(value = "systemName")
    private String systemName;
    @Source(value = "description")
    private String description;

    public PlatformDto() {
        super();
        this.id = ALL;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(name);
        return builder.toString();
    }
}
