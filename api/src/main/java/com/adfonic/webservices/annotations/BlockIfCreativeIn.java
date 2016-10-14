package com.adfonic.webservices.annotations;

import static com.adfonic.domain.Creative.Status.ACTIVE;
import static com.adfonic.domain.Creative.Status.NEW;
import static com.adfonic.domain.Creative.Status.PAUSED;
import static com.adfonic.domain.Creative.Status.PENDING;
import static com.adfonic.domain.Creative.Status.PENDING_PAUSED;
import static com.adfonic.domain.Creative.Status.REJECTED;
import static com.adfonic.domain.Creative.Status.STOPPED;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.adfonic.domain.Creative;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BlockIfCreativeIn {

    public Creative.Status[] value() default { NEW, PENDING, PENDING_PAUSED, REJECTED, ACTIVE, PAUSED, STOPPED };
}
