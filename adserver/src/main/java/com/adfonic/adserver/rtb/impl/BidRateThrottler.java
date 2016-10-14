package com.adfonic.adserver.rtb.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.adfonic.adserver.AdSrvCounter;
import com.adfonic.adserver.controller.rtb.BiddingSwitchController;
import com.adfonic.adserver.rtb.NoBidException;
import com.adfonic.adserver.rtb.NoBidReason;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;

/**
 * 
 * @author mvanek
 *
 */
public class BidRateThrottler {

    public static final int SAMPLE_SPREAD_COUNT = 256;
    private final int[] samplingRateArr = new int[SAMPLE_SPREAD_COUNT + 1];
    private final boolean[] doSample = new boolean[samplingRateArr.length];
    {
        doSample[SAMPLE_SPREAD_COUNT] = true;
        samplingRateArr[SAMPLE_SPREAD_COUNT] = 0;
    }

    private final boolean casEnable;

    public BidRateThrottler(boolean casEnable, int samplingRate) {
        this.casEnable = casEnable;
        setSamplingRate(samplingRate);
    }

    // Counter which need not be atomic var / volatile. We don't care a data race. Only the
    // two border cases need to be strict
    // private int samplingCounter;
    private AtomicInteger loopingCounter = new AtomicInteger();

    // The only two conditions that should be followed strictly are zero and 100
    //@Value("${Rtb.approx.sampling.rate:100}")
    public void setSamplingRate(int samplingRate) {
        List<Integer> idxList = new ArrayList<>(SAMPLE_SPREAD_COUNT);
        for (int i = 0; i < SAMPLE_SPREAD_COUNT; i++) {
            idxList.add(i);
        }
        Collections.shuffle(idxList);

        int noOfSampleUnits = samplingRate * SAMPLE_SPREAD_COUNT / 100;

        for (int i = 0; i < SAMPLE_SPREAD_COUNT; i++) {
            int idxLElm = idxList.get(i);
            doSample[idxLElm] = i < noOfSampleUnits;
            samplingRateArr[i] = idxLElm == 0 ? 1 : idxLElm;
        }
    }

    /**
     * Throws NoBidException when in nobid mode or global bid rate exceeds limit 
     */
    public int throttleRtbRate(ByydRequest byydRequest) throws NoBidException {

        if (!BiddingSwitchController.BIDDING_ENABLED) {
            throw new NoBidException(byydRequest, NoBidReason.REQUEST_DROPPED, AdSrvCounter.NOBID_MODE);
        }

        int loopIdx = getLoopingCount();
        if (!doSample[loopIdx]) {
            throw new NoBidException(byydRequest, NoBidReason.REQUEST_DROPPED, AdSrvCounter.GLOBAL_THROTTLING);
        }
        return loopIdx;
    }

    /**
     * Throws NoBidException when bid rate for publication exceeds limit
     */
    public void throttlePublicationRate(int loopIdx, ByydRequest byydRequest, AdSpaceDto adSpace) throws NoBidException {
        //publication level sampling - AI-222
        int samplingRate = adSpace.getPublication().getSamplingRate();
        if (samplingRate < samplingRateArr[loopIdx]) {
            throw new NoBidException(byydRequest, NoBidReason.REQUEST_DROPPED, AdSrvCounter.PUBLICATION_THROTTLING, samplingRate);
        }
    }

    private int getLoopingCount() {
        //return samplingCounter++ % SAMPLE_SPREAD_COUNT; //mod behaviour java
        while (casEnable) {
            int ret = loopingCounter.get();
            int modNxt = (ret + 1) % SAMPLE_SPREAD_COUNT;
            if (loopingCounter.compareAndSet(ret, modNxt))
                return ret;
        }
        return SAMPLE_SPREAD_COUNT;
    }
}
