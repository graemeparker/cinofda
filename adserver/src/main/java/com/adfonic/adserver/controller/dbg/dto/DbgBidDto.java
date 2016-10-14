package com.adfonic.adserver.controller.dbg.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.adfonic.adserver.CreativeEliminatedReason;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.ProxiedDestination;
import com.adfonic.adserver.SelectedCreative;
import com.adfonic.adserver.controller.dbg.DebugBidContext;
import com.adfonic.adserver.controller.dbg.RtbExchange;
import com.adfonic.adserver.rtb.NoBidReason;
import com.adfonic.adserver.rtb.nativ.ByydBid;
import com.adfonic.adserver.rtb.nativ.ByydImp;
import com.adfonic.adserver.rtb.nativ.ByydResponse;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.AdspaceWeightedCreative;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * 
 * @author mvanek
 *
 */
public class DbgBidDto {

    @JsonProperty
    private RtbExchange exchange;

    @JsonProperty
    private DebugBidContext debugContext;

    @JsonProperty
    private List<String> biddingEvents = new ArrayList<String>();

    @JsonProperty
    private DbgNoBidDto nobidReason;

    @JsonProperty
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
    private Exception exception;

    @JsonProperty
    private DbgImpressionBidDto bidImpression;

    @JsonProperty
    private List<DbgImpressionNoBidDto> noImpressions = new ArrayList<DbgBidDto.DbgImpressionNoBidDto>();

    @JsonProperty
    private String rtbResponse;
    //private com.adfonic.adserver.rtb.open.v1.BidResponse<?> rtbResponse;

    @JsonProperty
    private com.adfonic.adserver.rtb.nativ.ByydResponse byydResponse;

    @JsonProperty
    private com.adfonic.adserver.rtb.nativ.ByydRequest byydRequest;

    //@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    //private com.adfonic.adserver.rtb.open.v2.BidRequest<?> rtbRequest;
    @JsonProperty
    private String rtbRequest;

    @JsonProperty
    private DbgTargetingDto targetingInfo;

    @JsonProperty
    private Map<String, String> targetingContext;

    DbgBidDto() {
        // marshalling
    }

    public DbgBidDto(RtbExchange exchange) {
        Objects.requireNonNull(exchange);
        this.exchange = exchange;
    }

    public RtbExchange getExchange() {
        return exchange;
    }

    public void setExchange(RtbExchange exchange) {
        this.exchange = exchange;
    }

    public DebugBidContext getDebugContext() {
        return debugContext;
    }

    public void setDebugContext(DebugBidContext debugContext) {
        this.debugContext = debugContext;
    }

    public void addBiddingEvent(String message) {
        biddingEvents.add(message);
    }

    public AdSpaceDto getAdSpace() {
        if (byydRequest != null) {
            return byydRequest.getAdSpace();
        } else {
            return null;
        }
    }

    public CreativeDto getCreative() {
        if (byydResponse != null) {
            return byydResponse.getBid().getCreative();
        } else {
            return null;
        }
    }

    public static class DbgNoBidDto {

        private NoBidReason reason;
        private String offenceName;
        private String offensiveValue;

        protected DbgNoBidDto() {
            //json
        }

        public DbgNoBidDto(NoBidReason reason, String offence, String offenciveValue) {
            this.reason = reason;
            this.offenceName = offence;
            this.offensiveValue = offenciveValue;
        }

        public NoBidReason getReason() {
            return reason;
        }

        public String getOffenceName() {
            return offenceName;
        }

        public String getOffensiveValue() {
            return offensiveValue;
        }

    }

    public static class DbgTargetingDto {

        @JsonProperty("eligible")
        private AdspaceWeightedCreative[] eligible;

        @JsonProperty("eliminated")
        private List<DbgCreativeEliminationDto> eliminated = new ArrayList<DbgBidDto.DbgCreativeEliminationDto>();

        @JsonProperty("selected")
        private List<Long> selected = new ArrayList<Long>();

        @JsonProperty("targeted")
        private List<DbgCreativePickedDto> targeted;

        public AdspaceWeightedCreative[] getEligible() {
            return eligible;
        }

        public void setEligible(AdspaceWeightedCreative[] eligible) {
            this.eligible = eligible;
        }

        public List<Long> getSelected() {
            return selected;
        }

        public void addSelected(Long creativeId) {
            selected.add(creativeId);
        }

        public void setSelected(List<Long> selected) {
            this.selected = selected;
        }

        public boolean isSelected(long creativeId) {
            return this.selected != null && this.selected.contains(creativeId);
        }

        public List<DbgCreativeEliminationDto> getEliminated() {
            return eliminated;
        }

        public void setEliminated(List<DbgCreativeEliminationDto> eliminated) {
            this.eliminated = eliminated;
        }

        public void addEliminated(DbgCreativeEliminationDto dbgCreativeEliminationDto) {
            eliminated.add(dbgCreativeEliminationDto);
        }

        public DbgCreativeEliminationDto findEliminated(long creativeId) {
            if (eliminated != null) {
                for (DbgCreativeEliminationDto item : eliminated) {
                    if (item.getCreativeId().longValue() == creativeId) {
                        return item;
                    }
                }
            }
            return null;
        }

        public List<DbgCreativePickedDto> getTargeted() {
            if (targeted == null) {
                targeted = new ArrayList<DbgCreativePickedDto>();
            }
            return targeted;
        }

