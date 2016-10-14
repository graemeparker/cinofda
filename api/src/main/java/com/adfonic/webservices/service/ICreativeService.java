package com.adfonic.webservices.service;

import java.util.Set;

import com.adfonic.domain.Asset;
import com.adfonic.domain.ContentSpec;
import com.adfonic.domain.ContentType;
import com.adfonic.domain.Creative;
import com.adfonic.domain.DestinationType;
import com.adfonic.domain.User;

public interface ICreativeService extends IOwnedEntityService<Creative> {

    public Creative createMinimalCreative(User user, String campaignID, String name, String format);

    public Creative setDestination(Creative creative, DestinationType type, String destination);

    public Creative copyCustomAttributes(Creative creative, String englishTranslation, Set<String> categories);

    public void submit(Creative creative);

    public void notifyUpdate(Creative creative);

    public void delete(Creative creative);

    public Asset getNewAsset(Creative creative, ContentSpec contentSpec, ContentType contentType);

    public void start(Creative creative);

    public void pause(Creative creative);

    public void stop(Creative creative);

}
