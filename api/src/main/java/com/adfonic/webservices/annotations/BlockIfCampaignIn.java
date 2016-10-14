package com.adfonic.webservices.annotations;

import static com.adfonic.domain.Campaign.Status.ACTIVE;
import static com.adfonic.domain.Campaign.Status.COMPLETED;
import static com.adfonic.domain.Campaign.Status.NEW;
import static com.adfonic.domain.Campaign.Status.NEW_REVIEW;
import static com.adfonic.domain.Campaign.Status.PAUSED;
import static com.adfonic.domain.Campaign.Status.PENDING;
import static com.adfonic.domain.Campaign.Status.PENDING_PAUSED;
import static com.adfonic.domain.Campaign.Status.STOPPED;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.adfonic.domain.Campaign;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BlockIfCampaignIn {

    public Campaign.Status[] value() default { NEW, NEW_REVIEW, PENDING, PENDING_PAUSED, ACTIVE, PAUSED, COMPLETED, STOPPED };
}
