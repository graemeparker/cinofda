package com.adfonic.dto.optimisation;

import java.util.Date;

public class OptimisationUserInterfaceRemovedPublicationDto extends OptimisationUserInterfaceLivePublicationDto {

    private String removedType;
    private Date dateRemoved;

    public OptimisationUserInterfaceRemovedPublicationDto() {
        super();
    }

    public void setRemovedType(String removedType) {
        this.removedType = removedType;
    }

    public void setDateRemoved(Date dateRemoved) {
        this.dateRemoved = (dateRemoved == null ? null : new Date(dateRemoved.getTime()));
    }

    public String getRemovedType() {
        return removedType;
    }

    public Date getDateRemoved() {
        return dateRemoved;
    }

}
