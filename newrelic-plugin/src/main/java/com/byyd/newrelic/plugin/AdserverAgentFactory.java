package com.byyd.newrelic.plugin;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.AgentFactory;
import com.newrelic.metrics.publish.configuration.ConfigurationException;

// TODO add junit test
public class AdserverAgentFactory extends AgentFactory {

    @Override
    public Agent createConfiguredAgent(Map<String, Object> properties) throws ConfigurationException {
        
        String name = (String) properties.get("name");
        String host = (String) properties.get("host");
        Long port = (Long) properties.get("port");

        String userName = (String) properties.get("userName");
        String password = (String) properties.get("password");

        if (StringUtils.isBlank(host)) {
            throw new ConfigurationException("'host' must be specified.");
        }
        if (port == null) {
            throw new ConfigurationException("'port' cannot be null.");
        }

        AdserverAgent agent = new AdserverAgent(name, host, port, userName, password);
        return agent;
    }

}
