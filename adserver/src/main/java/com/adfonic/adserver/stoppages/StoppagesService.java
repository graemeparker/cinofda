package com.adfonic.adserver.stoppages;

import java.io.IOException;
import java.util.Map;

import com.adfonic.adserver.Stoppage;

public interface StoppagesService {
    Map<Long,Stoppage> getAdvertiserStoppages() throws IOException;

    Map<Long,Stoppage> getCampaignStoppages()throws IOException;
}