        public void addTargeted(DbgCreativePickedDto targeted) {
            getTargeted().add(targeted);
        }

        public DbgCreativePickedDto findTargeted(long creativeId) {
            if (targeted != null) {
                for (DbgCreativePickedDto item : targeted) {
                    if (item.getCreativeId() == creativeId) {
                        return item;
                    }
                }
            }
            return null;
        }

    }

    public static class DbgCreativePickedDto {

        private int priority;

        private long creativeId;

        private double ecpmWeight;

        public DbgCreativePickedDto(int priority, long creativeId, double ecpmWeight) {
            this.priority = priority;
            this.creativeId = creativeId;
            this.ecpmWeight = ecpmWeight;
        }

        protected DbgCreativePickedDto() {
            //json
        }

        public int getPriority() {
            return priority;
        }

        public double getEcpmWeight() {
            return ecpmWeight;
        }

        public long getCreativeId() {
            return creativeId;
        }

    }

    public static class DbgCreativeEliminationDto {
        @JsonProperty
        private Long creativeId;
        @JsonProperty
        private CreativeEliminatedReason reason;
        @JsonProperty
        private String message;

        protected DbgCreativeEliminationDto() {
            //jackson
        }

        public DbgCreativeEliminationDto(Long creativeId, CreativeEliminatedReason reason, String message) {
            this.creativeId = creativeId;
            this.reason = reason;
            this.message = message;
        }

        public Long getCreativeId() {
            return creativeId;
        }

        public CreativeEliminatedReason getReason() {
            return reason;
        }

        public String getMessage() {
            return message;
        }

    }

    public List<String> getBiddingEvents() {
        return biddingEvents;
    }

    public void setBiddingEvents(List<String> biddingEvents) {
        this.biddingEvents = biddingEvents;
    }

    public com.adfonic.adserver.rtb.nativ.ByydRequest getByydRequest() {
        return byydRequest;
    }

    public void setByydRequest(com.adfonic.adserver.rtb.nativ.ByydRequest byydRequest) {
        this.byydRequest = byydRequest;
    }

    public Map<String, String> getTargetingContext() {
        return targetingContext;
    }

    public void setTargetingContext(Map<String, String> targetingContext) {
        this.targetingContext = targetingContext;
    }

    public ByydResponse getByydResponse() {
        return byydResponse;
    }

    public void setByydResponse(ByydResponse byydResponse) {
        this.byydResponse = byydResponse;
    }

    public String getRtbResponse() {
        return rtbResponse;
    }

    public void setRtbResponse(String rtbResponse) {
        this.rtbResponse = rtbResponse;
    }

    public String getRtbRequest() {
        return rtbRequest;
    }

    public void setRtbRequest(String rtbRequest) {
        this.rtbRequest = rtbRequest;
    }

    public DbgNoBidDto getNobidReason() {
        return nobidReason;
    }

    public void setNobidReason(DbgNoBidDto nobidReason) {
        this.nobidReason = nobidReason;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public void setTargetingInfo(DbgTargetingDto targeting) {
        this.targetingInfo = targeting;
    }

    public DbgTargetingDto getTargetingInfo() {
        return targetingInfo;
    }

    public void setImpressionBid(ByydBid bid, SelectedCreative selectedCreative, Impression impression) {
        if (this.bidImpression != null) {
            throw new IllegalStateException("Bid Impression is already set: " + bidImpression);
        }
        this.bidImpression = new DbgImpressionBidDto(bid.getImpid(), impression, selectedCreative.getCreative().getId(), selectedCreative.getEcpmWeight(),
                selectedCreative.getProxiedDestination());
    }

    public void addImpressionNoBid(ByydImp imp, String reason) {
        this.noImpressions.add(new DbgImpressionNoBidDto(imp.getImpid(), reason));
    }

    public DbgImpressionBidDto getBidImpression() {
        return bidImpression;
    }

    public List<DbgImpressionNoBidDto> getNoImpressions() {
        return noImpressions;
    }

    public static class DbgImpressionNoBidDto {

        private String impid;
        private String reason;

        public DbgImpressionNoBidDto(String impid, String reason) {
            this.impid = impid;
            this.reason = reason;
        }

        protected DbgImpressionNoBidDto() {
            //json
        }

        public String getImpid() {
            return impid;
        }

        public String getReason() {
            return reason;
        }

    }

    public static class DbgImpressionBidDto {

        private String impid;
        private long creativeId;
        private ProxiedDestination proxiedDestination;
        private double ecpmWeight;

        private Impression impression;

        public DbgImpressionBidDto(String impid, Impression impression, long creativeId, double ecpmWeight, ProxiedDestination proxiedDestination) {
            this.impid = impid;
            this.impression = impression;
            this.creativeId = creativeId;
            this.ecpmWeight = ecpmWeight;
            this.proxiedDestination = proxiedDestination;
        }

        protected DbgImpressionBidDto() {
            //json
        }

        public String getImpid() {
            return impid;
        }

        public long getCreativeId() {
            return creativeId;
        }

        public ProxiedDestination getProxiedDestination() {
            return proxiedDestination;
        }

        public double getEcpmWeight() {
            return ecpmWeight;
        }

        public Impression getImpression() {
            return impression;
        }

    }

}
