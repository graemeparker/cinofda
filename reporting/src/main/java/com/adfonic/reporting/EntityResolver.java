package com.adfonic.reporting;

public interface EntityResolver<T> {
    T getEntityById(long id);
}