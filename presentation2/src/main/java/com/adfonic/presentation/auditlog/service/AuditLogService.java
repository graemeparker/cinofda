package com.adfonic.presentation.auditlog.service;

import java.util.List;

import com.adfonic.dto.auditlog.AuditLogDto;
import com.adfonic.dto.auditlog.AuditLogSearchDto;

/**
 * Service interface for providing Audit Log details for a specific campaign.
 * 
 * @author Attila
 */
public interface AuditLogService {

	List<AuditLogDto> getAuditLogHistoryForCampaign(AuditLogSearchDto searchDto);
}
