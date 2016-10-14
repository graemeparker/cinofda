package com.adfonic.dto.format;

import org.jdto.annotation.Source;

import com.adfonic.dto.NameIdBusinessDto;

public class FormatDto extends NameIdBusinessDto {

    private static final long serialVersionUID = 1L;

    @Source(value = "systemName")
    private String systemName;

    private Boolean enabled = true;

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("FormatDto [systemName=");
        builder.append(systemName);
        builder.append(", ");
        builder.append(super.toString());
        builder.append("]");
        return builder.toString();
    }

}
