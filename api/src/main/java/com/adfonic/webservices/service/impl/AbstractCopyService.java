package com.adfonic.webservices.service.impl;

import java.lang.reflect.Field;

import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import com.adfonic.webservices.service.ICopyService;
import com.adfonic.webservices.service.IRestrictor;

/**
 * Template
 * 
 * @author ac
 * 
 * @param <T>
 * @param <D>
 */
public abstract class AbstractCopyService<T, D> implements ICopyService<T, D>, IRestrictor {

    /**
     * Template Method
     */
    public boolean copyToDomain(T dto, D domain) {

        validate(dto);

        boolean overRidden = nullizeBasedOnState(dto);

        verifyPreCopy(dto, domain);

        copy(dto, domain);

        verifyDomain(domain);

        return (overRidden);
    }

    private void validate(T dto) {
        // TODO - call BVF or so; throw Exception on fail
    }

    private boolean nullizeBasedOnState(T dto) {
        boolean isNullized = false;
        for (Field field : dto.getClass().getDeclaredFields()) {
            if (isRestricted(field)) {
                try {
                    field.setAccessible(true);
                    field.set(dto, null);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                isNullized = true;// if any field had to be nulled
            }
        }
        return (isNullized);
    }

    private void verifyPreCopy(T dto, D domain) {
    }

    @Autowired
    private Mapper dozer;

    private D copy(T dto, D domain) {
        dozer.map(dto, domain);
        return (domain);
    }

    public void verifyDomain(D domain) {
    }

    public T copyFromDomain(D domain, Class<T> clazz) {
        return (dozer.map(domain, clazz));
    }

}
