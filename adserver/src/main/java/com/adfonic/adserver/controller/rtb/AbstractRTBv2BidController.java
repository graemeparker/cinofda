package com.adfonic.adserver.controller.rtb;

import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.adfonic.adserver.BackupLogger;
import com.adfonic.adserver.offence.OffenceRegistry;
import com.adfonic.adserver.rtb.BackupLoggingRtbBidEventListener;
import com.adfonic.adserver.rtb.NoBidException;
import com.adfonic.adserver.rtb.NoBidReason;
import com.adfonic.adserver.rtb.RtbBidLogic;
import com.adfonic.adserver.rtb.util.AdServerStats;
import com.adfonic.util.stats.FreqLogr;

public class AbstractRTBv2BidController {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass().getName());

    protected final RtbBidLogic rtbLogic;
    protected final OffenceRegistry offenceRegistry;
    protected final BackupLogger backupLogger;
    protected final BackupLoggingRtbBidEventListener loggingListener;
    protected final AdServerStats stats;

    public AbstractRTBv2BidController(RtbBidLogic rtbLogic, BackupLogger backupLogger, BackupLoggingRtbBidEventListener loggingListener, OffenceRegistry offenceRegistry,
            AdServerStats stats) {

        Objects.requireNonNull(rtbLogic);
        this.rtbLogic = rtbLogic;

        Objects.requireNonNull(backupLogger);
        this.backupLogger = backupLogger;

        Objects.requireNonNull(offenceRegistry);
        this.offenceRegistry = offenceRegistry;

        Objects.requireNonNull(loggingListener);
        this.loggingListener = loggingListener;

        Objects.requireNonNull(stats);
        this.stats = stats;
    }

    @ExceptionHandler(NoBidException.class)
    public void onNoBidException(NoBidException nbx, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.valueOf(nbx));
        }
        NoBidReason reason = nbx.getNoBidReason();
        if (reason == NoBidReason.REQUEST_INVALID || reason == NoBidReason.TECHNICAL_ERROR) {
            RtbExecutionContext<?, ?> executionContext = (RtbExecutionContext<?, ?>) httpRequest.getAttribute(RtbExecutionContext.RTB_CONTEXT);
            if (executionContext != null) {
                offenceRegistry.record(nbx, executionContext);
            }
        }
        httpRequest.removeAttribute(RtbExecutionContext.RTB_CONTEXT);//break cycle

        // And simply return 204 HttpStatus.NO_CONTENT
        httpResponse.setStatus(HttpStatus.NO_CONTENT.value());
    }

    @ExceptionHandler(Exception.class)
    public void onException(Exception x, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        //Original "persecond" logging
        //LoggingUtils.logUnexpectedError(logger, x, null);
        FreqLogr.report(x);

        RtbExecutionContext<?, ?> executionContext = (RtbExecutionContext<?, ?>) httpRequest.getAttribute(RtbExecutionContext.RTB_CONTEXT);
        if (executionContext != null) {
            offenceRegistry.record(x, executionContext);
            httpRequest.removeAttribute(RtbExecutionContext.RTB_CONTEXT);//break cycle
        }

        // And simply return 204 HttpStatus.NO_CONTENT
        httpResponse.setStatus(HttpStatus.NO_CONTENT.value());
    }

}
