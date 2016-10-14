package com.adfonic.adserver.controller.rtb;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.adfonic.adserver.BackupLogger;
import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.adserver.BlacklistedException;
import com.adfonic.adserver.InvalidIpAddressException;
import com.adfonic.adserver.controller.fish.RtbFisherman;
import com.adfonic.adserver.offence.OffenceRegistry;
import com.adfonic.adserver.rtb.BackupLoggingRtbBidEventListener;
import com.adfonic.adserver.rtb.RtbBidLogic;
import com.adfonic.adserver.rtb.impl.RtbWinLogicImpl;
import com.adfonic.adserver.rtb.mapper.OpenRTBv1QuickNdirty;
import com.adfonic.adserver.rtb.util.AdServerStats;
import com.adfonic.domain.cache.AdserverDomainCacheManager;
import com.adfonic.util.stats.CounterManager;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class TestRtbController extends BaseAdserverTest {

    private OpenRtbV1Controller rtbController;
    private RtbBidLogic rtbLogic;
    RtbWinLogicImpl rtbWinLogic;
    //private OffenceRegistry offenceRegistry = new OffenceRegistry(10, 10);
    private OpenRTBv1QuickNdirty v1mapper;
    private BackupLoggingRtbBidEventListener backupLoggingListener;
    private BackupLogger backupLogger;

    @Before
    public void initTests() throws IOException {
        rtbLogic = mock(RtbBidLogic.class, "rtbLogic");
        rtbWinLogic = mock(RtbWinLogicImpl.class, "rtbWinLogic");
        backupLoggingListener = mock(BackupLoggingRtbBidEventListener.class, "backupLoggingListener");
        backupLogger = mock(BackupLogger.class, "backupLogger");
        v1mapper = mock(OpenRTBv1QuickNdirty.class, "v1mapper");
        AdserverDomainCacheManager adCacheManager = mock(AdserverDomainCacheManager.class, "adCacheManager");
        AdServerStats stats = new AdServerStats(new CounterManager(), adCacheManager);
        rtbController = new OpenRtbV1Controller(rtbLogic, rtbWinLogic, backupLogger, backupLoggingListener, new OffenceRegistry(10, 10), new RtbFisherman(), stats);
        inject(rtbController, "backupLogger", backupLogger);
    }

    @Test
    public void testRtbController02_handleWinNotice() throws InvalidIpAddressException, BlacklistedException, IOException {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();

        expect(new Expectations() {
            {
                oneOf(rtbWinLogic).winOnRtbNurl(null, null, request);
                will(new Action() {

                    @Override
                    public String[] invoke(Invocation invocation) throws Throwable {
                        //response.getWriter().append("Hello Test XML Response");
                        return new String[] { "Hello Test XML Response", "text/html" };
                    }

                    @Override
                    public void describeTo(Description arg0) {
                    }
                });
            }
        });

        rtbController.handleWinNotice(request, response, null, null);
        String responseString = response.getContentAsString();
        assertTrue(responseString.equals("Hello Test XML Response"));
    }
}
