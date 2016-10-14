package com.adfonic.adserver.controller.rtb;

import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adfonic.adserver.BackupLogger;
import com.adfonic.adserver.TargetingEventListener;
import com.adfonic.adserver.controller.fish.RtbFisherman;
import com.adfonic.adserver.offence.OffenceRegistry;
import com.adfonic.adserver.rtb.NoBidException;
import com.adfonic.adserver.rtb.NoBidReason;
import com.adfonic.adserver.rtb.RtbBidEventListener;
import com.adfonic.adserver.rtb.RtbBidLogic;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.rtb.nativ.ByydResponse;
import com.adfonic.adserver.rtb.util.AdServerStats;
import com.adfonic.adserver.rtb.util.AsCounter;
import com.adfonic.adserver.rtb.util.RtbStats;
import com.adfonic.util.stats.FreqLogr;

/**
 * 
 * @author mvanek
 *
 */
public class RtbBidSequence<I, O> implements RtbHttpExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RtbBidSequence.class.getName());

    private final RtbEndpoint endpoint;
    private final ExchangeBidAdapter<I, O> adapter;
    private final RtbBidLogic rtbLogic;
    private final OffenceRegistry offenceRegistry;
    private final RtbFisherman fishnet;
    private final BackupLogger backupLogger;
    private final RtbBidEventListener bidEventListener;
    private final AdServerStats statsManager;

    public RtbBidSequence(RtbEndpoint endpoint, ExchangeBidAdapter<I, O> adapter, RtbBidLogic rtbLogic, BackupLogger backupLogger, RtbBidEventListener bidEventListener,
            OffenceRegistry offenceRegistry, RtbFisherman fishnet, AdServerStats statsManager) {
        Objects.requireNonNull(endpoint);
        this.endpoint = endpoint;
        Objects.requireNonNull(adapter);
        this.adapter = adapter;
        Objects.requireNonNull(rtbLogic);
        this.rtbLogic = rtbLogic;
        Objects.requireNonNull(backupLogger);
        this.backupLogger = backupLogger;
        Objects.requireNonNull(bidEventListener);
        this.bidEventListener = bidEventListener;
        Objects.requireNonNull(offenceRegistry);
        this.offenceRegistry = offenceRegistry;
        Objects.requireNonNull(fishnet);
        this.fishnet = fishnet;
        Objects.requireNonNull(statsManager);
        this.statsManager = statsManager;
    }

    @Override
    public RtbExecutionContext<I, O> execute(RtbHttpContext http) {
        return execute(http, this.bidEventListener, RtbStats.i().getTargetingListener());
    }

    @Override
    public RtbExecutionContext<I, O> execute(RtbHttpContext httpContext, RtbBidEventListener bidEventListener, TargetingEventListener bidTargetListener) {
        HttpServletResponse httpResponse = httpContext.getHttpResponse();
        httpResponse.setHeader("Expires", "0");
        httpResponse.setHeader("Pragma", "No-Cache");
        httpResponse.setContentType(endpoint.getProtocol().getResponseMediaType().toString());

        RtbExecutionContext<I, O> execution = new RtbExecutionContext<I, O>(httpContext, true);
        HttpServletRequest httpRequest = httpContext.getHttpRequest();
        httpRequest.setAttribute(RtbExecutionContext.RTB_CONTEXT, execution);

        statsManager.increment(httpContext.getPublisherExternalId(), AsCounter.BidRequest);

        try {
            backupLogger.startControllerRequest();
            I rtbRequest;
            try {
                rtbRequest = adapter.read(httpRequest, execution);
                execution.setRtbRequest(rtbRequest);
            } finally {
                execution.setRtbRequestParsedAt(System.currentTimeMillis());
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Rtb request read/parsed " + (execution.getRtbRequestParsedAt() - execution.getExecutionStartedAt()));
            }

            ByydRequest byydRequest;
            try {
                byydRequest = adapter.mapRequest(rtbRequest, execution);
                execution.setByydRequest(byydRequest);
            } finally {
                execution.setByydRequestMappedAt(System.currentTimeMillis());
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Byyd request mapped " + (execution.getByydRequestMappedAt() - execution.getRtbRequestParsedAt()));
            }

            ByydResponse byydResponse;
            try {
                byydResponse = rtbLogic.bid(execution, bidEventListener, bidTargetListener);
                execution.setByydResponse(byydResponse);
            } finally {
                execution.setByydResponseCreatedAt(System.currentTimeMillis());
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Byyd response created " + (execution.getByydResponseCreatedAt() - execution.getByydRequestMappedAt()));
            }

            O rtbResponse;
            try {
                rtbResponse = adapter.mapResponse(byydResponse, execution);
                execution.setRtbResponse(rtbResponse);
            } finally {
                execution.setRtbResponseMappedAt(System.currentTimeMillis());
            }
            try {
                adapter.write(rtbResponse, httpResponse.getOutputStream(), execution);
            } finally {
                execution.setRtbResponseWrittenAt(System.currentTimeMillis());
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Rtb response written " + (execution.getRtbResponseWrittenAt() - execution.getByydResponseCreatedAt()));
            }
            statsManager.increment(httpContext.getPublisherExternalId(), AsCounter.BidResponse);
            return execution;
        } catch (NoBidException nbx) {
            execution.setException(nbx);
            try {
                adapter.onNoBidException(nbx, httpRequest, httpResponse, execution);
            } catch (Exception x) {
                FreqLogr.report(x, "Failed to return nobid response for " + nbx);
            }
            try {
                onNoBidException(nbx, execution);
            } catch (Exception x) {
                FreqLogr.report(x, "Failed to record nobid offence for " + nbx);
            }
            return execution;
        } catch (Exception bidx) {
            execution.setException(bidx);
            try {
                adapter.onBiddingException(bidx, httpRequest, httpResponse, execution);
            } catch (Exception x) {
                FreqLogr.report(x, "Failed to return bid exception response for " + bidx);
            }

            try {
                onBidException(bidx, execution);
            } catch (Exception x) {
                FreqLogr.report(x, "Failed to record bid exception offence for " + bidx);
            }
            return execution;
        } finally {
            execution.setExcutionCompletedAt(System.currentTimeMillis());
            try {
                if (fishnet != null) {
                    fishnet.process(execution);
                }
            } catch (Exception x) {
                FreqLogr.report(x, "Failed to fish execution");
            }
        }
    }

    public void onNoBidException(NoBidException nbx, RtbExecutionContext<I, O> executionContext) {
        NoBidReason reason = nbx.getNoBidReason();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("NoBidReason reason: " + String.valueOf(reason) + "." + nbx.getOffenceName(), nbx);
        }
        statsManager.increment(executionContext.getPublisherExternalId(), String.valueOf(reason) + "." + nbx.getOffenceName());

        if (reason == NoBidReason.REQUEST_INVALID || reason == NoBidReason.TECHNICAL_ERROR) {
            offenceRegistry.record(nbx, executionContext);
        }

    }

    public void onBidException(Exception x, RtbExecutionContext<I, O> executionContext) {
        String publisherExternalId = executionContext.getPublisherExternalId();
        FreqLogr.report(x, "Bid failed for publisher " + publisherExternalId);

        statsManager.increment(publisherExternalId, AsCounter.BidError);
        statsManager.increment(publisherExternalId, String.valueOf(x.getClass().getName()));

        offenceRegistry.record(x, executionContext);
    }

}
