package com.adfonic.webservices.service.impl;

import java.lang.reflect.Field;
import java.util.Arrays;

import com.adfonic.domain.Creative.Status;
import com.adfonic.webservices.annotations.BlockIfCreativeIn;
import com.adfonic.webservices.service.IRestrictor;

public class CreativeStatusRestrictor implements IRestrictor {

    private Status status;

    public CreativeStatusRestrictor(Status status) {
        this.status = status;
    }

    public boolean isRestricted(Field field) {
        BlockIfCreativeIn creative = field.getAnnotation(BlockIfCreativeIn.class);
        return (creative != null ? Arrays.asList(creative.value()).contains(status) : false);
    }

    @Override
    public boolean equals(Object o) {
        try {
            return (status == ((CreativeStatusRestrictor) o).status);
        } catch (RuntimeException e) {
            return (false);
        }
    }

    @Override
    public int hashCode() {
        return (status.ordinal());
    }
}
