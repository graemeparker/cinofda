package com.adfonic.data.cache.ecpm;

import com.adfonic.data.cache.AdserverDataCache;
import com.adfonic.data.cache.AdserverDataCacheImpl;
import com.adfonic.data.cache.ecpm.api.EcpmDataRepository;
import com.adfonic.data.cache.ecpm.key.AdSpaceCreativeKey;
import com.adfonic.data.cache.ecpm.repository.EcpmRepositoryIncremental;
import com.adfonic.domain.BidType;
import com.adfonic.domain.CampaignBid.BidModelType;
import com.adfonic.domain.RtbConfig;
import com.adfonic.domain.cache.dto.adserver.EcpmInfo;
import com.adfonic.domain.cache.dto.adserver.ExpectedStatsDto;
import com.adfonic.domain.cache.dto.adserver.PlatformDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublisherDto;
import com.adfonic.domain.cache.dto.adserver.adspace.RtbConfigDto;
import com.adfonic.domain.cache.dto.adserver.creative.AdvertiserDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignBidDto;
import com.adfonic.domain.cache.dto.adserver.creative.CampaignDto;
import com.adfonic.domain.cache.dto.adserver.creative.CompanyDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.service.WeightageServices;
import com.adfonic.domain.cache.service.WeightageServicesImpl;
import com.adfonic.test.AbstractAdfonicTest;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.adfonic.data.cache.testUtils.TestStubs.createAdSpace;
import static com.adfonic.data.cache.testUtils.TestStubs.createAdvertiser;
import static com.adfonic.data.cache.testUtils.TestStubs.createBid;
import static com.adfonic.data.cache.testUtils.TestStubs.createCampaign;
import static com.adfonic.data.cache.testUtils.TestStubs.createCompany;
import static com.adfonic.data.cache.testUtils.TestStubs.createCreative;
import static com.adfonic.data.cache.testUtils.TestStubs.createPlatform;
import static com.adfonic.data.cache.testUtils.TestStubs.createPublication;
import static com.adfonic.data.cache.testUtils.TestStubs.createPublisher;
import static com.adfonic.data.cache.testUtils.TestStubs.createRtbConfig;
import static com.adfonic.data.cache.testUtils.TestStubs.createSysVar;
import static com.adfonic.data.cache.testUtils.TestStubs.randomAdMode;
import static com.adfonic.data.cache.testUtils.TestStubs.randomBidType;
import static com.adfonic.data.cache.testUtils.TestStubs.randomBidModelType;
import static com.adfonic.data.cache.testUtils.TestStubs.randomBoolean;
import static java.lang.Math.random;

public class TestEcpmDataCache extends AbstractAdfonicTest {

    public static final int MAX_ADSPACES = 100;
    public static final int MAX_CREATIVE = 100;


    private Logger logger = Logger.getLogger(TestEcpmDataCache.class.getName());


    private WeightageServices weightageServicesDD;
    private AdserverDataCache ecpmDataCache;
    private EcpmDataRepository repositoryEcpmDC;
    private RunnableEcpm taskDD;
    private RunnableEcpm taskEcpm;


    @Before
    public void setUp() throws Exception {
        weightageServicesDD = new WeightageServicesImpl();
        repositoryEcpmDC = new EcpmRepositoryIncremental();
        ecpmDataCache = new AdserverDataCacheImpl(repositoryEcpmDC);

        setUpVariables(weightageServicesDD);
        setUpVariables(repositoryEcpmDC);


        prepareTasks();
        System.gc();

        System.out.flush();

    }


