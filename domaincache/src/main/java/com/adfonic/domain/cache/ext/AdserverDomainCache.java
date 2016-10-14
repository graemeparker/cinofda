package com.adfonic.domain.cache.ext;

import java.util.Date;

import com.adfonic.domain.cache.SerializableCache;
import com.adfonic.domain.cache.service.AdSpaceService;
import com.adfonic.domain.cache.service.CategoryService;
import com.adfonic.domain.cache.service.CreativeService;
import com.adfonic.domain.cache.service.CurrencyService;
import com.adfonic.domain.cache.service.MiscCacheService;
import com.adfonic.domain.cache.service.RtbCacheService;
import com.adfonic.domain.cache.service.WeightageServices;

public interface AdserverDomainCache extends AdSpaceService, CategoryService, CreativeService, MiscCacheService, WeightageServices, RtbCacheService, CurrencyService,
        SerializableCache {

    public enum ShardMode {
        include, exclude, all
    }

    public Double getSystemVariableDoubleValue(String variableName, Double defaultValue);

    public Date getPopulationStartedAt();

    public Date getElegibilityStartedAt();

    public Date getPreprocessingStartedAt();

    public Date getPreprocessingFinishedAt();

    public Date getDeserializationStartedAt();

    public Date getPostprocessingStartedAt();

    public Date getPostprocessingFinishedAt();

}
