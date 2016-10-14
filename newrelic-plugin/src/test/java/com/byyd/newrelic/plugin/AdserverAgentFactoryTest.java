package com.byyd.newrelic.plugin;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.configuration.ConfigurationException;

@RunWith(MockitoJUnitRunner.class)
public class AdserverAgentFactoryTest {

    AdserverAgentFactory testObj = new AdserverAgentFactory();
    
    @Test
    public void testCreateConfiguredAgent() throws ConfigurationException {
        
        Map<String, Object> properties= new HashMap<>();
        properties.put("name", "QA");
        properties.put("host", "localhost");
        properties.put("port", 9003L);
        properties.put("userName", "jmxUser");
        properties.put("password", "very-secret");

        Agent agent = testObj.createConfiguredAgent(properties);
        
        Assert.assertNotNull(agent);
    }

    @Test
    public void testCreateConfiguredAgentNoPassword() throws ConfigurationException {
        
        Map<String, Object> properties= new HashMap<>();
        properties.put("name", "QA");
        properties.put("host", "localhost");
        properties.put("port", 9003L);
        properties.put("userName", "jmxUser");
//        properties.put("password", "very-secret");

        Agent agent = testObj.createConfiguredAgent(properties);
        
        Assert.assertNotNull(agent);
    }
    
    @Test
    public void testCreateConfiguredAgentWithoutName() throws ConfigurationException, UnknownHostException {
        
        Map<String, Object> properties= new HashMap<>();
//      properties.put("name", "QA");
        properties.put("host", "localhost");
        properties.put("port", 9003L);
        properties.put("userName", "jmxUser");
        properties.put("password", "very-secret");

        Agent agent = testObj.createConfiguredAgent(properties);
        String localhost = InetAddress.getLocalHost().getHostName();
        Assert.assertEquals(localhost, agent.getAgentName());
    }
    
    @Test(expected=ConfigurationException.class)
    public void testCreateConfiguredAgentWithoutHostThrowsException() throws ConfigurationException {
        
        Map<String, Object> properties= new HashMap<>();
        properties.put("name", "QA");
//        properties.put("host", "localhost");
        properties.put("port", 9003L);
        properties.put("userName", "jmxUser");
        properties.put("password", "very-secret");

        Agent agent = testObj.createConfiguredAgent(properties);
        
        Assert.assertNotNull(agent);
    }
    
    @Test(expected=ConfigurationException.class)
    public void testCreateConfiguredAgentWithoutPortThrowsException() throws ConfigurationException {
        
        Map<String, Object> properties= new HashMap<>();
        properties.put("name", "QA");
        properties.put("host", "localhost");
//        properties.put("port", 9003L);
        properties.put("userName", "jmxUser");
        properties.put("password", "very-secret");

        Agent agent = testObj.createConfiguredAgent(properties);
        
        Assert.assertNotNull(agent);
    }
    
}