    private void prepareTasks() {
        taskDD = new RunnableEcpm() {
            @Override
            public void getEcpm(EcpmInputData ecpmData, EcpmInfo ecpmInfo) {
                weightageServicesDD.computeEcpmInfo(ecpmData.getAdSpace(), ecpmData.getCreative(), ecpmData.getPlatform(), ecpmData.getCountryId(), ecpmData.getBidFloorPrice(), ecpmInfo);
            }
        };

        taskEcpm = new RunnableEcpm() {
            @Override
            public void getEcpm(EcpmInputData ecpmData, EcpmInfo ecpmInfo) {
                ecpmDataCache.computeEcpmInfo(ecpmData.getAdSpace(), ecpmData.getCreative(), ecpmData.getPlatform(), ecpmData.getCountryId(), ecpmData.getBidFloorPrice(), ecpmInfo);
            }
        };

    }

    private void setUpVariables(WeightageServices weightageServices) {
        weightageServices.addSystemVariable(createSysVar("network_default_ctr", 0.11));
        weightageServices.addSystemVariable(createSysVar("network_default_cvr", 0.12));
        weightageServices.addSystemVariable(createSysVar("network_max_expected_rgr", 1.1));
        weightageServices.addSystemVariable(createSysVar("default_ctr_target", 1.01));
        weightageServices.addSystemVariable(createSysVar("cpm_ctr_underperformance_threshold", 0.99));
        weightageServices.addSystemVariable(createSysVar("cpc_ctr_underperformance_threshold", 0.96));
        weightageServices.addSystemVariable(createSysVar("default_cpc_ctr_target", 0.12));
        weightageServices.addSystemVariable(createSysVar("network_default_cvr_rtb", 0.09));
    }

    private void setUpVariables(EcpmDataRepository weightageServices) {
        weightageServices.addSystemVariable(createSysVar("network_default_ctr", 0.11));
        weightageServices.addSystemVariable(createSysVar("network_default_cvr", 0.12));
        weightageServices.addSystemVariable(createSysVar("network_max_expected_rgr", 1.1));
        weightageServices.addSystemVariable(createSysVar("default_ctr_target", 1.01));
        weightageServices.addSystemVariable(createSysVar("cpm_ctr_underperformance_threshold", 0.99));
        weightageServices.addSystemVariable(createSysVar("cpc_ctr_underperformance_threshold", 0.96));
        weightageServices.addSystemVariable(createSysVar("default_cpc_ctr_target", 0.12));
        weightageServices.addSystemVariable(createSysVar("network_default_cvr_rtb", 0.09));
    }


    @Test
    @Ignore
    public void shouldGetCorrectEcpmCPAWithRtb() throws Exception {


        compareCalculations(0.1, 0.2, 10, 0.1, 2, true, BidType.CPA, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 74.25);
        compareCalculations(0.2, 0.2, 10, 0.1, 2, true, BidType.CPA, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 74.25);
        compareCalculations(0.1, 0.3, 10, 0.1, 2, true, BidType.CPA, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 68.53846153846153);
        compareCalculations(0.1, 0.2, 20, 0.1, 2, true, BidType.CPA, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 148.5);
        compareCalculations(0.1, 0.2, 10, 0.2, 2, true, BidType.CPA, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 66);
        compareCalculations(1.1, 0.2, 10, 0.1, 2, true, BidType.CPA, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 74.25);
        compareCalculations(0.1, 1.2, 10, 0.1, 2, true, BidType.CPA, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 40.5);
        compareCalculations(0.1, 0.2, 1, 0.1, 2, true, BidType.CPA, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 7.425);
        compareCalculations(0.1, 0.2, 10, 1, 2, true, BidType.CPA, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 0);
        compareCalculations(0.55, 0.22, 12, 0.33, 2, true, BidType.CPA, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 65.24262295081965);
        compareCalculations(0.14, 0.52, 16, 0.121, 2, true, BidType.CPA, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 91.60105263157892);
        compareCalculations(0.05, 0.3, 12, 0.25, 2, true, BidType.CPA, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 68.53846153846153);

    }

