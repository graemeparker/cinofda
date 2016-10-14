package com.byyd.newrelic.cassandra;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.AgentFactory;
import com.newrelic.metrics.publish.configuration.ConfigurationException;

// TODO add junit test
public class CassandraAgentFactory extends AgentFactory {

    @Override
    public Agent createConfiguredAgent(Map<String, Object> properties) throws ConfigurationException {
        
        String name = (String) properties.get("name");
        String host = (String) properties.get("jmx_host");
        Long port = (Long) properties.get("jmx_port");

        String userName = (String) properties.get("userName");
        String password = (String) properties.get("password");

        if (StringUtils.isBlank(host)) {
            throw new ConfigurationException("'host' must be specified.");
        }
        if (port == null) {
            throw new ConfigurationException("'port' cannot be null.");
        }

        CassandraAgent agent = new CassandraAgent(name, host, port, userName, password);
        return agent;
    }

}
