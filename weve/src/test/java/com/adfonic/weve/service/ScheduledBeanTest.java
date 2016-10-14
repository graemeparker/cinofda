package com.adfonic.weve.service;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.weve.test.SchedulingTestBean;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:schedule-context.xml")
public class ScheduledBeanTest {

	@Autowired
	SchedulingTestBean schedulingBean;
	
	@Test
	public void shouldCallLogSomethingAtCorrectInterval() throws InterruptedException {
	    int counter = schedulingBean.getCounter();
		System.out.println("Starting test...");
		Thread.sleep(4000);
		assertTrue(schedulingBean.getCounter()>counter);
	}
}
