package com.adfonic.tasks.config.combined;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import com.byyd.middleware.auditlog.listener.AuditLogJpaListener;

/**
 * AuditLog Config
 */
@Configuration
@ImportResource(value = { "classpath*:spring/**/adfonic-middleware-auditlog-entities-configuration.xml" })
public class MiddlewareAuditLogConfig {
    
    @Bean
    AuditLogJpaListener auditLogJpaListener(@Value("${auditlog.log.auditsource}") String auditSource){
        return new AuditLogJpaListener(auditSource);
    }
}
