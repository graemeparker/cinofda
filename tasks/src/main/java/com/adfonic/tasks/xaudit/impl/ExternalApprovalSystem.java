package com.adfonic.tasks.xaudit.impl;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.adfonic.tasks.xaudit.ExternalApprovalService;

public abstract class ExternalApprovalSystem implements ExternalApprovalService {

    private static final ConcurrentMap<Long, ExternalApprovalService> serviceMap = new ConcurrentHashMap<>();

    protected ExternalApprovalSystem() {
    }
    
    protected ExternalApprovalSystem(Set<Long> publisherIds) {
        for (long publisherId : publisherIds){
            serviceMap.put(publisherId, this);
        }
    }
    public ExternalApprovalService getService(long publisherId) {
        ExternalApprovalService service = serviceMap.get(publisherId);
        return service;
    }

  
}
