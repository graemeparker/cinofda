package com.adfonic.util.status;

import java.io.Serializable;

/**
 * 
 * @author mvanek
 *
 */
public interface ResourceCheck<ID extends Serializable> {

    public ResourceStatus checkStatus(ResourceId<ID> resource);
}