    @Test
    @Ignore
    public void shouldGetCorrectEcpmCPMWithRtb() throws Exception {


        compareCalculations(0.1, 0.2, 10, 0.1, 2, true, BidType.CPM, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 7.5);
        compareCalculations(0.2, 0.2, 10, 0.1, 2, true, BidType.CPM, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 7.5);
        compareCalculations(0.1, 0.3, 10, 0.1, 2, true, BidType.CPM, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 6.923076923076923);
        compareCalculations(0.1, 0.2, 20, 0.1, 2, true, BidType.CPM, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 15);
        compareCalculations(0.1, 0.2, 10, 0.2, 2, true, BidType.CPM, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 6.666666666666667);
        compareCalculations(1.1, 0.2, 10, 0.1, 2, true, BidType.CPM, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 7.5);
        compareCalculations(0.1, 1.2, 10, 0.1, 2, true, BidType.CPM, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 4.0909090909090906);
        compareCalculations(0.1, 0.2, 1, 0.1, 2, true, BidType.CPM, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 0.75);
        compareCalculations(0.1, 0.2, 10, 1, 2, true, BidType.CPM, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 0);
        compareCalculations(0.55, 0.22, 12, 0.33, 2, true, BidType.CPM, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 6.590163934426229);
        compareCalculations(0.14, 0.52, 16, 0.121, 2, true, BidType.CPM, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 9.25263157894737);
        compareCalculations(0.05, 0.3, 12, 0.25, 2, true, BidType.CPM, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 6.9230769230769225);

    }

    @Test
    @Ignore
    public void shouldGetCorrectEcpmCPCWithRtb() throws Exception {

        compareCalculations(0.1, 0.2, 10, 0.1, 2, true, BidType.CPC, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 825);
        compareCalculations(0.2, 0.2, 10, 0.1, 2, true, BidType.CPC, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 825);
        compareCalculations(0.1, 0.3, 10, 0.1, 2, true, BidType.CPC, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 761.5384615384613);
        compareCalculations(0.1, 0.2, 20, 0.1, 2, true, BidType.CPC, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 1650);
        compareCalculations(0.1, 0.2, 10, 0.2, 2, true, BidType.CPC, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 733.3333333333333);
        compareCalculations(1.1, 0.2, 10, 0.1, 2, true, BidType.CPC, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 825);
        compareCalculations(0.1, 1.2, 10, 0.1, 2, true, BidType.CPC, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 450);
        compareCalculations(0.1, 0.2, 1, 0.1, 2, true, BidType.CPC,  BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 82.5);
        compareCalculations(0.1, 0.2, 10, 1, 2, true, BidType.CPC,   BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 0);
        compareCalculations(0.55, 0.22, 12, 0.33, 2, true, BidType.CPC, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 724.9180327868851);
        compareCalculations(0.14, 0.52, 16, 0.121, 2, true, BidType.CPC, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 1017.7894736842103);
        compareCalculations(0.05, 0.3, 12, 0.25, 2, true, BidType.CPC, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 761.5384615384613);

    }

    private void compareCalculations(double revShare, double buyerPremium, int amount, double discount, int country, boolean rtb, BidType bidType, BidModelType bidModelType, RtbConfig.RtbAdMode adMode, double expected) {
        compareCalculations(revShare, buyerPremium, amount, discount, country, rtb, bidType, bidModelType, adMode, false, expected);
    }

    private void compareCalculations(double revShare, double buyerPremium, int amount, double discount, int country, boolean rtb, BidType bidType, BidModelType bidModelType, RtbConfig.RtbAdMode adMode, boolean emptyPlatform, double expected) {
        EcpmInputData ecpmData = prepareEcpmData(revShare, buyerPremium, amount, discount, country, rtb, bidType, bidModelType, adMode, 0, emptyPlatform);

        EcpmInfo ecpmDC = new EcpmInfo();
        taskEcpm.getEcpm(ecpmData, ecpmDC);

        EcpmInfo ecpmDD = new EcpmInfo();
        taskDD.getEcpm(ecpmData, ecpmDD);
//
//                System.out.println("result     ecpmDD   " + ecpmDD.getBidPrice());
//                System.out.println("result     ecpmDC   " + ecpmDC.getBidPrice());

        Assert.assertEquals(expected, ecpmDC.getBidPrice(), 0.00001);
        Assert.assertEquals(expected, ecpmDD.getBidPrice(), 0.00001);
        Assert.assertEquals(ecpmDD, ecpmDC);
    }


