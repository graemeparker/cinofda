package com.adfonic.data.cache.ecpm.api;

import com.adfonic.data.cache.AdserverDataCache;
import com.adfonic.data.cache.AdserverDataCacheImpl;
import com.adfonic.data.cache.ecpm.key.CampaignCountryKey;
import com.adfonic.data.cache.ecpm.repository.EcpmRepositoryIncremental;
import com.adfonic.domain.cache.dto.SystemVariable;
import com.adfonic.domain.cache.dto.adserver.EcpmInfo;
import com.adfonic.domain.cache.dto.adserver.ExpectedStatsDto;
import com.adfonic.domain.cache.dto.adserver.PlatformDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.test.AbstractAdfonicTest;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;

import static com.adfonic.data.cache.testUtils.TestStubs.*;
import static java.lang.Math.random;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

public class EcpmDataCacheTest extends AbstractAdfonicTest {

    public static final int CREATIVES_NO = 1000;
    public static final int ADSPACES_NO = 100;

    private AdserverDataCache ecpmDataCache;
    private CreativeDto[] creatives;
    private AdSpaceDto[] adspaces;

    @Before
    public void setUp() throws Exception {
        ecpmDataCache = new AdserverDataCacheImpl(createTestSnapshot());
    }

    private EcpmDataRepository createTestSnapshot() {

        EcpmRepositoryIncremental repository = new EcpmRepositoryIncremental();
        repository.addSystemVariable(createSysVar("network_default_ctr", 0.11));
        repository.addSystemVariable(createSysVar("network_default_cvr", 0.12));
        repository.addSystemVariable(createSysVar("network_max_expected_rgr", 1.1));
        repository.addSystemVariable(createSysVar("network_default_rgr", 0.9));
        repository.addSystemVariable(createSysVar("cpm_ctr_underperformance_threshold", 0.53));

        repository.addSystemVariable(createSysVar("cpc_ctr_underperformance_threshold", 0.96));
        repository.addSystemVariable(createSysVar("default_cpc_ctr_target", 0.12));
        repository.addSystemVariable(createSysVar("default_ctr_target", 0.74));
        repository.addSystemVariable(createSysVar("network_default_cvr_rtb", 0.09));

        repository.addSystemVariable(createSysVar("adfonic_ctr_dsp_buffer", .95));
        repository.addSystemVariable(createSysVar("adfonic_cpx_dsp_buffer", .95));

        assertThat(repository.getSystemVariableByName("network_default_ctr").getDoubleValue(), is(0.11));
        assertThat(repository.getSystemVariableByName("cpm_ctr_underperformance_threshold").getDoubleValue(), is(0.53));
        assertThat(repository.getSystemVariableByName("default_ctr_target").getDoubleValue(), is(0.74));

        return repository;
    }

    @Test
    public void storeVariableNames() throws ClassNotFoundException, IOException {
        assertThat(ecpmDataCache.getSystemVariableByName("network_default_ctr").getDoubleValue(), is(0.11));
        assertThat(ecpmDataCache.getSystemVariableByName("pippo"), is(nullValue()));
        assertThat(ecpmDataCache.getSystemVariableByName("network_default_rgr").getDoubleValue(), is(0.9));

    }


    @Test
    public void shouldCalculateEcpm() throws ClassNotFoundException, IOException {


        for (int t = 0; t < 4; t++) {


            long start = System.nanoTime();

            int times = 10000;
            for (int i = 0; i < times; i++) {

                CreativeDto creativeDto = randomCreative();


                calculateEcpm(randomAdSpace(), creativeDto, createPlatform(),  1);
            }
            long elapsed = System.nanoTime() - start;

            System.out.println("adserverDomainCacheManager calculateEcpm elapsed " + elapsed / (times * 1000.0));
        }

    }


