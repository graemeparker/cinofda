package com.adfonic.adserver;

public interface BidCacheService<T extends BidDetails> {

    T getBidDetails(String key);

    T getAndRemoveBidDetails(String key);

    void saveBidDetails(String key, T bidDetails);

    boolean removeBidDetails(String key);
}