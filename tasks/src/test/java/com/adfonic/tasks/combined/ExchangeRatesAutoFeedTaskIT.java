package com.adfonic.tasks.combined;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { 
        "classpath:/adfonic-toolsdb-context.xml",
        "classpath:/adfonic-tasks-context.xml",
        "classpath:/exchangerates_autofeed_task_test.xml"
        })
public class ExchangeRatesAutoFeedTaskIT {
    
    @Autowired
    ExchangeRatesAutoFeedTask exchangeRatesAutoFeedTask;
	
    @Test
    public void testIsDeviceIdValid() {
        exchangeRatesAutoFeedTask.doTask();
    }
	
}
