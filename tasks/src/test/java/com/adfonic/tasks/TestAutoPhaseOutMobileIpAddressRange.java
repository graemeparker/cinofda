package com.adfonic.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.domain.Country;
import com.adfonic.domain.MobileIpAddressRange;
import com.adfonic.domain.Operator;
import com.adfonic.domain.OperatorAlias;
import com.adfonic.quova.QuovaClient;
import com.adfonic.test.AbstractAdfonicTest;
import com.byyd.middleware.common.service.CommonManager;
import com.byyd.middleware.device.service.DeviceManager;
import com.quova.data._1.Ipinfo;
import com.quova.data._1.NetworkType;

public class TestAutoPhaseOutMobileIpAddressRange extends AbstractAdfonicTest {
    private DeviceManager deviceManager;
    private CommonManager commonManager;
    private QuovaClient quovaClient;
    private AutoPhaseOutMobileIpAddressRange autoPhaseOutMobileIpAddressRange;

    @Before
    public void runBeforeEachTest() {
        deviceManager = mock(DeviceManager.class);
        commonManager = mock(CommonManager.class);
        quovaClient = mock(QuovaClient.class);
        autoPhaseOutMobileIpAddressRange = new AutoPhaseOutMobileIpAddressRange();
        inject(autoPhaseOutMobileIpAddressRange, "deviceManager", deviceManager);
        inject(autoPhaseOutMobileIpAddressRange, "commonManager", commonManager);
        inject(autoPhaseOutMobileIpAddressRange, "quovaClient", quovaClient);
    }

    @Test
    public void testDeriveOperatorUsingQuova() throws Exception {
        final String ip = randomAlphaNumericString(10);
        final Country country = mock(Country.class);
        final Ipinfo ipinfo = mock(Ipinfo.class);
        final NetworkType network = mock(NetworkType.class);
        final String networkCarrier = randomAlphaNumericString(10);
        final Operator operator = mock(Operator.class);

        expect(new Expectations() {{
            // Test 1: IP not found
            oneOf (quovaClient).getIpinfo(ip); will(returnValue(null));

            // Test 2: IP found but no network
            allowing (quovaClient).getIpinfo(ip); will(returnValue(ipinfo));
            oneOf (ipinfo).getNetwork(); will(returnValue(null));

            // Test 3: IP found but network.carrier is blank
            allowing (ipinfo).getNetwork(); will(returnValue(network));
            oneOf (network).getCarrier(); will(returnValue(""));

            // Test 4: IP found, network.carrier not blank, but network.ipRoutingType is not "mobile gateway"
            allowing (network).getCarrier(); will(returnValue(networkCarrier));
            oneOf (network).getIpRoutingType(); will(returnValue("blah"));

            // Test 5: all valid
            allowing (network).getIpRoutingType(); will(returnValue("mobile gateway"));
            oneOf (deviceManager).getOperatorForOperatorAliasAndCountry(OperatorAlias.Type.QUOVA, country, networkCarrier); will(returnValue(operator));
        }});
        
        // Test 1: IP not found
        assertNull(autoPhaseOutMobileIpAddressRange.deriveOperatorUsingQuova(ip, country));

        // Test 2: IP found but no network
        assertNull(autoPhaseOutMobileIpAddressRange.deriveOperatorUsingQuova(ip, country));

        // Test 3: IP found but network.carrier is blank
        assertNull(autoPhaseOutMobileIpAddressRange.deriveOperatorUsingQuova(ip, country));

        // Test 4: IP found, network.carrier not blank, but network.ipRoutingType is not "mobile gateway"
        assertNull(autoPhaseOutMobileIpAddressRange.deriveOperatorUsingQuova(ip, country));

        // Test 5: all valid
        assertEquals(operator, autoPhaseOutMobileIpAddressRange.deriveOperatorUsingQuova(ip, country));
    }
    
