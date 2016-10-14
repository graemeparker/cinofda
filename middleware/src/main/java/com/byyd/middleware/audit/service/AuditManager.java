package com.byyd.middleware.audit.service;

import com.adfonic.domain.Audit;
import com.byyd.middleware.iface.service.BaseManager;

public interface AuditManager extends BaseManager {
    
    //------------------------------------------------------------------------------------------
    // Audit
    //------------------------------------------------------------------------------------------
    Audit newAudit(String className, String query);
}
