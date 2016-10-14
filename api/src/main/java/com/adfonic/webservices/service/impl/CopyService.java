package com.adfonic.webservices.service.impl;

import java.lang.reflect.Field;

public class CopyService<T, D> extends AbstractCopyService<T, D> {

    public boolean isRestricted(Field field) {
        return (false);
    }

}