    @Test
    public void testPhaseOutMobileIpAddressRangeIfPossible() throws Exception {
        final MobileIpAddressRange mobileIpAddressRange = mock(MobileIpAddressRange.class);
        final long startPoint = randomLong();
        final long endPoint1 = startPoint + 101; // room for a midpoint check
        final long endPoint2 = startPoint + 1; // no room for a midpoint check
        final Country country = mock(Country.class);
        final Ipinfo ipinfo = mock(Ipinfo.class);
        final NetworkType network = mock(NetworkType.class);
        final String networkCarrier = randomAlphaNumericString(10);
        final Operator operator1 = mock(Operator.class, "operator1");
        final Operator operator2 = mock(Operator.class, "operator2");

        expect(new Expectations() {{
            allowing (mobileIpAddressRange).getId(); will(returnValue(randomLong()));
            allowing (mobileIpAddressRange).getCarrier(); will(returnValue(randomAlphaNumericString(10)));
            allowing (mobileIpAddressRange).getCountry(); will(returnValue(country));
            allowing (country).getIsoCode(); will(returnValue(randomAlphaString(2)));

            // deriveOperatorUsingQuova expected invocations
            allowing (quovaClient).getIpinfo(with(any(String.class))); will(returnValue(ipinfo));
            allowing (ipinfo).getNetwork(); will(returnValue(network));
            allowing (network).getCarrier(); will(returnValue(networkCarrier));
            allowing (network).getIpRoutingType(); will(returnValue("mobile gateway"));

            // Test 1: startOperator == null
            oneOf (mobileIpAddressRange).getStartPoint(); will(returnValue(startPoint));
            oneOf (mobileIpAddressRange).getEndPoint(); will(returnValue(endPoint1));
            oneOf (deviceManager).getOperatorForOperatorAliasAndCountry(OperatorAlias.Type.QUOVA, country, networkCarrier); will(returnValue(null));

            // Test 2: endOperator == null
            oneOf (mobileIpAddressRange).getStartPoint(); will(returnValue(startPoint));
            oneOf (mobileIpAddressRange).getEndPoint(); will(returnValue(endPoint1));
            oneOf (deviceManager).getOperatorForOperatorAliasAndCountry(OperatorAlias.Type.QUOVA, country, networkCarrier); will(returnValue(operator1));
            oneOf (deviceManager).getOperatorForOperatorAliasAndCountry(OperatorAlias.Type.QUOVA, country, networkCarrier); will(returnValue(null));

            // Test 3: endOperator != startOperator
            oneOf (mobileIpAddressRange).getStartPoint(); will(returnValue(startPoint));
            oneOf (mobileIpAddressRange).getEndPoint(); will(returnValue(endPoint1));
            oneOf (deviceManager).getOperatorForOperatorAliasAndCountry(OperatorAlias.Type.QUOVA, country, networkCarrier); will(returnValue(operator1));
            oneOf (deviceManager).getOperatorForOperatorAliasAndCountry(OperatorAlias.Type.QUOVA, country, networkCarrier); will(returnValue(operator2));

            // Test 4: startIp == endIp
            oneOf (mobileIpAddressRange).getStartPoint(); will(returnValue(startPoint));
            oneOf (mobileIpAddressRange).getEndPoint(); will(returnValue(startPoint));
            oneOf (deviceManager).getOperatorForOperatorAliasAndCountry(OperatorAlias.Type.QUOVA, country, networkCarrier); will(returnValue(operator1));
            oneOf (deviceManager).getOperatorForOperatorAliasAndCountry(OperatorAlias.Type.QUOVA, country, networkCarrier); will(returnValue(operator1));
            oneOf (commonManager).delete(mobileIpAddressRange);
            
            // Test 5: startIp != endIp && (mid == startPoint || mid == endPoint)
            oneOf (mobileIpAddressRange).getStartPoint(); will(returnValue(startPoint));
            oneOf (mobileIpAddressRange).getEndPoint(); will(returnValue(endPoint2));
            oneOf (deviceManager).getOperatorForOperatorAliasAndCountry(OperatorAlias.Type.QUOVA, country, networkCarrier); will(returnValue(operator1));
            oneOf (deviceManager).getOperatorForOperatorAliasAndCountry(OperatorAlias.Type.QUOVA, country, networkCarrier); will(returnValue(operator1));
            oneOf (commonManager).delete(mobileIpAddressRange);

            // Test 6: startIp != endIp && mid != startPoint && mid != endPoint, midOperator == null
            oneOf (mobileIpAddressRange).getStartPoint(); will(returnValue(startPoint));
            oneOf (mobileIpAddressRange).getEndPoint(); will(returnValue(endPoint1));
            oneOf (deviceManager).getOperatorForOperatorAliasAndCountry(OperatorAlias.Type.QUOVA, country, networkCarrier); will(returnValue(operator1));
            oneOf (deviceManager).getOperatorForOperatorAliasAndCountry(OperatorAlias.Type.QUOVA, country, networkCarrier); will(returnValue(operator1));
            oneOf (deviceManager).getOperatorForOperatorAliasAndCountry(OperatorAlias.Type.QUOVA, country, networkCarrier); will(returnValue(null));

            // Test 7: startIp != endIp && mid != startPoint && mid != endPoint, midOperator == startOperator
            oneOf (mobileIpAddressRange).getStartPoint(); will(returnValue(startPoint));
            oneOf (mobileIpAddressRange).getEndPoint(); will(returnValue(endPoint1));
            oneOf (deviceManager).getOperatorForOperatorAliasAndCountry(OperatorAlias.Type.QUOVA, country, networkCarrier); will(returnValue(operator1));
            oneOf (deviceManager).getOperatorForOperatorAliasAndCountry(OperatorAlias.Type.QUOVA, country, networkCarrier); will(returnValue(operator1));
            oneOf (deviceManager).getOperatorForOperatorAliasAndCountry(OperatorAlias.Type.QUOVA, country, networkCarrier); will(returnValue(operator1));
            oneOf (commonManager).delete(mobileIpAddressRange);
            
            // Test 8: startIp != endIp && mid != startPoint && mid != endPoint, midOperator != startOperator
            oneOf (mobileIpAddressRange).getStartPoint(); will(returnValue(startPoint));
            oneOf (mobileIpAddressRange).getEndPoint(); will(returnValue(endPoint1));
            oneOf (deviceManager).getOperatorForOperatorAliasAndCountry(OperatorAlias.Type.QUOVA, country, networkCarrier); will(returnValue(operator1));
            oneOf (deviceManager).getOperatorForOperatorAliasAndCountry(OperatorAlias.Type.QUOVA, country, networkCarrier); will(returnValue(operator1));
            oneOf (deviceManager).getOperatorForOperatorAliasAndCountry(OperatorAlias.Type.QUOVA, country, networkCarrier); will(returnValue(operator2));
        }});

        // Test 1: startOperator == null
        assertFalse(autoPhaseOutMobileIpAddressRange.phaseOutMobileIpAddressRangeIfPossible(mobileIpAddressRange));

        // Test 2: endOperator == null
        assertFalse(autoPhaseOutMobileIpAddressRange.phaseOutMobileIpAddressRangeIfPossible(mobileIpAddressRange));

        // Test 3: endOperator != startOperator
        assertFalse(autoPhaseOutMobileIpAddressRange.phaseOutMobileIpAddressRangeIfPossible(mobileIpAddressRange));
        
        // Test 4: startIp == endIp
        assertTrue(autoPhaseOutMobileIpAddressRange.phaseOutMobileIpAddressRangeIfPossible(mobileIpAddressRange));
            
        // Test 5: startIp != endIp && (mid == startPoint || mid == endPoint)
        assertTrue(autoPhaseOutMobileIpAddressRange.phaseOutMobileIpAddressRangeIfPossible(mobileIpAddressRange));
            
        // Test 6: startIp != endIp && mid != startPoint && mid != endPoint, midOperator == null
        assertFalse(autoPhaseOutMobileIpAddressRange.phaseOutMobileIpAddressRangeIfPossible(mobileIpAddressRange));

        // Test 7: startIp != endIp && mid != startPoint && mid != endPoint, midOperator == startOperator
        assertTrue(autoPhaseOutMobileIpAddressRange.phaseOutMobileIpAddressRangeIfPossible(mobileIpAddressRange));
            
        // Test 8: startIp != endIp && mid != startPoint && mid != endPoint, midOperator != startOperator
        assertFalse(autoPhaseOutMobileIpAddressRange.phaseOutMobileIpAddressRangeIfPossible(mobileIpAddressRange));
    }
}
