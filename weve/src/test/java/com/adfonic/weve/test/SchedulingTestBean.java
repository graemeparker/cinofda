package com.adfonic.weve.test;

import org.joda.time.DateTime;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SchedulingTestBean {
	
	private int counter = 0;
	@Scheduled(cron = "${weve.operatorcache.schedule}")
	public void logSomething() {
		counter++;
		System.out.println("Running scheduled task at " + DateTime.now());
	}
	
	public int getCounter() {
		return this.counter;
	}
}
