package com.adfonic.webservices.service;

import java.lang.reflect.Field;

public interface IRestrictor {
    public boolean isRestricted(Field field);
}
