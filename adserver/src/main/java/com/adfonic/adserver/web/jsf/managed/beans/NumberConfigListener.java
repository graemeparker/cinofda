package com.adfonic.adserver.web.jsf.managed.beans;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class NumberConfigListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent event) {
		System.setProperty("org.apache.el.parser.COERCE_TO_ZERO", "false");
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		// NOOP
	}
}
