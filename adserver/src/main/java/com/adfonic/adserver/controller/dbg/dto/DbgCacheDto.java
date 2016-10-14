package com.adfonic.adserver.controller.dbg.dto;

/**
 * 
 * @author mvanek
 *
 */
public class DbgCacheDto {

    private String cacheDir;

    private DbgAdCacheDto adCache;

    private DbgDomainCacheDto domainCache;

    public String getCacheDir() {
        return cacheDir;
    }

    public void setCacheDir(String cacheDir) {
        this.cacheDir = cacheDir;
    }

    public DbgAdCacheDto getAdCache() {
        return adCache;
    }

    public void setAdCache(DbgAdCacheDto adCache) {
        this.adCache = adCache;
    }

    public DbgDomainCacheDto getDomainCache() {
        return domainCache;
    }

    public void setDomainCache(DbgDomainCacheDto domainCache) {
        this.domainCache = domainCache;
    }

}
