package com.adfonic.util.status;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @author mvanek
 *
 */
public abstract class BaseResourceCheck<ID extends Serializable> implements ResourceCheck<ID> {

    @Override
    public ResourceStatus checkStatus(ResourceId<ID> resource) {
        Date started = new Date();
        String message = null;
        try {
            message = doCheck(resource);
        } catch (Exception x) {
            return new ResourceStatus(resource, started, x);
        }
        return new ResourceStatus(resource, started, message);
    }

    public abstract String doCheck(ResourceId<ID> resource) throws Exception;

}
