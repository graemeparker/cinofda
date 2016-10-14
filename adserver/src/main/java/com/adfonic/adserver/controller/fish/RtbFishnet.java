package com.adfonic.adserver.controller.fish;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import com.adfonic.adserver.controller.fish.RtbFishnet.MatchContext;
import com.adfonic.adserver.controller.rtb.RtbExecutionContext;

public class RtbFishnet {
    public static enum QueryTarget {
        RTB_REQUEST_STRING, RTB_REQUEST, BYYD_REQUEST, BYYD_RESPONSE, RTB_RESPONSE_BEAN, RTB_RESPONSE_STRING;
    }

    public static enum QueryType {
        STRING, GREP, JSON_PATH, JX_PATH;
    }

    public static class MatchContext {
        //nothing so far needs to be passed along matching...
    }

}

interface FishMatcher {

    public boolean match(RtbExecutionContext<?, ?> execContext, MatchContext matchContext);

    public AtomicInteger getMatchCount();

    public AtomicInteger getMissCount();

}

abstract class FishMatcherBase implements FishMatcher {

    private final AtomicInteger matchCount = new AtomicInteger();

    private final AtomicInteger missCount = new AtomicInteger();

    @Override
    public AtomicInteger getMatchCount() {
        return matchCount;
    }

    @Override
    public AtomicInteger getMissCount() {
        return missCount;
    }

    @Override
    public boolean match(RtbExecutionContext<?, ?> execContext, MatchContext matchContext) {
        boolean isMatch = doMatch(execContext, matchContext);
        if (isMatch) {
            matchCount.incrementAndGet();
        } else {
            missCount.incrementAndGet();
        }
        return isMatch;
    }

    protected abstract boolean doMatch(RtbExecutionContext<?, ?> execContext, MatchContext matchContext);

}

interface RtbRequestMatcher extends FishMatcher {

}

interface RtbResponseMatcher extends FishMatcher {

}

class ExchangeMatcher extends FishMatcherBase {

    private final String exchangeExternalId;

    public ExchangeMatcher(String exchangeExternalId) {
        Objects.requireNonNull(exchangeExternalId);
        this.exchangeExternalId = exchangeExternalId;
    }

    @Override
    public boolean doMatch(RtbExecutionContext<?, ?> execContext, MatchContext matchContext) {
        return exchangeExternalId.equals(execContext.getPublisherExternalId());
    }

}
