package com.adfonic.adserver.controller.fish;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;

import com.adfonic.adserver.controller.fish.RtbFishnet.MatchContext;
import com.adfonic.adserver.controller.rtb.RtbExecutionContext;
import com.adfonic.adserver.rtb.NoBidException;
import com.adfonic.adserver.rtb.NoBidReason;
import com.adfonic.adserver.rtb.nativ.ByydBid;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.adserver.rtb.nativ.ByydResponse;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;

public class RtbFishnetConfig {

    public static enum RtbOutcome {
        BID_MADE, NOBID, DROPPED, INVALID, FAILURE;
    }

    public static enum CatchType {
        TRAFFIC, //capture bid request and response  
        COUNTS; //count stats for adspace
    }

    private final int catchLimit;

    private final int timeLimit;

    private final String exchangeExternalId;

    private final Long adSpaceId;

    private final Long publicationId;

    private final RtbOutcome rtbOutcome;

    private final Long creativeId;

    private final Long campaignId;

    private final CatchType catchType;
    /*
        private final Long formatId;

        private final Long integrationId;

        private final Long extendedCreativeId;
    */
    private final RtbRequestMatcher[] requestMatchers;

    private final RtbResponseMatcher[] responseMatchers;

    public RtbFishnetConfig(CatchType captureType, int catchLimit, int timeLimit, String exchangeExternalId, RtbRequestMatcher[] requestMatchers, Long adSpaceId,
            Long publicationId, RtbOutcome rtbOutcome, Long creativeId, Long campaignId, RtbResponseMatcher[] responseMatchers) {
        this.catchType = captureType;
        this.catchLimit = catchLimit;
        this.timeLimit = timeLimit;
        this.exchangeExternalId = exchangeExternalId;
        this.requestMatchers = requestMatchers;
        this.adSpaceId = adSpaceId;
        this.publicationId = publicationId;
        this.rtbOutcome = rtbOutcome;
        this.creativeId = creativeId;
        this.campaignId = campaignId;
        this.responseMatchers = responseMatchers;
    }

    public boolean match(RtbExecutionContext<?, ?> execution) {

        if (exchangeExternalId != null && !exchangeExternalId.equals(execution.getPublisherExternalId())) {
            //TODO it would be nice to have counts of filtered out requests for every condition...
            return false;
        }

        MatchContext matchContext = null;
        if (requestMatchers != null) {
            for (RtbRequestMatcher matcher : requestMatchers) {
                if (!matcher.match(execution, matchContext)) {
                    return false;
                }
            }
        }

        if (adSpaceId != null) {
            ByydRequest byydRequest = execution.getByydRequest();
            if (byydRequest == null) {
                return false;
            } else {
                AdSpaceDto adSpace = byydRequest.getAdSpace();
                if (adSpace == null || !adSpace.getId().equals(adSpaceId)) {
                    return false;
                }
            }
        }

        if (publicationId != null) {
            ByydRequest byydRequest = execution.getByydRequest();
            if (byydRequest == null) {
                return false;
            } else {
                AdSpaceDto adSpace = byydRequest.getAdSpace();
                if (adSpace == null || !adSpace.getPublication().getId().equals(publicationId)) {
                    return false;
                }
            }
        }

        ByydResponse byydResponse = execution.getByydResponse();
        boolean byydNotMade = byydResponse == null;

        // Everything from now needs response

        if (creativeId != null) {
            if (byydNotMade) {
                return false;
            }
            ByydBid bid = byydResponse.getBid();
            if (!bid.getCreative().getId().equals(creativeId)) {
                return false;
            }
        }

        if (campaignId != null) {
            if (byydNotMade) {
                return false;
            }
            ByydBid bid = byydResponse.getBid();
            if (!bid.getCreative().getCampaign().getId().equals(campaignId)) {
                return false;
            }
        }

        if (responseMatchers != null) {
            if (byydNotMade) {
                return false;
            }
            for (RtbResponseMatcher matcher : responseMatchers) {
                if (!matcher.match(execution, matchContext)) {
                    return false;
                }
            }
        }

        if (rtbOutcome == null) {
            return true; // No specific RtbOutcome expected/required
        } else {
            if (rtbOutcome == RtbOutcome.BID_MADE && !byydNotMade) {
                return true;
            } else if (rtbOutcome == RtbOutcome.NOBID && byydNotMade) {
                return true;
            } else {
                NoBidReason noBidReason = null;
                Exception exception = execution.getException();
                if (exception != null) {
                    if (exception instanceof NoBidException) {
                        noBidReason = ((NoBidException) exception).getNoBidReason();
                    } else {
                        noBidReason = NoBidReason.TECHNICAL_ERROR; //Any other exception is counted as TECHNICAL_ERROR
                    }
                }
                if (rtbOutcome == RtbOutcome.FAILURE) {
                    return noBidReason == NoBidReason.TECHNICAL_ERROR;
                } else if (rtbOutcome == RtbOutcome.INVALID) {
                    return noBidReason == NoBidReason.REQUEST_INVALID || noBidReason == NoBidReason.KNOWN_IGNORED;
                } else if (rtbOutcome == RtbOutcome.DROPPED) {
                    return noBidReason == NoBidReason.REQUEST_DROPPED;
                } else {
                    return false; // Unsupported RtbOutcome type
                }
            }
        }

    }

