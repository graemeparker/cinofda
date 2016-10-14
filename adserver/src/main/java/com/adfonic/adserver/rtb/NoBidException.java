package com.adfonic.adserver.rtb;

import java.util.Objects;

import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.adfonic.domain.UnfilledReason;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;

/**
 * 
 * @author mvanek
 *
 */
public class NoBidException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Default behaviour is to skip stack trace creation.
     * For debuging purposes here goes way to reenable it using System property
     */
    private static final boolean stacktrace = Boolean.valueOf(System.getProperty("NoBidException.stacktrace"));

    private final ByydRequest byydRequest;

    private final NoBidReason noBidReason;

    private final Object offendingValue; //nullable

    private final AdSpaceDto adSpace; //nullable

    /**
     * Use in cases where we don't know AdSpace yet. BidRequest Mappers usually...
     */
    public <E extends Enum<?>> NoBidException(ByydRequest byydRequest, NoBidReason noBidReason, E offence) {
        this(byydRequest, noBidReason, offence, null);
    }

    /**
     * Use in cases where we don't know AdSpace yet. BidRequest Mappers usually...
     */
    public <E extends Enum<?>> NoBidException(ByydRequest byydRequest, NoBidReason noBidReason, E offence, Object offendingValue) {
        super(String.valueOf(offence));
        Objects.requireNonNull(offence);

        Objects.requireNonNull(byydRequest);
        this.byydRequest = byydRequest;

        Objects.requireNonNull(noBidReason);
        this.noBidReason = noBidReason;

        this.offendingValue = offendingValue;
        this.adSpace = null;
    }

    public NoBidException(ByydRequest byydRequest, NoBidReason noBidReason, AdSpaceDto adSpace, String offence) {
        this(byydRequest, noBidReason, adSpace, offence, null);
    }

    public NoBidException(ByydRequest byydRequest, NoBidReason noBidReason, AdSpaceDto adSpace, String offence, Object offendingValue) {
        super(offence);
        Objects.requireNonNull(offence);

        Objects.requireNonNull(byydRequest);
        this.byydRequest = byydRequest;

        Objects.requireNonNull(noBidReason);
        this.noBidReason = noBidReason;

        this.adSpace = adSpace;
        this.offendingValue = offendingValue;
    }

    public static NoBidException build(ByydRequest byydRequest, AdSpaceDto adSpace, UnfilledReason unfilledReason) throws NoBidException {
        if (unfilledReason == null) {
            return new NoBidException(byydRequest, NoBidReason.REQUEST_DROPPED, adSpace, "Unfilled");
        } else if (unfilledReason == UnfilledReason.EXCEPTION || unfilledReason == UnfilledReason.UNKNOWN) {
            return new NoBidException(byydRequest, NoBidReason.TECHNICAL_ERROR, adSpace, String.valueOf(unfilledReason));
        } else if (unfilledReason == UnfilledReason.NO_CREATIVES) {
            return new NoBidException(byydRequest, NoBidReason.NOTHING_TO_BID, adSpace, String.valueOf(unfilledReason));
        } else {
            return new NoBidException(byydRequest, NoBidReason.REQUEST_DROPPED, adSpace, String.valueOf(unfilledReason));
        }

    }

    public NoBidReason getNoBidReason() {
        return noBidReason;
    }

    public ByydRequest getByydRequest() {
        return byydRequest;
    }

    public Object getOffenceValue() {
        return offendingValue;
    }

    public AdSpaceDto getAdSpace() {
        return adSpace;
    }

    public String getOffenceName() {
        return super.getMessage();
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("publisher=").append(byydRequest.getPublisherExternalId());
        sb.append(", reason=").append(noBidReason);
        if (super.getMessage() != null) {
            sb.append(", message='").append(super.getMessage()).append('\'');
        }
        if (offendingValue != null) {
            sb.append(" value='").append(offendingValue).append('\'');
        }
        return sb.toString();
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        if (stacktrace) {
            return super.fillInStackTrace();
        } else {
            return this;
        }
    }

    @Override
    public StackTraceElement[] getStackTrace() {
        return new StackTraceElement[0];
    }
}
