package com.adfonic.webservices.service;

public interface ICopyService<T, D> {

    public boolean copyToDomain(T dto, D domain);

    public T copyFromDomain(D domain, Class<T> clazz);

}
