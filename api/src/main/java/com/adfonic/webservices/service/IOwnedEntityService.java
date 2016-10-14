package com.adfonic.webservices.service;

import com.adfonic.domain.HasExternalID;
import com.adfonic.domain.User;

public interface IOwnedEntityService<T extends HasExternalID> {

    public void authorize(User user, T ownedEntity);

    public T findbyExternalID(User user, String externalID);
    
    public T findbyExternalID(String externalID);    

    public void validate(T ownedEntity);

}
