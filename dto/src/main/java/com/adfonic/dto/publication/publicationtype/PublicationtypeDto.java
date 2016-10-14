package com.adfonic.dto.publication.publicationtype;

import org.jdto.annotation.Source;

import com.adfonic.dto.NameIdBusinessDto;

public class PublicationtypeDto extends NameIdBusinessDto {

    private static final long serialVersionUID = 1L;

    @Source(value = "systemName")
    private String systemName;

    @Source(value = "medium")
    private String medium;

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getMedium() {
        return medium;
    }

    public void setMedium(String medium) {
        this.medium = medium;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PublicationtypeDto [systemName=");
        builder.append(systemName);
        builder.append(", medium=");
        builder.append(medium);
        builder.append(", ");
        builder.append(super.toString());
        builder.append("]");
        return builder.toString();
    }

}