    @Test
    @Ignore
    public void shouldGetCorrectEcpmWithoutRtb() throws Exception {

        compareCalculations(0.1, 0.2, 10, 0.1, 2, false, BidType.CPA, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 11.88);
        compareCalculations(0.2, 0.2, 10, 0.1, 2, false, BidType.CPA, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 23.76);
        compareCalculations(0.1, 0.3, 10, 0.1, 2, false, BidType.CPA, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 11.88);
        compareCalculations(0.1, 0.2, 20, 0.1, 2, false, BidType.CPA, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 23.76);
        compareCalculations(0.1, 0.2, 10, 0.2, 2, false, BidType.CPA, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 10.56);
        compareCalculations(1.1, 0.2, 10, 0.1, 2, false, BidType.CPA, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 130.68);
        compareCalculations(0.1, 1.2, 10, 0.1, 2, false, BidType.CPA, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 11.88);
        compareCalculations(0.1, 0.2, 1, 0.1, 2, false, BidType.CPA, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 1.188);
        compareCalculations(0.1, 0.2, 10, 1, 2, false, BidType.CPA, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 0);
        compareCalculations(0.55, 0.22, 12, 0.33, 2, false, BidType.CPA, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 58.3704);
        compareCalculations(0.14, 0.52, 16, 0.121, 2, false, BidType.CPA, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 25.990272);
        compareCalculations(0.05, 0.3, 12, 0.25, 2, false, BidType.CPA, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 5.94);

        compareCalculations(0.1, 0.2, 10, 0.1, 2, false, BidType.CPM, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 0.9);
        compareCalculations(0.2, 0.2, 10, 0.1, 2, false, BidType.CPM, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 1.8);
        compareCalculations(0.1, 0.3, 10, 0.1, 2, false, BidType.CPM, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 0.9);
        compareCalculations(0.1, 0.2, 20, 0.1, 2, false, BidType.CPM, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 1.8);
        compareCalculations(0.1, 0.2, 10, 0.2, 2, false, BidType.CPM, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 0.8);
        compareCalculations(1.1, 0.2, 10, 0.1, 2, false, BidType.CPM, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 9.9);
        compareCalculations(0.1, 1.2, 10, 0.1, 2, false, BidType.CPM, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 0.9);
        compareCalculations(0.1, 0.2, 1, 0.1, 2, false, BidType.CPM, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 0.09);
        compareCalculations(0.1, 0.2, 10, 1, 2, false, BidType.CPM, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 0);
        compareCalculations(0.55, 0.22, 12, 0.33, 2, false, BidType.CPM, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 4.422);
        compareCalculations(0.14, 0.52, 16, 0.121, 2, false, BidType.CPM, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 1.96896);
        compareCalculations(0.05, 0.3, 12, 0.25, 2, false, BidType.CPM, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, 0.45);

    }