    private EcpmData calculateEcpm(AdSpaceDto adspace, CreativeDto creative, PlatformDto platform, long countryIdForEcpm) {

        EcpmData result = new EcpmData();
//        AdSpaceDto adspace = ecpmDataCache.getAdSpaceById(adspaceIdForEcpm);
        //creative = ecpmDataCache.getCreativeById(creativeIdForEcpm);
//        if (adspace == null) {
//            throw new RuntimeException("Adspace not found for adspace id = [" + adspaceIdForEcpm + "]");
//        }
//        if (creative == null) {
//            throw new RuntimeException("Creative not found for creative id = [" + creativeIdForEcpm + "]");
//        }
//            if (!FacesContext.getCurrentInstance().isValidationFailed()) {
        //Passing platform as null, later we shud show platform on the tool to select it
/////        result.setEcpm(ecpmDataCache.getEcpm(adspace, creative, platform, countryIdForEcpm));

        ecpmDataCache.computeEcpmInfo(adspace, creative, platform, countryIdForEcpm, BigDecimal.ZERO, result.getEcpmInfo());


        result.setCampaignCvr(ecpmDataCache.getCampaignCvr(creative.getId()));
        result.setCreativeCvr(ecpmDataCache.getCreativeCvr(creative.getId()));
        result.setAdspaceCtr(ecpmDataCache.getAdspaceCtr(adspace.getId()));
        ExpectedStatsDto expectedStatsDto = ecpmDataCache.getExpectedStats(adspace.getId(), creative.getId());
        if (expectedStatsDto == null) {
            result.setExpectedStatsRgr(0.0);
            result.setExpectedStatsCvr(0.0);
            result.setExpectedStatsCtr(0.0);
        } else {
            result.setExpectedStatsRgr(expectedStatsDto.getExpectedRgr());
            result.setExpectedStatsCvr(expectedStatsDto.getExpectedCvr());
            result.setExpectedStatsCtr(expectedStatsDto.getExpectedCtr());
        }
        SystemVariable defaultCtrVariable = ecpmDataCache.getSystemVariableByName("network_default_ctr");
        if (defaultCtrVariable == null) {
            result.setDefaultCtr(0.0);
        } else {
            result.setDefaultCtr(defaultCtrVariable.getDoubleValue());
        }
        SystemVariable defaultCvrVariable = ecpmDataCache.getSystemVariableByName("network_default_cvr");
        if (defaultCvrVariable == null) {
            result.setDefaultCvr(0.0);
        } else {
            result.setDefaultCvr(defaultCvrVariable.getDoubleValue());
        }

        SystemVariable networkMaxExpectedRgrVariable = ecpmDataCache.getSystemVariableByName("network_max_expected_rgr");
        if (networkMaxExpectedRgrVariable == null) {
            result.setNetworkMaxExpectedRgr(0.0);
        } else {
            result.setNetworkMaxExpectedRgr(networkMaxExpectedRgrVariable.getDoubleValue());
        }

        result.setCountryWeighting(ecpmDataCache.getCampaignCountryWeight(creative.getCampaign().getId(), countryIdForEcpm));
        result.setBuyerPremium(adspace.getPublication().getPublisher().getBuyerPremium());

        return result;
    }


    private Double getBidMultiplierValue(String variableName) {
        Double returnValue = 1.0;
        SystemVariable systemVariable = ecpmDataCache.getSystemVariableByName(variableName);
        if (systemVariable != null) {
            returnValue = systemVariable.getDoubleValue();
        }
        return returnValue;

    }


    private AdSpaceDto randomAdSpace() {
        return getAllAdSpaces()[randomInteger(ADSPACES_NO)];
    }

    private CreativeDto randomCreative() {
        return getAllCreatives()[randomInteger(CREATIVES_NO)];
    }

    private CreativeDto[] getAllCreatives() {
        if (creatives == null) {
            creatives = new CreativeDto[CREATIVES_NO];

            for (int i = 0; i < creatives.length; i++) {

                creatives[i] = createCreativeWithId(i, createCampaign(createBid(random(), randomBidType(), randomBidModelType(), randomBoolean()), createAdvertiser(createCompany(0.5))));
            }
        }
        return creatives;
    }

    private AdSpaceDto[] getAllAdSpaces() {
        if (adspaces == null) {
            adspaces = new AdSpaceDto[ADSPACES_NO];
            for (int i = 0; i < adspaces.length; i++) {

                adspaces[i] = createAdSpaceWithId(i, createPublication(createPublisher(random(), random(), createRtbConfig(randomAdMode()))));
            }
        }
        return adspaces;
    }


}
