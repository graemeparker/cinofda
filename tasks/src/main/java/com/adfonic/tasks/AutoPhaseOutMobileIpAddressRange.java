package com.adfonic.tasks;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.adfonic.domain.Country;
import com.adfonic.domain.MobileIpAddressRange;
import com.adfonic.domain.MobileIpAddressRange_;
import com.adfonic.domain.Operator;
import com.adfonic.domain.OperatorAlias;
import com.adfonic.quova.QuovaClient;
import com.adfonic.util.IpAddressUtils;
import com.byyd.middleware.common.service.CommonManager;
import com.byyd.middleware.device.service.DeviceManager;
import com.byyd.middleware.iface.dao.SortOrder;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;
import com.quova.data._1.Ipinfo;
import com.quova.data._1.NetworkType;

public class AutoPhaseOutMobileIpAddressRange implements Runnable {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    @Autowired
    private CommonManager commonManager;
    @Autowired
    private DeviceManager deviceManager;
    @Autowired
    private QuovaClient quovaClient;

    @Override
    public void run() {
        LOG.debug("Running");
        for (MobileIpAddressRange mobileIpAddressRange : commonManager.getAllMobileIpAddressRanges(new Sorting(SortOrder.asc(MobileIpAddressRange.class, "id")),
                new FetchStrategyBuilder().addInner(MobileIpAddressRange_.country).build())) {
            try {
                phaseOutMobileIpAddressRangeIfPossible(mobileIpAddressRange);
            } catch (Exception e) {
                LOG.error("While processing MobileIpAddressRange id={} {}", mobileIpAddressRange.getId(), e);
            }
        }
    }

    boolean phaseOutMobileIpAddressRangeIfPossible(MobileIpAddressRange mobileIpAddressRange) throws IOException {
        long startPoint = mobileIpAddressRange.getStartPoint();
        long endPoint = mobileIpAddressRange.getEndPoint();
        String startIp = IpAddressUtils.longToIpAddress(startPoint);
        String endIp = IpAddressUtils.longToIpAddress(endPoint);
        LOG.debug("Processing id={}, {}/{} - ", mobileIpAddressRange.getId(), startIp, endIp, mobileIpAddressRange.getCarrier());
        // Check the start-of-range IP address
        Operator startOperator = deriveOperatorUsingQuova(startIp, mobileIpAddressRange.getCountry());
        if (startOperator == null) {
            LOG.debug("No operator found for startIp {}", startIp);
            // No good, gotta keep this MobileIpAddressRange
            return false;
        }
        LOG.debug("Found operator \"{}\" for {}", startOperator, startIp);

        // Check the end-of-range IP address
        Operator endOperator = deriveOperatorUsingQuova(endIp, mobileIpAddressRange.getCountry());
        if (endOperator == null) {
            LOG.debug("No operator found for endIp {}", endIp);
            // No good, gotta keep this MobileIpAddressRange
            return false;
        }
        LOG.debug("Found operator \"{}\" for {}", endOperator, endIp);
        if (!endOperator.equals(startOperator)) {
            LOG.debug("Different from \"{}\"", startOperator);
            // No good, it needs to be the same Operator throughout the whole range
            return false;
        }

        // Check the middle-of-range IP address if the range is big enough to have a distinct midpoint
        if (startPoint != endPoint) {
            long midPoint = startPoint + Math.round((endPoint - startPoint) / 2.0);
            if (midPoint != startPoint && midPoint != endPoint) {
                String midIp = IpAddressUtils.longToIpAddress(midPoint);
                Operator midOperator = deriveOperatorUsingQuova(midIp, mobileIpAddressRange.getCountry());
                if (midOperator == null) {
                    LOG.debug("No operator found for midIp {}", midIp);
                    // No good, gotta keep this MobileIpAddressRange
                    return false;
                }
                LOG.debug("Found operator \"{}\" for {}", midOperator, midIp);
                if (!midOperator.equals(startOperator)) {
                    LOG.debug("Different from \"{}\"", startOperator);
                    // No good, it needs to be the same Operator throughout the whole range
                    return false;
                }
            }
        }

        // If we made it this far, we successfully derived the same Operator for
        // the start[/mid]/end IP addresses using Quova OperatorAlias,  which means
        // we can get phase out this MobileIpAddressRange.
        LOG.info("Phasing out MobileIpAddressRange id={}, start={}, end={}, country={}", mobileIpAddressRange.getId(), startIp, endIp, mobileIpAddressRange.getCountry()
                .getIsoCode());
        commonManager.delete(mobileIpAddressRange);
        return true;
    }

    Operator deriveOperatorUsingQuova(String ip, Country country) throws IOException {
        Ipinfo ipinfo = quovaClient.getIpinfo(ip);
        if (ipinfo == null) {
            return null; // Quova doesn't recognize this IP address
        }

        // Make sure Quova thinks the IP address is coming from a "mobile gateway"
        NetworkType network = ipinfo.getNetwork();
        if (network == null || StringUtils.isBlank(network.getCarrier()) || !"mobile gateway".equals(network.getIpRoutingType())) {
            return null;
        }

        String quovaOperatorAlias = network.getCarrier();

        // See if we have an OperatorAlias set up for this country/carrier combo already
        return deviceManager.getOperatorForOperatorAliasAndCountry(OperatorAlias.Type.QUOVA, country, quovaOperatorAlias);
    }

    public static void main(String[] args) {
        int exitCode = 0;
        try {
            SpringTaskBase.runBean(AutoPhaseOutMobileIpAddressRange.class, "adfonic-toolsdb-context.xml", "adfonic-quova-context.xml", "adfonic-tasks-context.xml");
        } catch (Throwable e) {
            exitCode = 1;
        } finally {
            Runtime.getRuntime().exit(exitCode);
        }
    }
}
