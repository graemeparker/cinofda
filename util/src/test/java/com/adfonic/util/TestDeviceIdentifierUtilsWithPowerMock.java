package com.adfonic.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.ignoreStubs;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.deviceinsight.api.DeviceInsight;
import com.deviceinsight.api.LocalTime;
import com.deviceinsight.config.Configuration;
import com.deviceinsight.config.ConfigurationBuilder;
import com.deviceinsight.logging.Logger;
import com.deviceinsight.utils.DeviceInsightUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DeviceInsightUtils.class)
public class TestDeviceIdentifierUtilsWithPowerMock {

    @Test
    @SuppressWarnings("unchecked")
    public void testGetAdtruthId() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        ConfigurationBuilder builder = mock(ConfigurationBuilder.class);
        Logger logger = mock(Logger.class);
        mockStatic(DeviceInsightUtils.class);
        Map<String, String> headerMap = mock(Map.class);
        Map<String, String> metadata = mock(Map.class);
        DeviceInsight di = mock(DeviceInsight.class);
        Configuration config = mock(Configuration.class);
        String adtruthData = "mock_data";
        String browserIp = "127.0.0.1";

        given(DeviceInsightUtils.extractHttpHeaders(any(HttpServletRequest.class))).willReturn(headerMap);
        given(builder.build()).willReturn(config);
        PowerMockito.whenNew(DeviceInsight.class).withArguments(any(Map.class), anyString(), any(LocalTime.class), anyString(), any(Configuration.class)).thenReturn(di);
        String atid = AdtruthUtil.getAtid(request, adtruthData, browserIp, metadata);
        assertEquals("DA39A3EE5E6B4B0D3255BFEF95601890AFD80709", atid);
        verifyStatic();
        ignoreStubs(logger);
    }
}
