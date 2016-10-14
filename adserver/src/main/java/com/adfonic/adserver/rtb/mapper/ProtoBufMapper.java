package com.adfonic.adserver.rtb.mapper;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.adfonic.adserver.AdSrvCounter;
import com.adfonic.adserver.rtb.NoBidException;
import com.adfonic.adserver.rtb.NoBidReason;
import com.adfonic.adserver.rtb.nativ.ByydRequest;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;

public abstract class ProtoBufMapper {

    private final Logger LOG = Logger.getLogger(getClass().getName());

    // Size of PUBLICATION.URL_STRING
    protected static final int MAX_URL_LENGTH = 255;

    protected <E extends Enum<?>> void abort(ByydRequest byydRequest, E reasonMsg, Object value, Level level) throws NoBidException {
        if (level != null && LOG.isLoggable(level)) {
            LOG.log(level, reasonMsg + " " + value);
        }
        throw new NoBidException(byydRequest, NoBidReason.REQUEST_INVALID, reasonMsg, value);
    }

    protected <E extends Enum<?>> void abort(ByydRequest byydRequest, E reasonMsg, Level level) throws NoBidException {
        abort(byydRequest, reasonMsg, null, level);
    }

    /*
            protected <E extends Enum<?>> require(ByydRequest byydRequest, E description, String value) throws NoBidException {
                if (StringUtils.isEmpty(value)) {
                    abort(byydRequest, NoBidReason.REQUEST_INVALID , Missing. "Missing " + description, Level.WARNING);
                }
            }
        */
    /**
     * Protobuf prices are in micros (1000000 times bigger than OpenRtb prices)
     * $1.00 = 1,000,000 micros
     */
    public static BigDecimal fromMicros(long micros) {
        return BigDecimal.valueOf(micros).movePointLeft(6);
    }

    public static long intoMicros(BigDecimal price) {
        return price.movePointRight(6).longValue();
    }

    // defensive for null
    protected static boolean isNotInitialized(Message message) {
        return message == null || !message.isInitialized();
    }

    // defensive for null
    protected static boolean isInitialized(Message message) {
        return message != null && message.isInitialized();
    }

    protected static boolean isEmpty(ByteString message) {
        return message == null || message.isEmpty();
    }

    protected String ipv4BytesToString(ByteString ipBS, ByydRequest byydRequest) throws NoBidException {
        if (isEmpty(ipBS)) {
            return null;
        }
        byte[] ipB = new byte[4];
        ipB[3] = 1; // last octet may not actually have been provided (AdX)
        int sizeIn = ipBS.size();
        if (sizeIn > 4) {
            abort(byydRequest, AdSrvCounter.BAD_FIELD, "Valid IPv4 addresses only. possible v6", Level.FINE);
        }
        if (sizeIn < 3) {
            abort(byydRequest, AdSrvCounter.BAD_FIELD, "Cannot derive a valid IPv4 address using " + Arrays.toString(ipBS.toByteArray()), Level.WARNING);
        }
        ipBS.copyTo(ipB, 0);
        String ip = null;
        try {
            ip = InetAddress.getByAddress(ipB).getHostAddress();
        } catch (UnknownHostException e) {
            abort(byydRequest, AdSrvCounter.BAD_FIELD, "Unknown_host_exception on IP address " + Arrays.toString(ipB), Level.WARNING);
        }
        return ip;
    }
}
