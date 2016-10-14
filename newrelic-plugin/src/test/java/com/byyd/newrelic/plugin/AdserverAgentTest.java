package com.byyd.newrelic.plugin;

import java.util.Map;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AdserverAgentTest {

    @Mock
    MBeanServerConnection mbsc;

    @Mock
    JMXConnector jmxc;

    @Mock
    MBeanInfo mBeanInfo;

    private MBeanAttributeInfo a1 = new MBeanAttributeInfo("counter1", "java.lang.String", "Counter counter1: ", true, false, false);
    private MBeanAttributeInfo a2 = new MBeanAttributeInfo("AdController", "java.lang.String", "Counter AdController: ", true, false, false);
    private MBeanAttributeInfo a3 = new MBeanAttributeInfo("LongCounter", "java.lang.Long", "not supported type: ", true, false, false);

    private MBeanAttributeInfo[] attributes = new MBeanAttributeInfo[] { a1, a2, a3 };

    @Spy
    private AdserverAgent testObj = new AdserverAgent("QA", "localhost", 9003L, "jmxUser", "topSecretPassword");

    @Test
    public void buildJmxServiceUrl() {
        String url = testObj.buildJmxServiceUrl();
        Assert.assertEquals("service:jmx:rmi:///jndi/rmi://localhost:9003/jmxrmi", url);
    }

    @Test
    public void buildCredentials() {
        Map<String, String[]> credentials = testObj.buildCredentials();
        Assert.assertEquals(1, credentials.size());

        String[] arr = credentials.get(JMXConnector.CREDENTIALS);
        Assert.assertEquals("jmxUser", arr[0]);
        Assert.assertEquals("topSecretPassword", arr[1]);
    }

    @Test
    public void buildCredentialsWithoutPassword() {
        AdserverAgent testObj = new AdserverAgent("QA", "localhost", 9003L, "jmxUser", null);
        Map<String, String[]> credentials = testObj.buildCredentials();
        Assert.assertNull(credentials);
    }

    @Test
    public void buildCredentialsWithoutUsername() {
        AdserverAgent testObj = new AdserverAgent("QA", "localhost", 9003L, "", "topSecretPassword");
        Map<String, String[]> credentials = testObj.buildCredentials();
        Assert.assertNull(credentials);
    }

    @Test
    public void testReportMetricData() throws Exception {
        ObjectName objectName = new ObjectName("AdfonicCounters:mbean=GenericCounters");

        Mockito.when(mbsc.getMBeanInfo(objectName)).thenReturn(mBeanInfo);
        Mockito.when(mbsc.getAttribute(objectName, "counter1")).thenReturn("123");
        Mockito.when(mbsc.getAttribute(objectName, "AdController")).thenReturn("456");
        Mockito.when(mbsc.getAttribute(objectName, "LongCounter")).thenReturn("invalid");
        Mockito.when(mBeanInfo.getAttributes()).thenReturn(attributes);

        // we don't really want to call real function
        Mockito.doNothing().when(testObj).reportMetric(Mockito.anyString(), Mockito.anyString(), Mockito.anyLong());

        testObj.reportMetricData(mbsc);

        Mockito.verify(testObj).reportMetric("count/counter1", "count", 123L);
        Mockito.verify(testObj).reportMetric("count/AdController", "count", 456L);
        Mockito.verify(testObj, Mockito.never()).reportMetric("LongCounter", "", 789L);
    }

    @Ignore
    @Test
    public void testGetAgentName() {

        String agentName = testObj.getAgentName();
        Assert.assertEquals("QA", agentName);
    }

    @Test
    public void testPollCycle() throws Exception {
        Mockito.doReturn(jmxc).when(testObj).getMbeanServerConnection();
        Mockito.doReturn(mbsc).when(jmxc).getMBeanServerConnection();
        Mockito.doNothing().when(testObj).reportMetricData(mbsc);

        testObj.pollCycle();

        Mockito.verify(testObj).reportMetricData(mbsc);
    }

    @Test
    public void testAsNumber() {
        Assert.assertNull(testObj.asNumber(null, "".getClass().getName()));
        Assert.assertEquals(123L, testObj.asNumber("123", String.class.getName()));
        Assert.assertEquals(123L, testObj.asNumber(123L, Long.class.getName()));
        Assert.assertEquals(123, testObj.asNumber(123, Integer.class.getName()));
        Assert.assertEquals(123.0D, testObj.asNumber(123.0D, Double.class.getName()));
        Assert.assertEquals(123.0f, testObj.asNumber(123.0f, Float.class.getName()));
    }

    @Test(expected = ClassCastException.class)
    public void testAsNumberMismatchType() {
        testObj.asNumber("123", Double.class.getName());
    }
}
