package com.adfonic.adserver.impl;

import java.math.BigDecimal;

import com.adfonic.adserver.LocalBudgetManager;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;

/**
 * add counters
 * 
 * @author bijanfathi
 */
public class LocalBudgetManagerAllAck implements LocalBudgetManager {

    @Override
    public boolean verifyAndReserveBudget(String ref, CampaignDto campaign, BigDecimal value) {
        return true;
    }

    @Override
    public boolean acquireBudget(String ref, BigDecimal settlementPrice, boolean useReservedValue) {
        return true;
    }

    @Override
    public boolean releaseBudget(String ref) {
        return true;
    }

	@Override
	public boolean isRecentlyUnderfunded(CampaignDto campaign) {
		return false;
	}

	@Override
	public ClickRegisterState registerClick(String reference, CampaignDto campaign) {
		return ClickRegisterState.NORMAL;
	}
}
