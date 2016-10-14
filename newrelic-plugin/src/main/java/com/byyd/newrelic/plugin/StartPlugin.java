package com.byyd.newrelic.plugin;

import com.newrelic.metrics.publish.AgentFactory;
import com.newrelic.metrics.publish.Runner;
import com.newrelic.metrics.publish.configuration.ConfigurationException;

public class StartPlugin {

    public static void main(String[] args) {
        try {
            Runner runner = new Runner();
            AgentFactory agentFactory = new AdserverAgentFactory();
            runner.add(agentFactory);
            runner.setupAndRun(); // Never returns
        } catch (ConfigurationException e) {
            System.err.println("ERROR: " + e.getMessage());
            System.exit(1);
        }
    }

}