    public int getCatchLimit() {
        return catchLimit;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public String getExchangeExternalId() {
        return exchangeExternalId;
    }

    public Long getAdSpaceId() {
        return adSpaceId;
    }

    public RtbOutcome getRtbOutcome() {
        return rtbOutcome;
    }

    public Long getCreativeId() {
        return creativeId;
    }

    public CatchType getCatchType() {
        return catchType;
    }

    public static class Builder {

        private CatchType catchType = CatchType.TRAFFIC;

        private int catchLimit = 1; //

        private int timeLimit = 60; //seconds

        private String exchangeExternalId;

        private List<RtbRequestMatcher> rtbRequestMatchers = new ArrayList<RtbRequestMatcher>();

        private List<RtbResponseMatcher> rtbResponseMatchers = new ArrayList<RtbResponseMatcher>();

        private Long adSpaceId;

        private Long publicationId;

        private RtbOutcome rtbOutcome;

        private Long creativeId;

        private Long campaignId;

        public Builder setCatchType(CatchType catchType) {
            this.catchType = catchType;
            return this;
        }

        public Builder setCatchLimit(int catchLimit) {
            if (catchLimit < 1) {
                throw new IllegalArgumentException("Catch limit must be > 0");
            }
            this.catchLimit = catchLimit;
            return this;
        }

        public Builder setTimeLimit(int timeLimit, TimeUnit unit) {
            if (timeLimit < 1) {
                throw new IllegalArgumentException("Time limit must be > 0");
            }
            this.timeLimit = (int) unit.toSeconds(timeLimit);
            return this;
        }

        public Builder setExchangeExternalId(String exchangeExternalId) {
            this.exchangeExternalId = exchangeExternalId;
            return this;
        }

        public Builder setAdSpaceId(Long adSpaceId) {
            this.adSpaceId = adSpaceId;
            return this;
        }

        public Builder setPublicationId(Long publicationId) {
            this.publicationId = publicationId;
            return this;
        }

        public Builder setRtbOutcome(RtbOutcome rtbOutcome) {
            this.rtbOutcome = rtbOutcome;
            return this;
        }

        public Builder setCreativeId(Long creativeId) {
            this.creativeId = creativeId;
            return this;
        }

        public Builder setCampaignId(Long campaignId) {
            this.campaignId = campaignId;
            return this;
        }

        public Builder addRtbRequestContains(String string, boolean positive) {
            this.rtbRequestMatchers.add(new ContainsRtbRequestStringMatcher(string, positive));
            return this;
        }

        public Builder addRtbResponseContains(String string, boolean positive) {
            this.rtbResponseMatchers.add(new ContainsRtbResponseStringMatcher(string, positive));
            return this;
        }

        public Builder addRtbRequestRegex(String expression, boolean positive) {
            this.rtbRequestMatchers.add(new RegexRtbRequestStringMatcher(expression, positive));
            return this;
        }

        public Builder addRtbResponseRegex(String expression, boolean positive) {
            this.rtbResponseMatchers.add(new RegexRtbResponseStringMatcher(expression, positive));
            return this;
        }

        public RtbFishnetConfig build() {
            if (StringUtils.isBlank(exchangeExternalId)) {
                throw new IllegalStateException("Exchange ExternalID is mandatory");
            }
            RtbRequestMatcher[] reqMatchers = null;
            if (!rtbRequestMatchers.isEmpty()) {
                reqMatchers = rtbRequestMatchers.toArray(new RtbRequestMatcher[rtbRequestMatchers.size()]);
            }

            RtbResponseMatcher[] resMatchers = null;
            if (!rtbResponseMatchers.isEmpty()) {
                resMatchers = rtbResponseMatchers.toArray(new RtbResponseMatcher[rtbResponseMatchers.size()]);
            }

            return new RtbFishnetConfig(catchType, catchLimit, timeLimit, exchangeExternalId, reqMatchers, adSpaceId, publicationId, rtbOutcome, creativeId, campaignId,
                    resMatchers);
        }

    }

}
