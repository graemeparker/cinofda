package com.adfonic.adserver.simulation.impl;

import javax.jms.JMSException;
import javax.jms.Queue;

public class SimulationQueue implements Queue {

	@Override
	public String getQueueName() throws JMSException {
		return null;
	}

}
