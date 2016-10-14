package com.adfonic.adserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ParallelModeBidManager extends AbstractBidManager<ParallelModeBidDetails> {

    @Autowired
    public ParallelModeBidManager(ParallelModeCacheService parallelModeCacheService) {
        super(parallelModeCacheService);
    }
}