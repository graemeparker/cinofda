package com.adfonic.webservices.util;

public interface Get {
    public <T> T get(Class<T> clazz, String path, String... params) throws Exception;

}