    @Test
    @Ignore
    public void shouldGetCorrectEcpmWithoutRtbAndPlatform() throws Exception {

        compareCalculations(0.1, 0.2, 10, 0.1, 2, false, BidType.CPA, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, true, 11.88);
        compareCalculations(0.2, 0.2, 10, 0.1, 2, false, BidType.CPA, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, true, 23.76);
        compareCalculations(0.1, 0.3, 10, 0.1, 2, false, BidType.CPA, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, true, 11.88);
        compareCalculations(0.1, 0.2, 20, 0.1, 2, false, BidType.CPA, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, true, 23.76);
        compareCalculations(0.1, 0.2, 10, 0.2, 2, false, BidType.CPA, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, true, 10.56);
        compareCalculations(1.1, 0.2, 10, 0.1, 2, false, BidType.CPA, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, true, 130.68);
        compareCalculations(0.1, 1.2, 10, 0.1, 2, false, BidType.CPA, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, true, 11.88);
        compareCalculations(0.1, 0.2, 1, 0.1, 2, false, BidType.CPA, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, true, 1.188);
        compareCalculations(0.1, 0.2, 10, 1, 2, false, BidType.CPA, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, true, 0);
        compareCalculations(0.55, 0.22, 12, 0.33, 2, false, BidType.CPA, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, true, 58.37039999999999);
        compareCalculations(0.14, 0.52, 16, 0.121, 2, false, BidType.CPA, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, true, 25.990272000000004);
        compareCalculations(0.05, 0.3, 12, 0.25, 2, false, BidType.CPA, BidModelType.NORMAL, RtbConfig.RtbAdMode.BID, true, 5.9399999999999995);

    }


    @Test
    @Ignore
    public void shouldGetCorrectEcpmWithRamdomData() throws Exception {
        EcpmInputData ecpmData = randomEcpmData();


        EcpmInfo ecpmDC = new EcpmInfo();
        taskEcpm.getEcpm(ecpmData, ecpmDC);

        EcpmInfo ecpmDD = new EcpmInfo();
        taskDD.getEcpm(ecpmData, ecpmDD);

        Assert.assertEquals(ecpmDD.getBidPrice(), ecpmDC.getBidPrice(), 0.00001);
        Assert.assertEquals(ecpmDD, ecpmDC);
    }


    @Test @Ignore
    public void shouldGetItFast() throws Exception {

        int times = 10;
        EcpmInputData[] datas = new EcpmInputData[10_000];

        for (int i = 0; i < datas.length; i++) {
            datas[i] = randomEcpmData();
        }


        System.out.println("elapsed microsec from Domain Cache " + measureEcpm(times, datas, taskDD) / (datas.length * times * 1_000.0));

        System.out.println("elapsed microsec from Ecpm Data Cache " + measureEcpm(times, datas, taskEcpm) / (datas.length * times * 1_000.0));


        System.out.flush();

    }

    private long measureEcpm(int times, EcpmInputData[] datas, RunnableEcpm task) {
        double total = 0;
        long start = System.nanoTime();

        EcpmInfo ecpmInfo = new EcpmInfo();
        for (int i = 0; i < times; i++) {


            for (EcpmInputData data : datas) {

                task.getEcpm(data, ecpmInfo);
                total += ecpmInfo.getBidPrice();
            }
        }

        long elapsed = System.nanoTime() - start;

        System.out.println("total " + total);
        return elapsed;
    }


    @Ignore
    @Test
    public void shouldWorkMultiThread() throws Exception {

        final int threadNumber = 10;
        final int times = 2;
        final EcpmInputData[] datas = new EcpmInputData[10_000];

        for (int i = 0; i < datas.length; i++) {
            datas[i] = randomEcpmData();
        }


        long elapsed = runInParallel(threadNumber, createRunnableWithTimes(times, datas, taskDD));
        System.out.println("multiThread DomainCache elapsed microsec for call " + (double) elapsed / (datas.length * times * threadNumber));

        long elapsedEcpm = runInParallel(threadNumber, createRunnableWithTimes(times, datas, taskEcpm));
        System.out.println("multiThread EcpmDC elapsed microsec for call " + (double) elapsedEcpm / (datas.length * times * threadNumber));

        logger.info("--------------");
        logger.info("shouldWorkMultiThread");
        ecpmDataCache.logCounts("shouldWorkMultiThread", logger, Level.INFO);


        System.out.flush();
    }


