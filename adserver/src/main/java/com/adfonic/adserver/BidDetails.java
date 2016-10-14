package com.adfonic.adserver;

public interface BidDetails {

    String getIpAddress();

    Impression getImpression();

    TargetingContext getBidTimeTargetingContext();
}
