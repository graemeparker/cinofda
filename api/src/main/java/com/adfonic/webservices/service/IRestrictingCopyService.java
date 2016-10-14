package com.adfonic.webservices.service;

import com.adfonic.domain.Campaign;
import com.adfonic.domain.Creative;

public interface IRestrictingCopyService<T, D> extends ICopyService<T, D> {

    public IRestrictingCopyService<T, D> restrictOnCampaignStatus(Campaign.Status campaignStatus);

    public IRestrictingCopyService<T, D> restrictOnCreativeStatus(Creative.Status creativeStatus);

}