    private Runnable createRunnableWithTimes(final int times, final EcpmInputData[] datas, final RunnableEcpm task) {
        return new Runnable() {
            @Override
            public void run() {
                EcpmInfo ecpmInfo = new EcpmInfo();
                for (int i = 0; i < times; i++) {

                    for (EcpmInputData data : datas) {
                        task.getEcpm(data, ecpmInfo);
                    }
                }
            }
        };
    }


    private long runInParallel(int threadNumber, final Runnable test) {

        Executor taskExecutor = Executors.newFixedThreadPool(threadNumber);

        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch endLatch = new CountDownLatch(threadNumber);
        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    startLatch.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException("start latch interruption", e);
                }


                try {
                    test.run();
                } finally {
                    endLatch.countDown();
                }

            }
        };


        for (int i = 0; i < threadNumber; i++) {

            taskExecutor.execute(task);
        }
        long start = System.nanoTime();

        startLatch.countDown();
        try {
            endLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException("end latch interruption", e);
        }

        long timeElapsed = (System.nanoTime() - start) / 1000;
        return timeElapsed;
    }


    private EcpmInputData randomEcpmData() {
        double revShare = random();
        double buyerPremium = random();
        double amount = randomInteger(10);
        double discount = random();
        long country = randomInteger(20);


        EcpmInputData ecpmData = prepareEcpmData(revShare, buyerPremium, amount, discount, country, randomBoolean(), randomBidType(), randomBidModelType(), randomAdMode(), random(), randomBoolean());

        double campaignCvr = random();
        ExpectedStatsDto expectedStatsDto = new ExpectedStatsDto(random(), random(), random());
        double creativeCvr = random();


        repositoryEcpmDC.addCampaignCvr(ecpmData.getCreative().getCampaign().getId(), campaignCvr);
        repositoryEcpmDC.addExpectedStats(new AdSpaceCreativeKey(ecpmData.getAdSpace().getId(), ecpmData.getCreative().getId()), expectedStatsDto);
        repositoryEcpmDC.addCreativeCvr(ecpmData.getCreative().getId(), creativeCvr);


        weightageServicesDD.addCampaignCvr(ecpmData.getCreative().getCampaign().getId(), campaignCvr);
        weightageServicesDD.addExpectedStats(ecpmData.getAdSpace().getId(), ecpmData.getCreative().getId(), expectedStatsDto);
        weightageServicesDD.addCreativeCvr(ecpmData.getCreative().getId(), creativeCvr);


        return ecpmData;
    }


    private EcpmInputData prepareEcpmData(double revShare, double buyerPremium, double amount, double discount, long country, boolean rtb, BidType bidType, BidModelType bidModelType, RtbConfig.RtbAdMode adMode, double bidFloorPrice, boolean emptyPlatform) {
        RtbConfigDto rtbConfig = rtb ? createRtbConfig(adMode) : null;
        PublisherDto publisher = createPublisher(revShare, buyerPremium, rtbConfig);
        PublicationDto publication = createPublication(publisher);
        AdSpaceDto adSpace = createAdSpace(publication, MAX_ADSPACES);
        CampaignBidDto currentBid = createBid(amount, bidType, bidModelType, false);
        CompanyDto company = createCompany(discount);
        AdvertiserDto advertiser = createAdvertiser(company);
        CampaignDto campaign = createCampaign(currentBid, advertiser);
        CreativeDto creative = createCreative(campaign, MAX_CREATIVE);

        PlatformDto platform = null;
        if (!emptyPlatform) {
            platform = createPlatform();
        }

        return new EcpmInputData(adSpace, creative, platform, country, bidFloorPrice);
    }

    private EcpmInputData prepareEmptyEcpmData(long country) {
        AdSpaceDto adSpace = null;
        CreativeDto creative = null;
        PlatformDto platform = createPlatform();
        return new EcpmInputData(adSpace, creative, platform, country, 0);
    }


}
