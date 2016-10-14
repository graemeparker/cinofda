package com.adfonic.adserver.controller.rtb;

import org.mockito.Mock;

import com.adfonic.adserver.DisplayTypeUtils;
import com.adfonic.adserver.LocalBudgetManager;
import com.adfonic.adserver.StatusChangeManager;
import com.adfonic.adserver.StoppageManager;
import com.adfonic.adserver.impl.AdsquareTargetingChecks;
import com.adfonic.adserver.impl.BasicTargetingEngineImpl;
import com.adfonic.adserver.impl.DataCacheProperties;
import com.adfonic.adserver.impl.DeviceIdentifierTargetingChecks;
import com.adfonic.adserver.impl.DeviceLocationTargetingChecks;
import com.adfonic.adserver.impl.FrequencyCapper;
import com.adfonic.adserver.plugin.PluginFillRateTracker;
import com.adfonic.adserver.plugin.PluginManager;
import com.adfonic.util.stats.CounterManager;

/**
 * 
 * @author mvanek
 *
 */
public class TestCornersOfTargetingEngine {

    @Mock
    private DisplayTypeUtils displayTypeUtils;
    @Mock
    private FrequencyCapper frequencyCapper;
    @Mock
    private PluginFillRateTracker pluginFillRateTracker;
    @Mock
    private PluginManager pluginManager;
    @Mock
    private StatusChangeManager statusChangeManager;
    @Mock
    private StoppageManager stoppageManager;
    @Mock
    private CounterManager counterManager;
    @Mock
    private LocalBudgetManager budgetManager;
    @Mock
    private DataCacheProperties dcProperties;
    @Mock
    DeviceLocationTargetingChecks geoChecks;
    @Mock
    DeviceIdentifierTargetingChecks didChecks;
    @Mock
    AdsquareTargetingChecks adsquareChecks;

    public void test2() {
        new BasicTargetingEngineImpl(displayTypeUtils, pluginManager, stoppageManager, frequencyCapper, pluginFillRateTracker, statusChangeManager, counterManager, budgetManager,
                geoChecks, didChecks, adsquareChecks);
    }
}
