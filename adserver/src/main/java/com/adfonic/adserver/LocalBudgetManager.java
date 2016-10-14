package com.adfonic.adserver;

import java.math.BigDecimal;

import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;

public interface LocalBudgetManager {
	
	enum ClickRegisterState {
		NORMAL,
		DUPLICATE,
		OVER_BUDGET
	}

	boolean verifyAndReserveBudget(String ref, CampaignDto campaign, BigDecimal budget);
	
	boolean isRecentlyUnderfunded(CampaignDto campaign);
	
	boolean acquireBudget(String ref, BigDecimal settlementPrice, boolean useReservedValue);
	
	boolean releaseBudget(String ref);
	
	ClickRegisterState registerClick(String reference, CampaignDto campaign);
}
